/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.common.web3.parity.JsonRpcSquidAdmin;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.service.ProviderConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OceanManagerIT {

    private static final Logger log = LogManager.getLogger(OceanManagerIT.class);

    private static final String OEP7_DATASET_EXAMPLE_URL = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/8/v0.4/ddo-example-access.json";

    private static final String DDO_JSON_SAMPLE = "src/test/resources/examples/ddo-example.json";
    private static String DDO_JSON_CONTENT;
    private static final String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;

    private static DDO ddoBase;
    private static AssetMetadata metadataBase;

    private static OceanManager managerPublisher;
    private static OceanManager managerConsumer;

    private static KeeperService keeperPublisher;
    private static KeeperService keeperConsumer;

    private static AquariusService aquarius;
    private static SecretStoreManager secretStore;
    private static String providerAddress;

    private static DIDRegistry didRegistry;
    private static EscrowReward escrowReward;
    private static AccessSecretStoreCondition accessSecretStoreCondition;
    private static LockRewardCondition lockRewardCondition;
    private static EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate;


    private static final Config config = ConfigFactory.load();

    private static final String DID_REGISTRY_CONTRACT;
    static {
        DID_REGISTRY_CONTRACT = config.getString("contract.DIDRegistry.address");
    }

    private static final String ESCROW_REWARD_CONTRACT;
    static {
        ESCROW_REWARD_CONTRACT = config.getString("contract.EscrowReward.address");
    }

    private static final String LOCK_REWARD_CONTRACT;
    static {
        LOCK_REWARD_CONTRACT = config.getString("contract.LockRewardCondition.address");
    }


    private static final String ACCESS_SS_CONDITION_CONTRACT;
    static {
        ACCESS_SS_CONDITION_CONTRACT = config.getString("contract.AccessSecretStoreCondition.address");
    }

    private static final String ESCROW_ACCESS_CONTRACT;
    static {
        ESCROW_ACCESS_CONTRACT = config.getString("contract.EscrowAccessSecretStoreTemplate.address");
    }


    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        Account publisherAccount = new Account(config.getString("account.parity.address"), config.getString("account.parity.password"));
        Account consumerAccount = new Account(config.getString("account.parity.address2"), config.getString("account.parity.password2"));

        keeperPublisher = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        keeperConsumer = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "2");

        aquarius= ManagerHelper.getAquarius(config);
        EvmDto evmDto = ManagerHelper.getEvmDto(config, ManagerHelper.VmClient.parity);
        secretStore= ManagerHelper.getSecretStoreController(config, evmDto);

        providerAddress= config.getString("provider.address");

        didRegistry= ManagerHelper.loadDIDRegistryContract(keeperPublisher, DID_REGISTRY_CONTRACT);
        escrowReward= ManagerHelper.loadEscrowRewardContract(keeperPublisher, ESCROW_REWARD_CONTRACT);
        accessSecretStoreCondition= ManagerHelper.loadAccessSecretStoreConditionContract(keeperPublisher, ACCESS_SS_CONDITION_CONTRACT);
        lockRewardCondition= ManagerHelper.loadLockRewardCondition(keeperPublisher, LOCK_REWARD_CONTRACT);
        escrowAccessSecretStoreTemplate= ManagerHelper.loadEscrowAccessSecretStoreTemplate(keeperPublisher, ESCROW_ACCESS_CONTRACT);


        // Initializing the OceanManager for the Publisher
        managerPublisher = OceanManager.getInstance(keeperPublisher, aquarius);
        managerPublisher.setSecretStoreManager(secretStore)
                .setDidRegistryContract(didRegistry)
                .setEscrowReward(escrowReward)
                .setAccessSecretStoreCondition(accessSecretStoreCondition)
                .setLockRewardCondition(lockRewardCondition)
                .setEscrowAccessSecretStoreTemplate(escrowAccessSecretStoreTemplate)
                .setMainAccount(publisherAccount)
                .setEvmDto(evmDto);

        // Initializing the OceanManager for the Consumer
        managerConsumer = OceanManager.getInstance(keeperConsumer, aquarius);
        managerConsumer.setSecretStoreManager(secretStore)
                .setDidRegistryContract(didRegistry)
                .setEscrowReward(escrowReward)
                .setAccessSecretStoreCondition(accessSecretStoreCondition)
                .setLockRewardCondition(lockRewardCondition)
                .setEscrowAccessSecretStoreTemplate(escrowAccessSecretStoreTemplate)
                .setMainAccount(consumerAccount)
                .setEvmDto(evmDto);

        // Pre-parsing of json's and models
        DDO_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_SAMPLE)));
        ddoBase = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {}, METADATA_JSON_CONTENT);

    }

    @Test
    public void getInstance() {
        // Checking if web3j driver included in KeeperService implements the Web3j interface
        assertTrue(
                managerPublisher.getKeeperService().getWeb3().getClass().isAssignableFrom(JsonRpcSquidAdmin.class));
        assertTrue(
                managerPublisher.getAquariusService().getClass().isAssignableFrom(AquariusService.class));
    }


    private DDO newRegisteredAsset() throws Exception {

        String OEP7_DATASET_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP7_DATASET_EXAMPLE_URL), "utf-8");

        DDO completeDDO = DDO.fromJSON(new TypeReference<DDO>() {
        }, OEP7_DATASET_EXAMPLE_CONTENT);

        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {}, completeDDO.services.get(0).toJson());

        String metadataUrl= config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl= config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl= config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint= config.getString("secretstore.url");
        String providerAddress= config.getString("provider.address");

        ProviderConfig providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        return managerPublisher.registerAccessServiceAsset(metadataBase,
                providerConfig,
                0);

    }

    @Test
    public void registerAsset() throws Exception {

        String metadataUrl= config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl= config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl= config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint= config.getString("secretstore.url");
        String providerAddress= config.getString("provider.address");


        ProviderConfig providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        DDO ddo= managerPublisher.registerAccessServiceAsset(metadataBase,
                providerConfig,
                0);

        DID did= ddo.getDid();
        DDO resolvedDDO= managerPublisher.resolveDID(did);

        assertEquals(ddo.id, resolvedDDO.id);
        assertEquals(metadataUrl.replace("{did}", did.toString()), resolvedDDO.services.get(0).serviceEndpoint);
        assertTrue( resolvedDDO.services.size() == 4);

    }

    @Test
    public void resolveDID() throws Exception {

        DID did= DID.builder();
        String oldUrl= "http://mymetadata.io/api";
        String newUrl= config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";

        String checksum = "0xd190bc85ee50643baffe7afe84ec6a9dd5212b67223523cd8e4d88f9069255fb";

        ddoBase.id = did.toString();

        ddoBase.services.get(0).serviceEndpoint = newUrl;
        aquarius.createDDO(ddoBase);

        boolean didRegistered= managerPublisher.registerDID(did, oldUrl, checksum, Arrays.asList(providerAddress));
        assertTrue(didRegistered);

        log.debug("Registering " + did.toString());
        managerPublisher.registerDID(did, newUrl, checksum, Arrays.asList(providerAddress));

        DDO ddo= managerPublisher.resolveDID(did);
        assertEquals(did.getDid(), ddo.id);
        assertEquals(newUrl, ddo.services.get(0).serviceEndpoint);
    }

    @Test(expected = DDOException.class)
    public void resolveDIDException() throws Exception {
        DID did= DID.builder();
        String url= "http://badhostname.inet:5000/api/v1/aquarius/assets/ddo/{did}";
        String checksum = "0xd190bc85ee50643baffe7afe84ec6a9dd5212b67223523cd8e4d88f9069255fb";

        ddoBase.id = did.toString();

        ddoBase.services.get(0).serviceEndpoint = url;
        aquarius.createDDO(ddoBase);

        boolean didRegistered= managerPublisher.registerDID(did, url, checksum, Arrays.asList(providerAddress));
        assertTrue(didRegistered);

        managerPublisher.resolveDID(did);

    }

    @Test
    public void generateDID() throws Exception {
        DID did= managerPublisher.generateDID(newRegisteredAsset());
        assertEquals(64, did.getHash().length());
    }

}