package io.keyko.nevermined.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import io.keyko.common.exceptions.CryptoException;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.common.helpers.JwtHelper;
import io.keyko.common.helpers.UrlHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.contracts.*;
import io.keyko.nevermined.core.conditions.LockPaymentConditionPayable;
import io.keyko.nevermined.exceptions.*;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.types.AuthorizationService;
import io.keyko.nevermined.models.service.types.MetadataService;
import io.keyko.secretstore.core.EvmDto;
import io.keyko.secretstore.core.SecretStoreDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple9;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for the Managers
 */
public abstract class BaseManager {

    protected static final Logger log = LogManager.getLogger(BaseManager.class);
    private static final int MAX_SS_RETRIES = 10;
    private static final long SS_DECRYPTION_SLEEP = 1000l;

    private KeeperService keeperService;
    private MetadataApiService metadataApiService;
    private SecretStoreDto secretStoreDto;
    private EvmDto evmDto;
    private SecretStoreManager secretStoreManager;
    protected NeverminedToken tokenContract;
    protected Dispenser dispenser;
    protected DIDRegistry didRegistry;
    protected LockPaymentConditionPayable lockCondition;
    protected EscrowPaymentCondition escrowCondition;
    protected AccessCondition accessCondition;
    protected TransferNFTCondition transferNFTCondition;
    protected TransferDIDOwnershipCondition transferDIDCondition;
    protected NFTAccessCondition nftAccessCondition;
    protected NFTHolderCondition nftHolderCondition;
    protected TemplateStoreManager templateStoreManager;
    protected AccessTemplate accessTemplate;
    protected EscrowComputeExecutionTemplate escrowComputeExecutionTemplate;
    protected NFTSalesTemplate nftSalesTemplate;
    protected NFTAccessTemplate nftAccessTemplate;
    protected DIDSalesTemplate didSalesTemplate;
    protected AgreementStoreManager agreementStoreManager;
    protected ConditionStoreManager conditionStoreManager;
    protected ComputeExecutionCondition computeExecutionCondition;
    protected Condition condition;
    protected ContractAddresses contractAddresses = new ContractAddresses();
    protected Config config = ConfigFactory.load();

    protected Account mainAccount;
    protected String providerAddress;

    public static class ContractAddresses {

        private String lockPaymentConditionAddress;
        private String accessConditionAddress;

        public ContractAddresses() {
        }

        public String getLockPaymentConditionAddress() {
            return lockPaymentConditionAddress;
        }

        public void setLockPaymentConditionAddress(String address) {
            this.lockPaymentConditionAddress = address;
        }

        public String getAccessConditionAddress() {
            return accessConditionAddress;
        }

        public void setAccessConditionAddress(String address) {
            this.accessConditionAddress = address;
        }
    }

    /**
     * Constructor
     *
     * @param keeperService      KeeperService
     * @param metadataApiService MetadataApiService
     */
    public BaseManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        this.keeperService = keeperService;
        this.metadataApiService = metadataApiService;
    }

    private SecretStoreManager getSecretStoreInstance(AuthorizationService authorizationService) {

        if (authorizationService == null)
            return getSecretStoreManager();

        return SecretStoreManager.getInstance(SecretStoreDto.builder(authorizationService.serviceEndpoint), evmDto);
    }

    protected DDO buildDDO(MetadataService metadataService, String address) throws DDOException {
        try {
            return new DDO(metadataService, address, "");
        } catch (DIDFormatException e) {
            throw new DDOException("Error building DDO", e);
        }
    }

    public List<AssetMetadata.File> getDecriptedSecretStoreMetadataFiles(DDO ddo)
            throws IOException, EncryptionException, InterruptedException {
        return getDecriptedSecretStoreMetadataFiles(ddo, MAX_SS_RETRIES);
    }

    public List<AssetMetadata.File> getDecriptedSecretStoreMetadataFiles(DDO ddo, int retries)
            throws IOException, EncryptionException, InterruptedException {
        int counter = 0;
        AuthorizationService authorizationService = ddo.getAuthorizationService();
        SecretStoreManager secretStoreManager = getSecretStoreInstance(authorizationService);

        String jsonFiles = null;
        while (counter < retries) {
            try {
                jsonFiles = secretStoreManager.decryptDocument(ddo.getDID().getHash(),
                        ddo.getMetadataService().attributes.encryptedFiles);
                return DDO.fromJSON(new TypeReference<ArrayList<AssetMetadata.File>>() {
                }, jsonFiles);
            } catch (EncryptionException e) {
                log.warn("Unable to decrypt [" + counter + "]");
                counter++;
                Thread.sleep(SS_DECRYPTION_SLEEP);
            }
        }
        throw new EncryptionException("Unable to decrypt document after " + retries + " retries");

    }

    public boolean tokenApprove(NeverminedToken tokenContract, String spenderAddress, String price)
            throws TokenApproveException {

        String checksumAddress = Keys.toChecksumAddress(spenderAddress);

        try {

            TransactionReceipt receipt = tokenContract.approve(checksumAddress, new BigInteger(price)).send();

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
     * Given a DID, fetch the on-chain url from the DIDRegistry. That urls resolves
     * the DDO stored in the Metadata API. This method returns the DDO found
     *
     * @param did the did
     * @return DDO
     * @throws DDOException DDOException
     */
    public DDO resolveDID(DID did) throws DDOException {

        try {
            final Tuple9<String, byte[], String, String, BigInteger, List<String>, BigInteger, BigInteger, BigInteger> didAttributes = didRegistry
                    .getDIDRegister(EncodingHelper.hexStringToBytes(did.getHash())).send();

            String didUrl = didAttributes.component3();

            MetadataApiService ddoMetadataDto = MetadataApiService.getInstance(UrlHelper.getBaseUrl(didUrl));
            return ddoMetadataDto.getDDO(didUrl);

        } catch (Exception ex) {
            log.error("Unable to retrieve DDO " + ex.getMessage());
            throw new DDOException("Unable to retrieve DDO " + ex.getMessage());
        }
    }

    /**
     * Given a DID, scans the DIDRegistry events to resolve the Metadata API url and
     * return the DDO found
     *
     * @param did the did
     * @return DDO
     * @throws DDOException DDOException
     */
    public DDO resolveDIDFromEvent(DID did) throws DDOException {

        EthFilter didFilter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST,
                didRegistry.getContractAddress());

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
            List<Type> nonIndexed = FunctionReturnDecoder.decode(((EthLog.LogObject) logResult).getData(),
                    event.getNonIndexedParameters());
            String didUrl = nonIndexed.get(0).getValue().toString();

            MetadataApiService ddoMetadataDto = MetadataApiService.getInstance(UrlHelper.getBaseUrl(didUrl));
            return ddoMetadataDto.getDDO(didUrl);

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
     * Get the MetadataApiService
     *
     * @return MetadataApiService
     */
    public MetadataApiService getMetadataApiService() {
        return metadataApiService;
    }

    /**
     * Set the MetadataApiService
     *
     * @param metadataApiService MetadataApiService
     * @return this
     */
    public BaseManager setMetadataApiService(MetadataApiService metadataApiService) {
        this.metadataApiService = metadataApiService;
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
     * Set the EvmDto necessary to stablish the encryption/decryption flow necessary
     * by Secret Store
     *
     * @param evmDto EvmDto
     * @return this
     */
    public BaseManager setEvmDto(EvmDto evmDto) {
        this.evmDto = evmDto;
        return this;
    }

    /**
     * It sets the NeverminedToken stub instance
     *
     * @param contract NeverminedToken instance
     * @return BaseManager instance
     */
    public BaseManager setTokenContract(NeverminedToken contract) {
        this.tokenContract = contract;
        return this;
    }

    /**
     * It sets the NeverminedToken stub instance
     *
     * @param contract NeverminedToken instance
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
     * It sets the AccessTemplate stub instance
     *
     * @param contract AccessTemplate instance
     * @return BaseManager instance
     */
    public BaseManager setAccessTemplate(AccessTemplate contract) {
        this.accessTemplate = contract;
        return this;
    }

    /**
     * It sets the NFTSalesTemplate stub instance
     *
     * @param contract NFTSalesTemplate instance
     * @return BaseManager instance
     */
    public BaseManager setNFTSalesTemplate(NFTSalesTemplate contract) {
        this.nftSalesTemplate = contract;
        return this;
    }

    /**
     * It sets the NFTAccessTemplate stub instance
     *
     * @param contract NFTAccessTemplate instance
     * @return BaseManager instance
     */
    public BaseManager setNFTAccessTemplate(NFTAccessTemplate contract) {
        this.nftAccessTemplate = contract;
        return this;
    }

    /**
     * It sets the DIDSalesTemplate stub instance
     *
     * @param contract DIDSalesTemplate instance
     * @return BaseManager instance
     */
    public BaseManager setDIDSalesTemplate(DIDSalesTemplate contract) {
        this.didSalesTemplate = contract;
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
     * @return LockPaymentCondition instance
     */
    public LockPaymentCondition getLockCondition() {
        return lockCondition;
    }

    /**
     * It sets the LockPaymentCondition instance
     *
     * @param lockCondition instance
     * @return BaseManager instance
     */
    public BaseManager setLockCondition(LockPaymentConditionPayable lockCondition) {
        this.lockCondition = lockCondition;
        return this;
    }

    /**
     * It gets the EscrowPaymentCondition stub instance
     *
     * @return EscrowPaymentCondition instance
     */
    public EscrowPaymentCondition getEscrowCondition() {
        return escrowCondition;
    }

    /**
     * It sets the EscrowPaymentCondition instance
     *
     * @param escrowCondition EscrowPaymentCondition instance
     * @return BaseManager instance
     */
    public BaseManager setEscrowCondition(EscrowPaymentCondition escrowCondition) {
        this.escrowCondition = escrowCondition;
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
    public BaseManager setEscrowComputeExecutionTemplate(
            EscrowComputeExecutionTemplate escrowComputeExecutionTemplate) {
        this.escrowComputeExecutionTemplate = escrowComputeExecutionTemplate;
        return this;
    }

    /**
     * It gets the AccessCondition stub instance
     *
     * @return AccessCondition instance
     */
    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    /**
     * It sets the EscrowPaymentCondition instance
     *
     * @param accessCondition AccessCondition instance
     * @return BaseManager instance
     */
    public BaseManager setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    /**
     * It gets the TransferNFTCondition stub instance
     *
     * @return TransferNFTCondition instance
     */
    public TransferNFTCondition getTransferNFTCondition() {
        return transferNFTCondition;
    }

    /**
     * It sets the TransferNFTCondition instance
     *
     * @param _condition TransferNFTCondition instance
     * @return BaseManager instance
     */
    public BaseManager setTransferNFTCondition(TransferNFTCondition _condition) {
        this.transferNFTCondition = _condition;
        return this;
    }

    /**
     * It gets the NFTAccessCondition stub instance
     *
     * @return NFTAccessCondition instance
     */
    public NFTAccessCondition getNFTAccessCondition() {
        return nftAccessCondition;
    }

    /**
     * It sets the NFTAccessCondition instance
     *
     * @param _condition NFTAccessCondition instance
     * @return BaseManager instance
     */
    public BaseManager setNFTAccessCondition(NFTAccessCondition _condition) {
        this.nftAccessCondition = _condition;
        return this;
    }

    /**
     * It gets the NFTHolderCondition stub instance
     *
     * @return NFTHolderCondition instance
     */
    public NFTHolderCondition getNFTHolderCondition() {
        return nftHolderCondition;
    }

    /**
     * It sets the NFTHolderCondition instance
     *
     * @param _condition NFTHolderCondition instance
     * @return BaseManager instance
     */
    public BaseManager setNFTHolderCondition(NFTHolderCondition _condition) {
        this.nftHolderCondition = _condition;
        return this;
    }

    /**
     * It gets the TransferDIDOwnershipCondition stub instance
     *
     * @return TransferDIDOwnershipCondition instance
     */
    public TransferDIDOwnershipCondition getTransferDIDCondition() {
        return transferDIDCondition;
    }

    /**
     * It sets the TransferDIDOwnershipCondition instance
     *
     * @param _condition TransferDIDOwnershipCondition instance
     * @return BaseManager instance
     */
    public BaseManager setTransferDIDCondition(TransferDIDOwnershipCondition _condition) {
        this.transferDIDCondition = _condition;
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

    public byte[] getTemplateIdByName(String contractName) {
        return Hash.sha3(contractName.getBytes());

    }

    public String generateSignature(String message) throws IOException, CipherException {
        return EncodingHelper
                .signatureToString(EthereumHelper.signMessage(message, getKeeperService().getCredentials()));
    }

    /**
     * Generate the Grant Token for the Access service.
     *
     * @param serviceAgreementId The Service Agreement Id.
     * @param did The did.
     * @return String The Grant Token.
     * @throws CryptoException CryptoException
     * @throws IOException IOException
     * @throws CipherException CipherException
     */
    public String generateAccessGrantToken(String serviceAgreementId, DID did)
            throws CryptoException, IOException, CipherException {
        return JwtHelper.generateAccessGrantToken(getKeeperService().getCredentials(), serviceAgreementId, did.getDid());
    }

    /**
     * Generate the Grant Token for the NFT Access service.
     *
     * @param serviceAgreementId The Service Agreement Id.
     * @param did The did.
     * @return String The Grant Token.
     * @throws CryptoException CryptoException
     * @throws IOException IOException
     * @throws CipherException CipherException
     */
    public String generateNFTAccessGrantToken(String serviceAgreementId, DID did)
            throws CryptoException, IOException, CipherException {
        return JwtHelper.generateNFTAccessGrantToken(getKeeperService().getCredentials(), serviceAgreementId, did.getDid());
    }

    /**
     * Generat the Grant Token for the Download service.
     *
     * @param did The did.
     * @return String The Grant Token.
     * @throws CryptoException CryptoException
     * @throws IOException IOExcception
     * @throws CipherException CipherException
     */
    public String generateDownloadGrantToken(DID did) throws CryptoException, IOException, CipherException {
        return JwtHelper.generateDownloadGrantToken(getKeeperService().getCredentials(), did.getDid());
    }

    /**
     * Generate the Grant Token for the Execute service.
     *
     * @param serviceAgreementId The Service Agreement Id.
     * @param workflowDid The workflow did.
     * @return String The Grant Token.
     * @throws CryptoException CryptoException
     * @throws IOException IOException
     * @throws CipherException CipherException
     */
    public String generateExecuteGrantToken(String serviceAgreementId, DID workflowDid)
            throws CryptoException, IOException, CipherException {
        return JwtHelper.generateExecuteGrantToken(getKeeperService().getCredentials(), serviceAgreementId, workflowDid.getDid());
    }

    /**
     * Generate the Grant Token for the Compute service.
     *
     * @param serviceAgreementId The Service Agreement Id.
     * @param executionId The excution Id of the compute job.
     * @return String The Grant Token.
     * @throws CryptoException CryptoException
     * @throws IOException IOException
     * @throws CipherException CipherException
     */
    public String generateComputeGrantToken(String serviceAgreementId, String executionId)
            throws CryptoException, IOException, CipherException {
        return JwtHelper.generateComputeGrantToken(getKeeperService().getCredentials(), serviceAgreementId, executionId);
    }

    @Override
    public String toString() {
        return "BaseManager{" +
                "keeperService=" + keeperService +
                ", metadataApiService=" + metadataApiService +
                '}';
    }
}