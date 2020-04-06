/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.keeper.contracts.EscrowAccessSecretStoreTemplate;
import com.oceanprotocol.keeper.contracts.TemplateStoreManager;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.squid.manager.ManagerHelper;
import com.oceanprotocol.squid.models.Balance;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.asset.OrderResult;
import com.oceanprotocol.squid.models.service.types.ComputingService;
import com.oceanprotocol.squid.models.service.ProviderConfig;
import com.oceanprotocol.squid.models.service.Service;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AssetsApiIT {

    private static final Logger log = LogManager.getLogger(AssetsApiIT.class);

    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;

    private static String METADATA_ALG_JSON_SAMPLE = "src/test/resources/examples/metadata-algorithm.json";
    private static String METADATA_ALG_JSON_CONTENT;
    private static AssetMetadata metadataBaseAlgorithm;

    private static String COMPUTING_PROVIDER_JSON_SAMPLE = "src/test/resources/examples/computing-provider-example.json";
    private static String COMPUTING_PROVIDER_JSON_CONTENT;
    private static ComputingService.Provider computingProvider;
    private static ProviderConfig providerConfig;
    private static OceanAPI oceanAPI;
    private static OceanAPI oceanAPIConsumer;

    private static KeeperService keeper;

    private static Config config;

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);

        METADATA_ALG_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_ALG_JSON_SAMPLE)));
        metadataBaseAlgorithm = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_ALG_JSON_CONTENT);

        COMPUTING_PROVIDER_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(COMPUTING_PROVIDER_JSON_SAMPLE)));
        computingProvider = DDO.fromJSON(new TypeReference<ComputingService.Provider>() {
        },  COMPUTING_PROVIDER_JSON_CONTENT);

        String metadataUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl = config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getMainAccount());

        Properties properties = new Properties();
        properties.put(OceanConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(OceanConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(OceanConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(OceanConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(OceanConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(OceanConfig.AQUARIUS_URL, config.getString("aquarius.url"));
        properties.put(OceanConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(OceanConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(OceanConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address2"));
        properties.put(OceanConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password2"));
        properties.put(OceanConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.file2"));
        properties.put(OceanConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(OceanConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(OceanConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(OceanConfig.LOCKREWARD_CONDITIONS_ADDRESS, config.getString("contract.LockRewardCondition.address"));
        properties.put(OceanConfig.ESCROWREWARD_CONDITIONS_ADDRESS, config.getString("contract.EscrowReward.address"));
        properties.put(OceanConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        properties.put(OceanConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(OceanConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(OceanConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(OceanConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(OceanConfig.PROVIDER_ADDRESS, config.getString("provider.address"));

        properties.put(OceanConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(OceanConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));

        oceanAPIConsumer = OceanAPI.getInstance(properties);

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate = ManagerHelper.loadEscrowAccessSecretStoreTemplate(keeper, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        TemplateStoreManager templateManager = ManagerHelper.loadTemplateStoreManager(keeper, config.getString("contract.TemplateStoreManager.address"));

        oceanAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount());

        log.debug("Account " + oceanAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        boolean isTemplateApproved = templateManager.isTemplateApproved(escrowAccessSecretStoreTemplate.getContractAddress()).send();
        log.debug("Is escrowAccessSecretStoreTemplate approved? " + isTemplateApproved);
    }

    @Test
    public void create() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = oceanAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

    }

    @Test
    public void createComputingService() throws Exception {

        metadataBaseAlgorithm.attributes.main.dateCreated = new Date();
        DDO ddo = oceanAPI.getAssetsAPI().createComputingService(metadataBaseAlgorithm, providerConfig, computingProvider);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = oceanAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

    }

    @Test
    public void orderComputingService() throws Exception {

        metadataBaseAlgorithm.attributes.main.dateCreated = new Date();
        String computeServiceEndpoint =   config.getString("brizo.url") + "/api/v1/brizo/services/exec";
        providerConfig.setAccessEndpoint(computeServiceEndpoint);
        DDO ddo = oceanAPI.getAssetsAPI().createComputingService(metadataBaseAlgorithm, providerConfig, computingProvider);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = oceanAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

        Flowable<OrderResult> response = oceanAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_COMPUTING_INDEX);
        TimeUnit.SECONDS.sleep(2l);

        OrderResult result = response.blockingFirst();
        assertNotNull(result.getServiceAgreementId());
        assertEquals(true, result.isAccessGranted());

    }



    @Test
    public void order() throws Exception {


        log.info("PROVIDER ADDRESS: " + config.getString("provider.address"));

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        oceanAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Balance balance = oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount());

        log.debug("Account " + oceanAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        Flowable<OrderResult> response = oceanAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        //Balance balanceAfter= oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount());

        //log.debug("Account " + oceanAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        TimeUnit.SECONDS.sleep(2l);

        OrderResult result = response.blockingFirst();
        assertNotNull(result.getServiceAgreementId());
        assertEquals(true, result.isAccessGranted());

    }

    @Test
    public void search() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        String searchText = "Weather";

        List<DDO> results = oceanAPI.getAssetsAPI().search(searchText).getResults();
        assertNotNull(results);

    }

    @Test
    public void query() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        Map<String, Object> params = new HashMap<>();
        params.put("license", "CC-BY");

        List<DDO> results = oceanAPI.getAssetsAPI().query(params).getResults();
        assertNotNull(results);

    }


    @Test
    public void consumeBinary() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        oceanAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Balance balance = oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount());
        log.debug("Account " + oceanAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        Flowable<OrderResult> response = oceanAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);
        OrderResult orderResult = response.blockingFirst();
        assertNotNull(orderResult.getServiceAgreementId());
        assertEquals(true, orderResult.isAccessGranted());
        log.debug("Granted Access Received for the service Agreement " + orderResult.getServiceAgreementId());

        InputStream result = oceanAPIConsumer.getAssetsAPI().consumeBinary(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX,
                0);

        assertNotNull(result);

    }


    @Test
    public void owner() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        String owner = oceanAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(owner, oceanAPI.getMainAccount().address);
    }

    @Test(expected = DDOException.class)
    public void retire() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");
        assertTrue(oceanAPI.getAssetsAPI().retire(ddo.getDid()));
        oceanAPI.getAssetsAPI().resolve(ddo.getDid());
    }

    @Test
    public void ownerAssets() throws Exception {
        int assetsOwnedBefore = (oceanAPI.getAssetsAPI().ownerAssets(oceanAPI.getMainAccount().address)).size();

        metadataBase.attributes.main.dateCreated = new Date();
        oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        int assetsOwnedAfter = oceanAPI.getAssetsAPI().ownerAssets(oceanAPI.getMainAccount().address).size();
        assertEquals(assetsOwnedAfter, assetsOwnedBefore + 1);
    }

    @Test
    public void consumeAndConsumerAssets() throws Exception{
        int consumedAssetsBefore = oceanAPI.getAssetsAPI().consumerAssets(oceanAPIConsumer.getMainAccount().address).size();

        providerConfig.setSecretStoreEndpoint(config.getString("secretstore.url"));
        String basePath = config.getString("consume.basePath");
        AssetMetadata metadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);
        metadata.attributes.main.dateCreated = new Date();
        DDO ddo = oceanAPI.getAssetsAPI().create(metadata, providerConfig);
        DID did = new DID(ddo.id);

        log.debug("DDO registered!");
        oceanAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Flowable<OrderResult> response = oceanAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        TimeUnit.SECONDS.sleep(2l);

        OrderResult orderResult = response.blockingFirst();
        assertNotNull(orderResult.getServiceAgreementId());
        assertEquals(true, orderResult.isAccessGranted());
        log.debug("Granted Access Received for the service Agreement " + orderResult.getServiceAgreementId());

        boolean result = oceanAPIConsumer.getAssetsAPI().consume(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX, basePath);
        assertTrue(result);


        int consumedAssetsAfter = oceanAPI.getAssetsAPI().consumerAssets(oceanAPIConsumer.getMainAccount().address).size();
        assertEquals(consumedAssetsBefore + 1, consumedAssetsAfter);

    }

//    @Test
//    public void validate() throws Exception {
//        AssetMetadata metadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {
//        }, METADATA_JSON_CONTENT);
//        assertTrue(oceanAPI.getAssetsAPI().validate(metadata));
//    }
}
