package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.contracts.AccessTemplate;
import io.keyko.nevermined.contracts.TemplateStoreManager;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.exceptions.DownloadServiceException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.manager.ManagerHelper;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.Balance;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.service.*;
import io.keyko.nevermined.models.service.types.ComputingService;
import io.keyko.nevermined.models.service.types.DIDSalesService;
import io.keyko.nevermined.models.service.types.NFTAccessService;
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

        neverminedAPIConsumer = ManagerHelper.getNeverminedAPI(config, ManagerHelper.VmClient.parity, "2");

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        AccessTemplate escrowAccessSecretStoreTemplate = ManagerHelper.loadAccessTemplate(keeper, config.getString("contract.AccessTemplate.address"));
        TemplateStoreManager templateManager = ManagerHelper.loadTemplateStoreManager(keeper, config.getString("contract.TemplateStoreManager.address"));

        neverminedAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());

        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        boolean isTemplateApproved = templateManager.isTemplateApproved(escrowAccessSecretStoreTemplate.getContractAddress()).send();
        log.debug("Is escrowAccessSecretStoreTemplate approved? " + isTemplateApproved);
    }

    private AssetRewards getTestAssetRewards()  {
        return new AssetRewards(
                Map.ofEntries(
                        new AbstractMap.SimpleEntry<>(neverminedAPI.getMainAccount().address, "10"),
                        new AbstractMap.SimpleEntry<>(config.getString("provider.address"), "2")
                )
        );
    }

    @Test
    public void create() throws Exception {

        final AssetRewards assetRewards = getTestAssetRewards();
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig, assetRewards);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = neverminedAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertTrue(resolvedDDO.services.size() == 4);
        assertEquals(assetRewards.totalPrice, resolvedDDO.getAccessService().attributes.main.price);

        final List<String> receivers = (List<String>) resolvedDDO.getAccessService()
                .getConditionbyName(Condition.ConditionTypes.escrowPayment.name())
                .getParameterByName("_receivers").value;
        assertTrue(receivers.contains(neverminedAPI.getMainAccount().address));
        assertTrue(receivers.contains(config.getString("provider.address")));

        final List<String> _amounts = (List<String>) resolvedDDO.getAccessService()
                .getConditionbyName(Condition.ConditionTypes.escrowPayment.name())
                .getParameterByName("_amounts").value;
        assertTrue(_amounts.contains("10"));
        assertTrue(_amounts.contains("2"));
    }

    @Test
    public void createComputeService() throws Exception {

        metadataBaseAlgorithm.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().createComputeService(
                metadataBaseAlgorithm, providerConfig, computingProvider, getTestAssetRewards());

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

        Flowable<OrderResult> response = neverminedAPIConsumer.getAssetsAPI().purchaseOrder(did, Service.DEFAULT_ACCESS_INDEX);

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

        final AssetRewards testAssetRewards = getTestAssetRewards();

        int consumedAssetsBefore = neverminedAPI.getAssetsAPI().consumerAssets(
                neverminedAPIConsumer.getMainAccount().address).size();

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig, testAssetRewards);
        DID did = new DID(ddo.id);

        neverminedAPIConsumer.getAccountsAPI().requestTokens(new BigInteger(testAssetRewards.totalPrice));
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());
        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        final long startTime = System.currentTimeMillis();
        OrderResult orderResult = neverminedAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);
        final long orderTime = System.currentTimeMillis();

        assertTrue(orderResult.isAccessGranted());

        InputStream result = neverminedAPIConsumer.getAssetsAPI().downloadBinary(
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
        } catch (ServiceException | DownloadServiceException e) {
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

        DDO ddoComputeService = neverminedAPI.getAssetsAPI().createComputeService(
                metadataBase, providerConfig, computingProvider, getTestAssetRewards());

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
        OrderResult orderResult = neverminedAPIConsumer.getAssetsAPI().order(didComputeService, Service.DEFAULT_COMPUTE_INDEX);
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

        // TODO: We need a better way to wait for the compute status and logs to be available
        //       Skipping for now
        // 6. Get compute status
        // The jobs take some time to start we are going to retrie a couple of times
        // in case we get and exception
        // Integer retries = 0;
        // while (true) {
        //     try {
        //         ComputeStatus status = neverminedAPIConsumer.getAssetsAPI().getComputeStatus(
        //             EthereumHelper.add0x(orderResult.getServiceAgreementId()),
        //             executionResult.getExecutionId(),
        //             providerConfig);
        //         assertNotNull(status);
        //         break;
        //     } catch (ServiceException e) {
        //         retries += 1;
        //         if (retries == 3) {
        //             throw e;
        //         }
        //         TimeUnit.SECONDS.sleep(10);
        //     }
        // }
        // 7. Get compute logs
        // The jobs take some time to start we are going to retrie a couple of times
        // in case we get and exception
        // retries = 0;
        // while (true) {
        //     try {
        //         List<ComputeLogs> logs = neverminedAPIConsumer.getAssetsAPI().getComputeLogs(
        //             EthereumHelper.add0x(orderResult.getServiceAgreementId()),
        //             executionResult.getExecutionId(),
        //             providerConfig);
        //         assertNotNull(logs);
        //         break;
        //     } catch (ServiceException e) {
        //         retries += 1;
        //         if (retries == 3) {
        //             throw e;
        //         }
        //         TimeUnit.SECONDS.sleep(10);
        //     }
        // }
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
        Flowable<OrderResult> response = neverminedAPIConsumer.getAssetsAPI().purchaseOrder(did, Service.DEFAULT_ACCESS_INDEX);
        final long orderTime = System.currentTimeMillis();

        OrderResult orderResult = response.blockingFirst();
        assertNotNull(orderResult.getServiceAgreementId());
        assertEquals(true, orderResult.isAccessGranted());
        log.debug("Granted Access Received for the service Agreement " + orderResult.getServiceAgreementId());

        InputStream result = neverminedAPIConsumer.getAssetsAPI().downloadBinary(
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

        String owner = neverminedAPI.getAssetsAPI().owner(ddo.getDID());
        Assert.assertEquals(owner, neverminedAPI.getMainAccount().address);
    }

    @Test(expected = DDOException.class)
    public void retire() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        log.debug("DDO registered!");
        assertTrue(neverminedAPI.getAssetsAPI().retire(ddo.getDID()));
        neverminedAPI.getAssetsAPI().resolve(ddo.getDID());
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


    @Test
    public void manageProviders() throws Exception {
        String someoneAddress = "0x00a329c0648769A73afAc7F9381E08FB43dBEA72".toLowerCase();
        String someoneElseAddress = "0x00Bd138aBD70e2F00903268F3Db08f2D25677C9e".toLowerCase();

        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        DID did = new DID(ddo.id);
        final List<String> initialProviders = neverminedAPI.getAssetsAPI().listProviders(did);
        assertTrue(neverminedAPI.getAssetsAPI().addProvider(did, someoneAddress));
        assertTrue(neverminedAPI.getAssetsAPI().addProvider(did, someoneElseAddress));

        final List<String> providersAfterAdding = neverminedAPI.getAssetsAPI().listProviders(did);
        assertEquals(initialProviders.size() +2, providersAfterAdding.size());
        assertTrue(providersAfterAdding.contains(someoneAddress.toLowerCase()));
        assertTrue(providersAfterAdding.contains(someoneElseAddress.toLowerCase()));

        assertTrue(neverminedAPI.getAssetsAPI().removeProvider(did, someoneAddress));
        final List<String> providersAfterRemoving = neverminedAPI.getAssetsAPI().listProviders(did);
        assertEquals(initialProviders.size() +1, providersAfterRemoving.size());
        assertFalse(providersAfterRemoving.contains(someoneAddress.toLowerCase()));
        assertTrue(providersAfterRemoving.contains(someoneElseAddress.toLowerCase()));

    }


    @Test
    public void transferDIDService() throws Exception {

        final String DID_SALES_ENDPOINT = "http://localhost:8030/api/v1/gateway/services/access";
        final String TEMPLATE_ID_DID_SALES = config.getString("contract.DIDSalesTemplate.address");

        String consumerAddress = neverminedAPIConsumer.getMainAccount().getAddress();
        final AssetRewards assetRewards = getTestAssetRewards();
        metadataBase.attributes.main.dateCreated = new Date();

        final DIDSalesService didSalesService = ServiceBuilder.buildDIDSalesService(
                providerConfig.setAccessEndpoint(DID_SALES_ENDPOINT), TEMPLATE_ID_DID_SALES, assetRewards, consumerAddress);
        final ServiceDescriptor didSalesDesc = new ServiceDescriptor(didSalesService, assetRewards);

        DDO ddo = neverminedAPI.getAssetsAPI().create(
                metadataBase, Arrays.asList(didSalesDesc), providerConfig, BigInteger.ZERO, BigInteger.ZERO);
        DID did = new DID(ddo.id);

        neverminedAPIConsumer.getAccountsAPI().requestTokens(new BigInteger(assetRewards.totalPrice));
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());
        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        OrderResult orderResult = neverminedAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_DID_SALES_INDEX);

        assertTrue(orderResult.isAccessGranted());

        final boolean transferOk = neverminedAPI.getConditionsAPI().transferDIDOwnership(
                orderResult.getServiceAgreementId(),
                did,
                consumerAddress
        );
        assertTrue(transferOk);

        final AgreementStatus status = neverminedAPIConsumer.getAgreementsAPI().status(orderResult.getServiceAgreementId());

        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(),
                status.conditions.get(0).conditions.get(Condition.ConditionTypes.lockPayment.toString()));

        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(),
                status.conditions.get(0).conditions.get(Condition.ConditionTypes.transferDID.toString()));

        final Boolean downloaded = neverminedAPIConsumer.getAssetsAPI().ownerDownload(
                did,
                Service.DEFAULT_DID_SALES_INDEX,
                tempFolder.getRoot().getAbsolutePath());

        assertTrue(downloaded);

    }

}
