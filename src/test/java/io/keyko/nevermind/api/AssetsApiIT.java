package io.keyko.nevermind.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermind.api.config.NevermindConfig;
import io.keyko.nevermind.contracts.EscrowAccessSecretStoreTemplate;
import io.keyko.nevermind.contracts.TemplateStoreManager;
import io.keyko.nevermind.exceptions.DDOException;
import io.keyko.nevermind.manager.ManagerHelper;
import io.keyko.nevermind.models.Balance;
import io.keyko.nevermind.models.DDO;
import io.keyko.nevermind.models.DID;
import io.keyko.nevermind.models.asset.AssetMetadata;
import io.keyko.nevermind.models.asset.OrderResult;
import io.keyko.nevermind.models.service.ProviderConfig;
import io.keyko.nevermind.models.service.Service;
import io.keyko.nevermind.models.service.types.ComputingService;
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
    private static NevermindAPI nevermindAPI;
    private static NevermindAPI nevermindAPIConsumer;

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

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String consumeUrl = config.getString("gateway.url") + "/api/v1/gateway/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        nevermindAPI = NevermindAPI.getInstance(config);

        assertNotNull(nevermindAPI.getAssetsAPI());
        assertNotNull(nevermindAPI.getMainAccount());

        Properties properties = new Properties();
        properties.put(NevermindConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(NevermindConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(NevermindConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(NevermindConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(NevermindConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(NevermindConfig.METADATA_URL, config.getString("metadata.url"));
        properties.put(NevermindConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(NevermindConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(NevermindConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address2"));
        properties.put(NevermindConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password2"));
        properties.put(NevermindConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.file2"));
        properties.put(NevermindConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(NevermindConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(NevermindConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(NevermindConfig.LOCKREWARD_CONDITIONS_ADDRESS, config.getString("contract.LockRewardCondition.address"));
        properties.put(NevermindConfig.ESCROWREWARD_CONDITIONS_ADDRESS, config.getString("contract.EscrowReward.address"));
        properties.put(NevermindConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        properties.put(NevermindConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(NevermindConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(NevermindConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(NevermindConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(NevermindConfig.PROVIDER_ADDRESS, config.getString("provider.address"));

        properties.put(NevermindConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(NevermindConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));

        nevermindAPIConsumer = NevermindAPI.getInstance(properties);

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate = ManagerHelper.loadEscrowAccessSecretStoreTemplate(keeper, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        TemplateStoreManager templateManager = ManagerHelper.loadTemplateStoreManager(keeper, config.getString("contract.TemplateStoreManager.address"));

        nevermindAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = nevermindAPIConsumer.getAccountsAPI().balance(nevermindAPIConsumer.getMainAccount());

        log.debug("Account " + nevermindAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        boolean isTemplateApproved = templateManager.isTemplateApproved(escrowAccessSecretStoreTemplate.getContractAddress()).send();
        log.debug("Is escrowAccessSecretStoreTemplate approved? " + isTemplateApproved);
    }

    @Test
    public void create() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = nevermindAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

    }

    @Test
    public void createComputingService() throws Exception {

        metadataBaseAlgorithm.attributes.main.dateCreated = new Date();
        DDO ddo = nevermindAPI.getAssetsAPI().createComputingService(metadataBaseAlgorithm, providerConfig, computingProvider);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = nevermindAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

    }

    @Test
    public void orderComputingService() throws Exception {

        metadataBaseAlgorithm.attributes.main.dateCreated = new Date();
        String computeServiceEndpoint =   config.getString("gateway.url") + "/api/v1/gateway/services/exec";
        providerConfig.setAccessEndpoint(computeServiceEndpoint);
        DDO ddo = nevermindAPI.getAssetsAPI().createComputingService(metadataBaseAlgorithm, providerConfig, computingProvider);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = nevermindAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

        Flowable<OrderResult> response = nevermindAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_COMPUTING_INDEX);
        TimeUnit.SECONDS.sleep(2l);

        OrderResult result = response.blockingFirst();
        assertNotNull(result.getServiceAgreementId());
        assertEquals(true, result.isAccessGranted());

    }



    @Test
    public void order() throws Exception {

        log.info("PROVIDER ADDRESS: " + config.getString("provider.address"));

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        nevermindAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Balance balance = nevermindAPIConsumer.getAccountsAPI().balance(nevermindAPIConsumer.getMainAccount());

        log.debug("Account " + nevermindAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        Flowable<OrderResult> response = nevermindAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        //Balance balanceAfter= nevermindAPIConsumer.getAccountsAPI().balance(nevermindAPIConsumer.getMainAccount());

        //log.debug("Account " + nevermindAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        TimeUnit.SECONDS.sleep(2l);

        OrderResult result = response.blockingFirst();
        assertNotNull(result.getServiceAgreementId());
        assertEquals(true, result.isAccessGranted());

    }

    @Test
    public void search() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        String searchText = "Weather";

        List<DDO> results = nevermindAPI.getAssetsAPI().search(searchText).getResults();
        assertNotNull(results);

    }

    @Test
    public void query() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        Map<String, Object> params = new HashMap<>();
        params.put("license", "CC-BY");

        List<DDO> results = nevermindAPI.getAssetsAPI().query(params).getResults();
        assertNotNull(results);

    }


    @Test
    public void consumeBinary() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        nevermindAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Balance balance = nevermindAPIConsumer.getAccountsAPI().balance(nevermindAPIConsumer.getMainAccount());
        log.debug("Account " + nevermindAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        Flowable<OrderResult> response = nevermindAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);
        OrderResult orderResult = response.blockingFirst();
        assertNotNull(orderResult.getServiceAgreementId());
        assertEquals(true, orderResult.isAccessGranted());
        log.debug("Granted Access Received for the service Agreement " + orderResult.getServiceAgreementId());

        InputStream result = nevermindAPIConsumer.getAssetsAPI().consumeBinary(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX,
                0);

        assertNotNull(result);

    }


    @Test
    public void owner() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        String owner = nevermindAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(owner, nevermindAPI.getMainAccount().address);
    }

    @Test(expected = DDOException.class)
    public void retire() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");
        assertTrue(nevermindAPI.getAssetsAPI().retire(ddo.getDid()));
        nevermindAPI.getAssetsAPI().resolve(ddo.getDid());
    }

    @Test
    public void ownerAssets() throws Exception {
        int assetsOwnedBefore = (nevermindAPI.getAssetsAPI().ownerAssets(nevermindAPI.getMainAccount().address)).size();

        metadataBase.attributes.main.dateCreated = new Date();
        nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        int assetsOwnedAfter = nevermindAPI.getAssetsAPI().ownerAssets(nevermindAPI.getMainAccount().address).size();
        assertEquals(assetsOwnedAfter, assetsOwnedBefore + 1);
    }

    @Test
    public void consumeAndConsumerAssets() throws Exception{
        int consumedAssetsBefore = nevermindAPI.getAssetsAPI().consumerAssets(nevermindAPIConsumer.getMainAccount().address).size();

        providerConfig.setSecretStoreEndpoint(config.getString("secretstore.url"));
        String basePath = config.getString("consume.basePath");
        AssetMetadata metadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);
        metadata.attributes.main.dateCreated = new Date();
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadata, providerConfig);
        DID did = new DID(ddo.id);

        log.debug("DDO registered!");
        nevermindAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Flowable<OrderResult> response = nevermindAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        TimeUnit.SECONDS.sleep(2l);

        OrderResult orderResult = response.blockingFirst();
        assertNotNull(orderResult.getServiceAgreementId());
        assertEquals(true, orderResult.isAccessGranted());
        log.debug("Granted Access Received for the service Agreement " + orderResult.getServiceAgreementId());

        boolean result = nevermindAPIConsumer.getAssetsAPI().consume(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX, basePath);
        assertTrue(result);


        int consumedAssetsAfter = nevermindAPI.getAssetsAPI().consumerAssets(nevermindAPIConsumer.getMainAccount().address).size();
        assertEquals(consumedAssetsBefore + 1, consumedAssetsAfter);

    }

//    @Test
//    public void validate() throws Exception {
//        AssetMetadata metadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {
//        }, METADATA_JSON_CONTENT);
//        assertTrue(nevermindAPI.getAssetsAPI().validate(metadata));
//    }
}
