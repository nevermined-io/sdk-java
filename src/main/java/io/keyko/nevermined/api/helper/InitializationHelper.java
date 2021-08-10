package io.keyko.nevermined.api.helper;

import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.contracts.*;
import io.keyko.nevermined.core.conditions.LockPaymentConditionPayable;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.manager.*;
import io.keyko.secretstore.core.EvmDto;
import io.keyko.secretstore.core.SecretStoreDto;
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
     */
    public NeverminedManager getNeverminedManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        return NeverminedManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of AccountsManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized AccountsManager object
     */
    public AccountsManager getAccountsManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        return AccountsManager.getInstance(keeperService, metadataApiService)
                .setFaucetUrl(neverminedConfig.getFaucetUrl());
    }

    /**
     * Initialize an instance of AgreementsManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized AgreementsManager object
     */
    public AgreementsManager getAgreementsManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        return AgreementsManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of ConditionsManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized ConditionsManager object
     */
    public ConditionsManager getConditionsManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        return ConditionsManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of TemplatesManager
     *
     * @param keeperService   the keeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized TemplatesManager object
     */
    public TemplatesManager getTemplatesManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        return TemplatesManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of AssetsManager
     *
     * @param keeperService   the KeeperService
     * @param metadataApiService the MetadataApiService
     * @return an initialized AssetsManager object
     */
    public AssetsManager getAssetsManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        return AssetsManager.getInstance(keeperService, metadataApiService);
    }

    /**
     * Initialize an instance of ProvenanceManager
     *
     * @param keeperService   the KeeperService
     * @return an initialized ProvenanceManager object
     */
    public ProvenanceManager getProvenanceManager(KeeperService keeperService) {
        return ProvenanceManager.getInstance(keeperService);
    }

    /**
     * Loads the NeverminedToken contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of NeverminedToken contract deployed in keeper
     */
    public NeverminedToken loadNeverminedTokenContract(KeeperService keeper) {

        return NeverminedToken.load(
                neverminedConfig.getTokenAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the ERC20Upgradeable contract given a token address
     *
     * @param keeper the keeper Service
     * @param tokenAddress the ERC20 contract address
     * @return an instance of ERC20Upgradeable contract
     */
    public static ERC20Upgradeable loadERC20Contract(KeeperService keeper, String tokenAddress) {

        return ERC20Upgradeable.load(
                tokenAddress,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }


    /**
     * Loads the TemplateStoreManager contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of TemplateStoreManager contract deployed in keeper
     */
    public TemplateStoreManager loadTemplateStoreManagerContract(KeeperService keeper) {
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
     */
    public Dispenser loadDispenserContract(KeeperService keeper) {
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
     */
    public DIDRegistry loadDIDRegistryContract(KeeperService keeper) {

        return DIDRegistry.load(
                neverminedConfig.getDidRegistryAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the AccessTemplate contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of AccessTemplate contract deployed in keeper
     */
    public AccessTemplate loadAccessTemplate(KeeperService keeper) {
        return AccessTemplate.load(
                neverminedConfig.getAccessTemplateAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the NFTSalesTemplate contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of NFTSalesTemplate contract deployed in keeper
     */
    public NFTSalesTemplate loadNFTSalesTemplate(KeeperService keeper) {
        return NFTSalesTemplate.load(
                neverminedConfig.getNFTSalesTemplateAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the NFTAccessTemplate contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of NFTAccessTemplate contract deployed in keeper
     */
    public NFTAccessTemplate loadNFTAccessTemplate(KeeperService keeper) {
        return NFTAccessTemplate.load(
                neverminedConfig.getNFTAccessTemplateAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the DIDSalesTemplate contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of DIDSalesTemplate contract deployed in keeper
     */
    public DIDSalesTemplate loadDIDSalesTemplate(KeeperService keeper) {
        return DIDSalesTemplate.load(
                neverminedConfig.getDIDSalesTemplateAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    /**
     * Loads the LockPaymentCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of LockPaymentCondition contract deployed in keeper
     */
    public LockPaymentConditionPayable loadLockPaymentCondition(KeeperService keeper) {
        return LockPaymentConditionPayable.load(
                neverminedConfig.getLockPaymentConditionsAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the EscrowPaymentCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of EscrowPaymentCondition contract deployed in keeper
     */
    public EscrowPaymentCondition loadEscrowPaymentCondition(KeeperService keeper) {
        return EscrowPaymentCondition.load(
                neverminedConfig.getEscrowPaymentConditionAddress(),
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
     */
    public AgreementStoreManager loadAgreementStoreManager(KeeperService keeper)  {
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
     */
    public ConditionStoreManager loadConditionStoreManager(KeeperService keeper) {
        return ConditionStoreManager.load(
                neverminedConfig.getConditionStoreManagerAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the AccessCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of AccessCondition contract deployed in keeper
     */
    public AccessCondition loadAccessCondition(KeeperService keeper) {
        return AccessCondition.load(
                neverminedConfig.getAccessConditionsAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the TransferNFTCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of TransferNFTCondition contract deployed in keeper
     */
    public TransferNFTCondition loadTransferNFTCondition(KeeperService keeper) {
        return TransferNFTCondition.load(
                neverminedConfig.getTransferNFTConditionAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the TransferDIDOwnershipCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of TransferDIDOwnershipCondition contract deployed in keeper
     */
    public TransferDIDOwnershipCondition loadTransferDIDCondition(KeeperService keeper) {
        return TransferDIDOwnershipCondition.load(
                neverminedConfig.getTransferDIDConditionAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the NFTAccessCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of NFTAccessCondition contract deployed in keeper
     */
    public NFTAccessCondition loadNFTAccessCondition(KeeperService keeper) {
        return NFTAccessCondition.load(
                neverminedConfig.getNFTAccessConditionAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    /**
     * Loads the NFTHolderCondition contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of NFTHolderCondition contract deployed in keeper
     */
    public NFTHolderCondition loadNFTHolderCondition(KeeperService keeper) {
        return NFTHolderCondition.load(
                neverminedConfig.getNFTHolderConditionAddress(),
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
     */
    public EscrowComputeExecutionTemplate loadEscrowComputeExecutionTemplate(KeeperService keeper) {
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
     */
    public ComputeExecutionCondition loadComputeExecutionCondition(KeeperService keeper) {
        return ComputeExecutionCondition.load(
                neverminedConfig.getComputeExecutionConditionAddress(),
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


}
