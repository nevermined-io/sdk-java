/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api.helper;

import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.manager.*;
import org.web3j.crypto.CipherException;

import java.io.IOException;

/**
 * Helper to initialize all the managers, services and contracts needed for the API
 */
public class OceanInitializationHelper {

    private OceanConfig oceanConfig;

    /**
     * Constructor
     *
     * @param oceanConfig object with the configuration
     */
    public OceanInitializationHelper(OceanConfig oceanConfig) {
        this.oceanConfig = oceanConfig;
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
                oceanConfig.getKeeperUrl(),
                oceanConfig.getMainAccountAddress(),
                oceanConfig.getMainAccountPassword(),
                oceanConfig.getMainAccountCredentialsFile(),
                oceanConfig.getKeeperTxAttempts(),
                oceanConfig.getKeeperTxSleepDuration()
        );

        keeper.setGasLimit(oceanConfig.getKeeperGasLimit())
                .setGasPrice(oceanConfig.getKeeperGasPrice());

        return keeper;
    }

    /**
     * Initialize an instance of AquariusService
     *
     * @return an initialized AquariusService object
     */
    public AquariusService getAquarius() {
        return AquariusService.getInstance(oceanConfig.getAquariusUrl());
    }

    /**
     * Initialize an instance of SecretStoreDto
     *
     * @return an initializedSecretStoreDto object
     */
    public SecretStoreDto getSecretStoreDto() {
        return SecretStoreDto.builder(oceanConfig.getSecretStoreUrl());
    }

    /**
     * Initialize an instance of EvmDto
     *
     * @return an initialized EvmDto object
     */
    public EvmDto getEvmDto() {
        return EvmDto.builder(
                oceanConfig.getKeeperUrl(),
                oceanConfig.getMainAccountAddress(),
                oceanConfig.getMainAccountPassword()
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
     * Initialize an instance of OceanManager
     *
     * @param keeperService   the keeperService
     * @param aquariusService the aquariusService
     * @return an initialized OceanManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public OceanManager getOceanManager(KeeperService keeperService, AquariusService aquariusService) throws IOException, CipherException {
        return OceanManager.getInstance(keeperService, aquariusService);
    }

    /**
     * Initialize an instance of AccountsManager
     *
     * @param keeperService   the keeperService
     * @param aquariusService the AquariusService
     * @return an initialized AccountsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AccountsManager getAccountsManager(KeeperService keeperService, AquariusService aquariusService) throws IOException, CipherException {
        return AccountsManager.getInstance(keeperService, aquariusService);
    }

    /**
     * Initialize an instance of AgreementsManager
     *
     * @param keeperService   the keeperService
     * @param aquariusService the AquariusService
     * @return an initialized AgreementsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AgreementsManager getAgreementsManager(KeeperService keeperService, AquariusService aquariusService) throws IOException, CipherException {
        return AgreementsManager.getInstance(keeperService, aquariusService);
    }

    /**
     * Initialize an instance of ConditionsManager
     *
     * @param keeperService   the keeperService
     * @param aquariusService the AquariusService
     * @return an initialized ConditionsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public ConditionsManager getConditionsManager(KeeperService keeperService, AquariusService aquariusService) throws IOException, CipherException {
        return ConditionsManager.getInstance(keeperService, aquariusService);
    }

    /**
     * Initialize an instance of TemplatesManager
     *
     * @param keeperService   the keeperService
     * @param aquariusService the AquariusService
     * @return an initialized TemplatesManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public TemplatesManager getTemplatesManager(KeeperService keeperService, AquariusService aquariusService) throws IOException, CipherException {
        return TemplatesManager.getInstance(keeperService, aquariusService);
    }

    /**
     * Initialize an instance of AssetsManager
     *
     * @param keeperService   the KeeperService
     * @param aquariusService the AquariusService
     * @return an initialized AssetsManager object
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public AssetsManager getAssetsManager(KeeperService keeperService, AquariusService aquariusService) throws IOException, CipherException {
        return AssetsManager.getInstance(keeperService, aquariusService);
    }

    /**
     * Loads the OceanToken contract from Keeper
     *
     * @param keeper the keeper Service
     * @return an instance of OceanToken contract deployed in keeper
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public OceanToken loadOceanTokenContract(KeeperService keeper) throws IOException, CipherException {

        return OceanToken.load(
                oceanConfig.getTokenAddress(),
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
                oceanConfig.getTemplateStoreManagerAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getDispenserAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getDidRegistryAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getEscrowAccessSecretStoreTemplateAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getLockrewardConditionsAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getEscrowRewardConditionsAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getAgreementStoreManagerAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getConditionStoreManagerAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getAccessSsConditionsAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getEscrowComputeExecutionTemplateAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
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
                oceanConfig.getComputeExecutionConditionAddress(),
                keeper.getWeb3(),
//                keeper.getCredentials(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


}
