package io.keyko.ocean.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.helpers.CryptoHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.ocean.api.config.OceanConfig;
import io.keyko.ocean.exceptions.DDOException;
import io.keyko.ocean.keeper.contracts.TemplateStoreManager;
import io.keyko.ocean.manager.ManagerHelper;
import io.keyko.ocean.models.Balance;
import io.keyko.ocean.models.DDO;
import io.keyko.ocean.models.DID;
import io.keyko.ocean.models.asset.AssetMetadata;
import io.keyko.ocean.models.asset.OrderResult;
import io.keyko.ocean.models.service.ProviderConfig;
import io.keyko.ocean.models.service.Service;
import io.keyko.ocean.models.service.types.ComputingService;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
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

    private static String accessTemplateAddress;
    private static String computeTemplateAddress;


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
        accessTemplateAddress = config.getString("template.EscrowAccessSecretStoreTemplate.address");
        computeTemplateAddress = config.getString("template.EscrowComputeExecutionTemplate.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getMainAccount());

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        oceanAPIConsumer = OceanAPI.getInstance(
                ManagerHelper.getDefaultProperties(config, "2"));

//        TemplateStoreManager templateManager = ManagerHelper.loadTemplateStoreManager(keeper, config.getString("contract.TemplateStoreManager.address"));
        TemplateStoreManager templateStoreManager= ManagerHelper.deployTemplateStoreManager(keeper);
        templateStoreManager.initialize(keeper.getAddress()).send();

        String owner= templateStoreManager.owner().send();

        oceanAPI.setTemplateStoreManagerContract(templateStoreManager);
        oceanAPIConsumer.setTemplateStoreManagerContract(templateStoreManager);

        ManagerHelper.prepareEscrowTemplate(
                oceanAPI,
                config.getString("contract.EscrowReward.address"),
                owner,
                "EscrowAccessSecretStoreTemplate");



        Balance balance = oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount());
        log.debug("Account " + oceanAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        oceanAPIConsumer.getTokensAPI().request(BigInteger.TEN);

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
        Assert.assertEquals(owner, oceanAPI.getMainAccount().address);
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
