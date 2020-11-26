package io.keyko.nevermined.api.helper;

import io.keyko.secretstore.core.EvmDto;
import io.keyko.secretstore.core.SecretStoreDto;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.contracts.*;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.manager.*;
import org.web3j.crypto.CipherException;

import java.io.IOException;

/**
 * Helper to initialize all the managers, services and contracts needed for the API
 */
public class InitializationHelper {

    private NeverminedConfig neverminedConfig;

    /**
     * Constructor
     *
     * @param neverminedConfig object with the configuration
     */
    public InitializationHelper(NeverminedConfig neverminedConfig) {
        this.neverminedConfig = neverminedConfig;
    }

    /**
     * Initialize an instance of KeeperService
     *
     * @return an initialized KeeperService object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public KeeperService getKeeper() throws IOException, CipherException {

        KeeperService keeper = KeeperService.getInstance(
                neverminedConfig.getKeeperUrl(),
                neverminedConfig.getMainAccountAddress(),
                neverminedConfig.getMainAccountPassword(),
                neverminedConfig.getMainAccountCredentialsFile(),
                neverminedConfig.getKeeperTxAttempts(),
                neverminedConfig.getKeeperTxSleepDuration()
        );

        keeper.setGasLimit(neverminedConfig.getKeeperGasLimit())
                .setGasPrice(neverminedConfig.getKeeperGasPrice());

        return keeper;
    }

    /**
     * Initialize an instance of MetadataApiService
     *
     * @return an initialized MetadataApiService object
     */
    public MetadataApiService getMetadataService() {
        return MetadataApiService.getInstance(neverminedConfig.getMetadataUrl());
    }

    /**
     * Initialize an instance of SecretStoreDto
     *
     * @return an initializedSecretStoreDto object
     */
    public SecretStoreDto getSecretStoreDto() {
        return SecretStoreDto.builder(neverminedConfig.getSecretStoreUrl());
    }

    /**
     * Initialize an instance of EvmDto
     *
     * @return an initialized EvmDto object
     */
    public EvmDto getEvmDto() {
        return EvmDto.builder(
                neverminedConfig.getKeeperUrl(),
                neverminedConfig.getMainAccountAddress(),
                neverminedConfig.getMainAccountPassword()
        );
    }

    /**
     * Initialize an instance of SecretStoreManager
     *
     * @param secretStoreDto the DTO to connect with secret store
     * @param evmDto         DTO with the EVM
     * @return an initialized SecretStoreManager object
     */
    public SecretStoreManager getSecretStoreManager(SecretStoreDto secretStoreDto, EvmDto evmDto) {
        return SecretStoreManager.getInstance(secretStoreDto, evmDto);
    }

    /**
     * Initialize an instance of NeverminedManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized NeverminedManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public NeverminedManager getNeverminedManager(KeeperService keeperService, MetadataApiService metadataApiService) throws IOException, CipherException {
        return NeverminedManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of AccountsManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized AccountsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AccountsManager getAccountsManager(KeeperService keeperService, MetadataApiService metadataApiService) throws IOException, CipherException {
        return AccountsManager.getInstance(keeperService, metadataApiService)
                .setFaucetUrl(neverminedConfig.getFaucetUrl());
    }

    /**
     * Initialize an instance of AgreementsManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized AgreementsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AgreementsManager getAgreementsManager(KeeperService keeperService, MetadataApiService metadataApiService) throws IOException, CipherException {
        return AgreementsManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of ConditionsManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized ConditionsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public ConditionsManager getConditionsManager(KeeperService keeperService, MetadataApiService metadataApiService) throws IOException, CipherException {
        return ConditionsManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of TemplatesManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized TemplatesManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public TemplatesManager getTemplatesManager(KeeperService keeperService, MetadataApiService metadataApiService) throws IOException, CipherException {
        return TemplatesManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of AssetsManager
     *
     * @param keeperService   the KeeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized AssetsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AssetsManager getAssetsManager(KeeperService keeperService, MetadataApiService metadataApiService) throws IOException, CipherException {
        return AssetsManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of ProvenanceManager
     *
     * @param keeperService   the KeeperService
     * @return an initialized ProvenanceManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public ProvenanceManager getProvenanceManager(KeeperService keeperService) {
        return ProvenanceManager.getInstance(keeperService);
    }

    /**
     * Loads the NeverminedToken contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of NeverminedToken contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public NeverminedToken loadNeverminedTokenContract(KeeperService keeper) throws IOException, CipherException {

        return NeverminedToken.load(
                neverminedConfig.getTokenAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the TemplateStoreManager contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of TemplateStoreManager contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public TemplateStoreManager loadTemplateStoreManagerContract(KeeperService keeper) throws IOException, CipherException {
        return TemplateStoreManager.load(
                neverminedConfig.getTemplateStoreManagerAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }


    /**
     * Loads the Dispenser contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of Dispenser contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public Dispenser loadDispenserContract(KeeperService keeper) throws IOException, CipherException {
        return Dispenser.load(
                neverminedConfig.getDispenserAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


    /**
     * Loads the DIDRegistry contract from Keeper
     *
     * @param keeper the keeper service
     * @return an instance of DIDRegistry contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public DIDRegistry loadDIDRegistryContract(KeeperService keeper) throws IOException, CipherException {

        return DIDRegistry.load(
                neverminedConfig.getDidRegistryAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the EscrowAccessSecretStoreTemplate contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of EscrowAccessSecretStoreTemplate contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public EscrowAccessSecretStoreTemplate loadEscrowAccessSecretStoreTemplate(KeeperService keeper) throws IOException, CipherException {
        return EscrowAccessSecretStoreTemplate.load(
                neverminedConfig.getEscrowAccessSecretStoreTemplateAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the LockRewardCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of LockRewardCondition contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public LockRewardCondition loadLockRewardCondition(KeeperService keeper) throws IOException, CipherException {
        return LockRewardCondition.load(
                neverminedConfig.getLockrewardConditionsAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the EscrowReward contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of EscrowReward contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public EscrowReward loadEscrowReward(KeeperService keeper) throws IOException, CipherException {
        return EscrowReward.load(
                neverminedConfig.getEscrowRewardConditionsAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the AgreementStoreManager contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of AgreementStoreManager contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AgreementStoreManager loadAgreementStoreManager(KeeperService keeper) throws IOException, CipherException {
        return AgreementStoreManager.load(
                neverminedConfig.getAgreementStoreManagerAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


    /**
     * Loads the AgreementStoreManager contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of AgreementStoreManager contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public ConditionStoreManager loadConditionStoreManager(KeeperService keeper) throws IOException, CipherException {
        return ConditionStoreManager.load(
                neverminedConfig.getConditionStoreManagerAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the AccessSecretStoreCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of AccessSecretStoreCondition contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AccessSecretStoreCondition loadAccessSecretStoreCondition(KeeperService keeper) throws IOException, CipherException {
        return AccessSecretStoreCondition.load(
                neverminedConfig.getAccessSsConditionsAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the EscrowComputeExecutionTemplate contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of EscrowComputeExecutionTemplate contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public EscrowComputeExecutionTemplate loadEscrowComputeExecutionTemplate(KeeperService keeper) throws IOException, CipherException {
        return EscrowComputeExecutionTemplate.load(
                neverminedConfig.getEscrowComputeExecutionTemplateAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the ComputeExecutionCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of ComputeExecutionCondition contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public ComputeExecutionCondition loadComputeExecutionCondition(KeeperService keeper) throws IOException, CipherException {
        return ComputeExecutionCondition.load(
                neverminedConfig.getComputeExecutionConditionAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


}
