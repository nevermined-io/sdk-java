/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.common.helpers.UrlHelper;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.service.types.AuthorizationService;
import com.oceanprotocol.squid.models.service.types.MetadataService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.squid.exceptions.DIDFormatException;
import com.oceanprotocol.squid.exceptions.EncryptionException;
import com.oceanprotocol.squid.exceptions.TokenApproveException;
import com.oceanprotocol.squid.exceptions.EthereumException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for the Managers
 */
public abstract class BaseManager {

    protected static final Logger log = LogManager.getLogger(BaseManager.class);

    private KeeperService keeperService;
    private AquariusService aquariusService;
    private SecretStoreDto secretStoreDto;
    private EvmDto evmDto;
    private SecretStoreManager secretStoreManager;
    protected OceanToken tokenContract;
    protected Dispenser dispenser;
    protected DIDRegistry didRegistry;
    protected EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate;
    protected LockRewardCondition lockRewardCondition;
    protected EscrowReward escrowReward;
    protected AccessSecretStoreCondition accessSecretStoreCondition;
    protected TemplateStoreManager templateStoreManager;
    protected AgreementStoreManager agreementStoreManager;
    protected ConditionStoreManager conditionStoreManager;
    protected ComputeExecutionCondition computeExecutionCondition;
    protected EscrowComputeExecutionTemplate escrowComputeExecutionTemplate;
    protected ContractAddresses contractAddresses = new ContractAddresses();
    protected Config config = ConfigFactory.load();

    protected Account mainAccount;
    protected String providerAddress;

    public static class ContractAddresses {

        private String lockRewardConditionAddress;
        private String accessSecretStoreConditionAddress;

        public ContractAddresses() {
        }

        public String getLockRewardConditionAddress() {
            return lockRewardConditionAddress;
        }

        public void setLockRewardConditionAddress(String address) {
            this.lockRewardConditionAddress = address;
        }

        public String getAccessSecretStoreConditionAddress() {
            return accessSecretStoreConditionAddress;
        }

        public void setAccessSecretStoreConditionAddress(String address) {
            this.accessSecretStoreConditionAddress = address;
        }
    }


    /**
     * Constructor
     *
     * @param keeperService   KeeperService
     * @param aquariusService AquariusService
     */
    public BaseManager(KeeperService keeperService, AquariusService aquariusService) {
        this.keeperService = keeperService;
        this.aquariusService = aquariusService;
    }

    private SecretStoreManager getSecretStoreInstance(AuthorizationService authorizationService) {

        if (authorizationService == null)
            return getSecretStoreManager();

        return SecretStoreManager.getInstance(SecretStoreDto.builder(authorizationService.serviceEndpoint), evmDto);
    }


    protected DDO buildDDO(MetadataService metadataService, AuthorizationService authorizationService, String address, int threshold) throws DDOException {

        try {
            String signature = "";
            return new DDO(metadataService, address, signature);
        } catch (DIDFormatException e) {
            throw new DDOException("Error building DDO", e);
        }

    }

    protected DDO buildDDO(MetadataService metadataService, AuthorizationService authorizationService, String address) throws DDOException {
        return this.buildDDO(metadataService, authorizationService, address, 0);
    }

    public List<AssetMetadata.File> getMetadataFiles(DDO ddo) throws IOException, EncryptionException {

        AuthorizationService authorizationService = ddo.getAuthorizationService();
        SecretStoreManager secretStoreManager = getSecretStoreInstance(authorizationService);

        String jsonFiles = secretStoreManager.decryptDocument(ddo.getDid().getHash(), ddo.getMetadataService().attributes.encryptedFiles);
        return DDO.fromJSON(new TypeReference<ArrayList<AssetMetadata.File>>() {
        }, jsonFiles);
    }

    public boolean tokenApprove(OceanToken tokenContract, String spenderAddress, String price) throws TokenApproveException {

        String checksumAddress = Keys.toChecksumAddress(spenderAddress);

        try {

            TransactionReceipt receipt = tokenContract.approve(
                    checksumAddress,
                    new BigInteger(price)
            ).send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing Token Approve: " + receipt.getStatus();
                log.error(msg);
                throw new TokenApproveException(msg);
            }

            log.debug("Token Approve transactionReceipt OK ");
            return true;

        } catch (Exception e) {

            String msg = "Error executing Token Approve ";
            log.error(msg + ": " + e.getMessage());
            throw new TokenApproveException(msg, e);
        }

    }


    /**
     * Given a DID, scans the DIDRegistry events on-chain to resolve the
     * Metadata API url and return the DDO found
     *
     * @param did the did
     * @return DDO
     * @throws DDOException      DDOException
     */
    public DDO resolveDID(DID did) throws DDOException {

        EthFilter didFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                didRegistry.getContractAddress()
        );

        try {

            final Event event = didRegistry.DIDATTRIBUTEREGISTERED_EVENT;
            final String eventSignature = EventEncoder.encode(event);
            didFilter.addSingleTopic(eventSignature);

            String didTopic = "0x" + did.getHash();
            didFilter.addOptionalTopics(didTopic);

            EthLog ethLog;

            try {
                ethLog = getKeeperService().getWeb3().ethGetLogs(didFilter).send();
            } catch (IOException e) {
                throw new EthereumException("Error searching DID " + did.toString() + " onchain: " + e.getMessage());
            }

            List<EthLog.LogResult> logs = ethLog.getLogs();

            int numLogs = logs.size();
            if (numLogs < 1)
                throw new DDOException("No events found for " + did.toString());

            EthLog.LogResult logResult = logs.get(numLogs - 1);
            List<Type> nonIndexed = FunctionReturnDecoder.decode(((EthLog.LogObject) logResult).getData(), event.getNonIndexedParameters());
            String ddoUrl = nonIndexed.get(0).getValue().toString();
            String didUrl = UrlHelper.parseDDOUrl(ddoUrl, did.toString());

            AquariusService ddoAquariosDto = AquariusService.getInstance(UrlHelper.getBaseUrl(didUrl));
            return ddoAquariosDto.getDDO(didUrl);

        } catch (Exception ex) {
            log.error("Unable to retrieve DDO " + ex.getMessage());
            throw new DDOException("Unable to retrieve DDO " + ex.getMessage());
        }
    }


    public ContractAddresses getContractAddresses() {
        return contractAddresses;
    }

    /**
     * Get the KeeperService
     *
     * @return KeeperService
     */
    public KeeperService getKeeperService() {
        return keeperService;
    }

    /**
     * Set the KeeperService
     *
     * @param keeperService KeeperService
     * @return this
     */
    public BaseManager setKeeperService(KeeperService keeperService) {
        this.keeperService = keeperService;
        return this;
    }

    /**
     * Get the AquariusService
     *
     * @return AquariusService
     */
    public AquariusService getAquariusService() {
        return aquariusService;
    }

    /**
     * Set the AquariusService
     *
     * @param aquariusService AquariusService
     * @return this
     */
    public BaseManager setAquariusService(AquariusService aquariusService) {
        this.aquariusService = aquariusService;
        return this;
    }

    /**
     * Get the SecretStoreDto
     *
     * @return SecretStoreDto
     */
    public SecretStoreDto getSecretStoreDto() {
        return secretStoreDto;
    }

    /**
     * Set the SecretStoreDto
     *
     * @param secretStoreDto SecretStoreDto
     * @return this
     */
    public BaseManager setSecretStoreDto(SecretStoreDto secretStoreDto) {
        this.secretStoreDto = secretStoreDto;
        return this;
    }

    /**
     * Get the SecretStoreManager
     *
     * @return SecretStoreDto
     */
    public SecretStoreManager getSecretStoreManager() {
        return secretStoreManager;
    }

    /**
     * Set the SecretStoreManager
     *
     * @param secretStoreManager SecretStoreDto
     * @return this
     */
    public BaseManager setSecretStoreManager(SecretStoreManager secretStoreManager) {
        this.secretStoreManager = secretStoreManager;
        return this;
    }

    /**
     * Get the EvmDto
     *
     * @return EvmDto
     */
    public EvmDto getEvmDto() {
        return evmDto;
    }

    /**
     * Set the EvmDto necessary to stablish the encryption/decryption flow necessary by Secret Store
     *
     * @param evmDto EvmDto
     * @return this
     */
    public BaseManager setEvmDto(EvmDto evmDto) {
        this.evmDto = evmDto;
        return this;
    }

    /**
     * It sets the OceanToken stub instance
     *
     * @param contract OceanToken instance
     * @return BaseManager instance
     */
    public BaseManager setTokenContract(OceanToken contract) {
        this.tokenContract = contract;
        return this;
    }

    /**
     * It sets the OceanToken stub instance
     *
     * @param contract OceanToken instance
     * @return BaseManager instance
     */
    public BaseManager setTemplateStoreManagerContract(TemplateStoreManager contract) {
        this.templateStoreManager = contract;
        return this;
    }

    /**
     * It sets the Dispenser stub instance
     *
     * @param contract Dispenser instance
     * @return BaseManager instance
     */
    public BaseManager setDispenserContract(Dispenser contract) {
        this.dispenser = contract;
        return this;
    }


    /**
     * It sets the EscrowAccessSecretStoreTemplate stub instance
     *
     * @param contract EscrowAccessSecretStoreTemplate instance
     * @return BaseManager instance
     */
    public BaseManager setEscrowAccessSecretStoreTemplate(EscrowAccessSecretStoreTemplate contract) {
        this.escrowAccessSecretStoreTemplate = contract;
        return this;
    }


    /**
     * It sets the DIDRegistry stub instance
     *
     * @param contract DIDRegistry instance
     * @return BaseManager instance
     */
    public BaseManager setDidRegistryContract(DIDRegistry contract) {
        this.didRegistry = contract;
        return this;
    }

    /**
     * It sets the AgreementStoreManager stub instance
     *
     * @param contract AgreementStoreManager instance
     * @return BaseManager instance
     */
    public BaseManager setAgreementStoreManagerContract(AgreementStoreManager contract) {
        this.agreementStoreManager = contract;
        return this;
    }


    /**
     * It sets the AgreementStoreManager stub instance
     *
     * @param contract AgreementStoreManager instance
     * @return BaseManager instance
     */
    public BaseManager setConditionStoreManagerContract(ConditionStoreManager contract) {
        this.conditionStoreManager = contract;
        return this;
    }

    /**
     * It gets the lockRewardCondition stub instance
     *
     * @return LockRewardCondition instance
     */
    public LockRewardCondition getLockRewardCondition() {
        return lockRewardCondition;
    }

    /**
     * It sets the LockRewardCondition instance
     *
     * @param lockRewardCondition instance
     * @return BaseManager instance
     */
    public BaseManager setLockRewardCondition(LockRewardCondition lockRewardCondition) {
        this.lockRewardCondition = lockRewardCondition;
        return this;
    }

    /**
     * It gets the EscrowReward stub instance
     *
     * @return EscrowReward instance
     */
    public EscrowReward getEscrowReward() {
        return escrowReward;
    }

    /**
     * It sets the EscrowReward instance
     *
     * @param escrowReward EscrowReward instance
     * @return BaseManager instance
     */
    public BaseManager setEscrowReward(EscrowReward escrowReward) {
        this.escrowReward = escrowReward;
        return this;
    }

    /**
     * It gets the ComputeExecutionCondition stub instance
     *
     * @return ComputeExecutionCondition instance
     */
    public ComputeExecutionCondition getComputeExecutionCondition() {
        return computeExecutionCondition;
    }

    /**
     * It sets the ComputeExecutionCondition instance
     *
     * @param computeExecutionCondition ComputeExecutionCondition instance
     * @return BaseManager instance
     */
    public BaseManager setComputeExecutionCondition(ComputeExecutionCondition computeExecutionCondition) {
        this.computeExecutionCondition = computeExecutionCondition;
        return this;
    }

    /**
     * It gets the EscrowComputeExecutionTemplate stub instance
     *
     * @return EscrowComputeExecutionTemplate instance
     */
    public EscrowComputeExecutionTemplate getEscrowComputeExecutionTemplate() {
        return escrowComputeExecutionTemplate;
    }

    /**
     * It sets the EscrowComputeExecutionTemplate instance
     *
     * @param escrowComputeExecutionTemplate EscrowComputeExecutionTemplate instance
     * @return BaseManager instance
     */
    public BaseManager setEscrowComputeExecutionTemplate(EscrowComputeExecutionTemplate escrowComputeExecutionTemplate) {
        this.escrowComputeExecutionTemplate = escrowComputeExecutionTemplate;
        return this;
    }

    /**
     * It gets the AccessSecretStoreCondition stub instance
     *
     * @return AccessSecretStoreCondition instance
     */
    public AccessSecretStoreCondition getAccessSecretStoreCondition() {
        return accessSecretStoreCondition;
    }

    /**
     * It sets the EscrowReward instance
     *
     * @param accessSecretStoreCondition AccessSecretStoreCondition instance
     * @return BaseManager instance
     */
    public BaseManager setAccessSecretStoreCondition(AccessSecretStoreCondition accessSecretStoreCondition) {
        this.accessSecretStoreCondition = accessSecretStoreCondition;
        return this;
    }


    public Account getMainAccount() {
        return mainAccount;
    }

    public BaseManager setMainAccount(Account mainAccount) {
        this.mainAccount = mainAccount;
        return this;
    }


    public String getProviderAddress() {
        return providerAddress;
    }

    public BaseManager setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
        return this;
    }

    @Override
    public String toString() {
        return "BaseManager{" +
                "keeperService=" + keeperService +
                ", aquariusService=" + aquariusService +
                '}';
    }
}