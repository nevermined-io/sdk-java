package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.contracts.EscrowAccessSecretStoreTemplate;
import io.keyko.nevermined.contracts.TemplateStoreManager;
import io.keyko.nevermined.exceptions.*;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.manager.ManagerHelper;
import io.keyko.nevermined.models.Balance;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.gateway.ComputeLogs;
import io.keyko.nevermined.models.gateway.ComputeStatus;
import io.keyko.nevermined.models.service.ProviderConfig;
import io.keyko.nevermined.models.service.Service;
import io.keyko.nevermined.models.service.types.ComputingService;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
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

    private static String METADATA_WORKFLOW_JSON_SAMPLE = "src/test/resources/examples/metadata-workflow.json";
    private static String METADATA_WORKFLOW_JSON_CONTENT;
    private static AssetMetadata metadataBaseWorkflow;

    private static String COMPUTING_PROVIDER_JSON_SAMPLE = "src/test/resources/examples/computing-provider-example.json";
    private static String COMPUTING_PROVIDER_JSON_CONTENT;
    private static ComputingService.Provider computingProvider;
    private static ProviderConfig providerConfig;
    private static NeverminedAPI neverminedAPI;
    private static NeverminedAPI neverminedAPIConsumer;

    private static KeeperService keeper;

    private static Config config;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);

        METADATA_ALG_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_ALG_JSON_SAMPLE)));
        metadataBaseAlgorithm = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_ALG_JSON_CONTENT);

        METADATA_WORKFLOW_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_WORKFLOW_JSON_SAMPLE)));
        metadataBaseWorkflow = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_WORKFLOW_JSON_CONTENT);

        COMPUTING_PROVIDER_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(COMPUTING_PROVIDER_JSON_SAMPLE)));
        computingProvider = DDO.fromJSON(new TypeReference<ComputingService.Provider>() {
        },  COMPUTING_PROVIDER_JSON_CONTENT);

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String gatewayUrl = config.getString("gateway.url");
        String consumeUrl = gatewayUrl + "/api/v1/gateway/services/access";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, gatewayUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        neverminedAPI = NeverminedAPI.getInstance(config);

        assertNotNull(neverminedAPI.getAssetsAPI());
        assertNotNull(neverminedAPI.getMainAccount());

        Properties properties = new Properties();
        properties.put(NeverminedConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(NeverminedConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(NeverminedConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(NeverminedConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(NeverminedConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(NeverminedConfig.METADATA_URL, config.getString("metadata.url"));
        properties.put(NeverminedConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(NeverminedConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address2"));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password2"));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.credentialsFile2"));
        properties.put(NeverminedConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(NeverminedConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(NeverminedConfig.LOCKREWARD_CONDITIONS_ADDRESS, config.getString("contract.LockRewardCondition.address"));
        properties.put(NeverminedConfig.ESCROWREWARD_CONDITIONS_ADDRESS, config.getString("contract.EscrowReward.address"));
        properties.put(NeverminedConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        properties.put(NeverminedConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(NeverminedConfig.TOKEN_ADDRESS, config.getString("contract.NeverminedToken.address"));
        properties.put(NeverminedConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(NeverminedConfig.PROVIDER_ADDRESS, config.getString("provider.address"));

        properties.put(NeverminedConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(NeverminedConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));

        neverminedAPIConsumer = NeverminedAPI.getInstance(properties);

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate = ManagerHelper.loadEscrowAccessSecretStoreTemplate(keeper, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        TemplateStoreManager templateManager = ManagerHelper.loadTemplateStoreManager(keeper, config.getString("contract.TemplateStoreManager.address"));

        neverminedAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());

        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        boolean isTemplateApproved = templateManager.isTemplateApproved(escrowAccessSecretStoreTemplate.getContractAddress()).send();
        log.debug("Is escrowAccessSecretStoreTemplate approved? " + isTemplateApproved);
    }

    @Test
    public void create() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = neverminedAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

    }

    @Test
    public void createComputeService() throws Exception {

        metadataBaseAlgorithm.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().createComputeService(metadataBaseAlgorithm, providerConfig, computingProvider);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = neverminedAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);

    }


    // This test only makes sense if is executed in combination with the events handler
    // Disabling by default because in the integration tests, the events handler is not executed
    @Ignore
    @Test
    public void order() throws Exception {

        log.info("PROVIDER ADDRESS: " + config.getString("provider.address"));

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        neverminedAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());

        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        Flowable<OrderResult> response = neverminedAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        //Balance balanceAfter= neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());

        //log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        TimeUnit.SECONDS.sleep(2l);

        OrderResult result = response.blockingFirst();
        assertNotNull(result.getServiceAgreementId());
        assertEquals(true, result.isAccessGranted());

    }

    @Test
    public void search() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        String searchText = "Weather";

        List<DDO> results = neverminedAPI.getAssetsAPI().search(searchText).getResults();
        assertNotNull(results);

    }

    @Test
    public void query() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        Map<String, Object> params = new HashMap<>();
        params.put("license", "CC-BY");

        List<DDO> results = neverminedAPI.getAssetsAPI().query(params).getResults();
        assertNotNull(results);

    }

    @Test
    public void consumeBinaryDirectly() throws Exception {

        int consumedAssetsBefore = neverminedAPI.getAssetsAPI().consumerAssets(
                neverminedAPIConsumer.getMainAccount().address).size();

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        neverminedAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());
        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        final long startTime = System.currentTimeMillis();
        OrderResult orderResult = neverminedAPIConsumer.getAssetsAPI().orderDirect(did, Service.DEFAULT_ACCESS_INDEX);
        final long orderTime = System.currentTimeMillis();

        assertTrue(orderResult.isAccessGranted());

        InputStream result = neverminedAPIConsumer.getAssetsAPI().consumeBinary(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX,
                0);

        final long endTime = System.currentTimeMillis();
        log.debug("Order method took " + (orderTime - startTime) + " milliseconds");
        log.debug("Full consumption took " + (endTime - startTime) + " milliseconds");

        assertNotNull(result);

        int consumedAssetsAfter = neverminedAPI.getAssetsAPI().consumerAssets(
                neverminedAPIConsumer.getMainAccount().address).size();
        assertEquals(consumedAssetsBefore + 1, consumedAssetsAfter);
    }

    @Test
    public void ownerDownload() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        final Boolean downloaded = neverminedAPI.getAssetsAPI().ownerDownload(did, Service.DEFAULT_ACCESS_INDEX,
                tempFolder.getRoot().getAbsolutePath());

        String expectedDestinationPath = tempFolder.getRoot().getAbsolutePath() + File.separator + "datafile."
                + did.getHash() + ".0" + File.separator + "shs_dataset_test.txt";
        assertTrue(downloaded);
        assertTrue(new File(expectedDestinationPath).exists());


        Boolean shouldntBeDownloaded = false;
        try {
            shouldntBeDownloaded = neverminedAPIConsumer.getAssetsAPI().ownerDownload(did, Service.DEFAULT_ACCESS_INDEX,
                    tempFolder.getRoot().getAbsolutePath());
        } catch (ServiceException | ConsumeServiceException e) {
        }
        assertFalse(shouldntBeDownloaded);
    }

    @Test
    public void orderAndExecuteComputeService() throws Exception {

        log.info("E2E Compute Scenario");

        // 0. Configure the scenario
        metadataBase.attributes.main.dateCreated = new Date();
        metadataBaseAlgorithm.attributes.main.dateCreated = new Date();
        AssetMetadata metadataWorkflow = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_WORKFLOW_JSON_CONTENT);
        metadataWorkflow.attributes.main.dateCreated = new Date();

        String computeServiceEndpoint = config.getString("gateway.url") + "/api/v1/gateway/services/execute";
        providerConfig.setExecuteEndpoint(computeServiceEndpoint);

        // 1. Publish the compute service

        DDO ddoComputeService = neverminedAPI.getAssetsAPI().createComputeService(metadataBase, providerConfig, computingProvider);
        DID didComputeService = new DID(ddoComputeService.id);
        DDO resolvedDDO = neverminedAPI.getAssetsAPI().resolve(didComputeService);
        assertEquals(ddoComputeService.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);
        log.info("Published Compute Service: " + didComputeService.did);

        // 2. Publish the algorithm
        DDO ddoAlgorithm = neverminedAPI.getAssetsAPI().create(metadataBaseAlgorithm, providerConfig);
        DID didAlgorithm = new DID(ddoAlgorithm.id);
        log.info("Published Algorithm: " + didAlgorithm.did);

        // 3. Publish the workflow
        metadataWorkflow.attributes.main.workflow.stages.get(0).input.get(0).id = didComputeService;
        metadataWorkflow.attributes.main.workflow.stages.get(0).transformation.id = didAlgorithm;
        DDO ddoWorkflow = neverminedAPI.getAssetsAPI().create(metadataWorkflow, providerConfig);
        DID didWorkflow = new DID(ddoWorkflow.id);
        log.info("Published Workflow: " + didWorkflow.did);

        // 4. Order the compute service
        final long startTime = System.currentTimeMillis();
        OrderResult orderResult = neverminedAPIConsumer.getAssetsAPI().orderDirect(didComputeService, Service.DEFAULT_COMPUTE_INDEX);
        final long orderTime = System.currentTimeMillis();

        assertTrue(orderResult.isAccessGranted());
        assertNotNull(orderResult.getServiceAgreementId());

        // 5. Execute
        GatewayService.ServiceExecutionResult executionResult = neverminedAPIConsumer.getAssetsAPI().execute(
                EthereumHelper.add0x(orderResult.getServiceAgreementId()),
                didComputeService,
                Service.DEFAULT_COMPUTE_INDEX,
                didWorkflow);

        final long endTime = System.currentTimeMillis();
        log.debug("Order method took " + (orderTime - startTime) + " milliseconds");
        log.debug("Execution Request took " + (endTime - startTime) + " milliseconds");
        assertNotNull(executionResult.getExecutionId());

        // 6. Get compute status
        ComputeStatus status = neverminedAPIConsumer.getAssetsAPI().getComputeStatus(
            EthereumHelper.add0x(orderResult.getServiceAgreementId()),
            executionResult.getExecutionId(),
            providerConfig);
        assertNotNull(status);

        // 7. Get compute logs
        List<ComputeLogs> logs = neverminedAPIConsumer.getAssetsAPI().getComputeLogs(
            EthereumHelper.add0x(orderResult.getServiceAgreementId()),
            executionResult.getExecutionId(),
            providerConfig);
        assertNotNull(logs);
    }


    // This test only makes sense if is executed in combination with the events handler
    // Disabling by default because in the integration tests, the events handler is not executed
    @Ignore
    @Test
    public void consumeBinary() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        DID did = new DID(ddo.id);

        neverminedAPIConsumer.getAccountsAPI().requestTokens(BigInteger.TEN);
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());
        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        final long startTime = System.currentTimeMillis();
        Flowable<OrderResult> response = neverminedAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);
        final long orderTime = System.currentTimeMillis();

        OrderResult orderResult = response.blockingFirst();
        assertNotNull(orderResult.getServiceAgreementId());
        assertEquals(true, orderResult.isAccessGranted());
        log.debug("Granted Access Received for the service Agreement " + orderResult.getServiceAgreementId());

        InputStream result = neverminedAPIConsumer.getAssetsAPI().consumeBinary(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX,
                0);

        final long endTime = System.currentTimeMillis();
        log.debug("Order method took " + (orderTime - startTime) + " milliseconds");
        log.debug("Full consumption took " + (endTime - startTime) + " milliseconds");

        assertNotNull(result);
    }


    @Test
    public void owner() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        String owner = neverminedAPI.getAssetsAPI().owner(ddo.getDid());
        Assert.assertEquals(owner, neverminedAPI.getMainAccount().address);
    }

    @Test(expected = DDOException.class)
    public void retire() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");
        assertTrue(neverminedAPI.getAssetsAPI().retire(ddo.getDid()));
        neverminedAPI.getAssetsAPI().resolve(ddo.getDid());
    }

    @Test
    public void ownerAssets() throws Exception {
        int assetsOwnedBefore = (neverminedAPI.getAssetsAPI().ownerAssets(neverminedAPI.getMainAccount().address)).size();

        metadataBase.attributes.main.dateCreated = new Date();
        neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");

        int assetsOwnedAfter = neverminedAPI.getAssetsAPI().ownerAssets(neverminedAPI.getMainAccount().address).size();
        assertEquals(assetsOwnedAfter, assetsOwnedBefore + 1);
    }

}
