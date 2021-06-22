package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.contracts.AccessTemplate;
import io.keyko.nevermined.contracts.TemplateStoreManager;
import io.keyko.nevermined.manager.ManagerHelper;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.Balance;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.service.*;
import io.keyko.nevermined.models.service.types.NFTAccessService;
import io.keyko.nevermined.models.service.types.NFTSalesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

public class NFTsApiIT {

    private static final Logger log = LogManager.getLogger(NFTsApiIT.class);

    final private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;
    private static ProviderConfig providerConfig;
    private static NeverminedAPI apiArtist;
    private static NeverminedAPI apiCollector;

    private static KeeperService keeper;

    private static final Config config = ConfigFactory.load();
    private static final String TEMPLATE_ID_NFT_ACCESS = config.getString("contract.NFTAccessTemplate.address");
    private static final String TEMPLATE_ID_NFT_SALES = config.getString("contract.NFTSalesTemplate.address");

    private static final String NFT_ACCESS_ENDPOINT = "http://localhost:8030/api/v1/gateway/services/nft-access";
    private static final String NFT_SALES_ENDPOINT = "http://localhost:8030/api/v1/gateway/services/nft-access";


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String gatewayUrl = config.getString("gateway.url");
        String consumeUrl = gatewayUrl + "/api/v1/gateway/services/access";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, gatewayUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        apiArtist = NeverminedAPI.getInstance(config);

        assertNotNull(apiArtist.getAssetsAPI());
        assertNotNull(apiArtist.getMainAccount());

        apiCollector = ManagerHelper.getNeverminedAPI(config, ManagerHelper.VmClient.parity, "2");

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        AccessTemplate escrowAccessSecretStoreTemplate = ManagerHelper.loadAccessTemplate(keeper, config.getString("contract.AccessTemplate.address"));
        TemplateStoreManager templateManager = ManagerHelper.loadTemplateStoreManager(keeper, config.getString("contract.TemplateStoreManager.address"));

        apiCollector.getTokensAPI().request(new BigInteger(getTestAssetRewards().totalPrice).multiply(BigInteger.TEN));
        Balance balance = apiCollector.getAccountsAPI().balance(apiCollector.getMainAccount());

        log.debug("Account " + apiCollector.getMainAccount().address + " balance is: " + balance.toString());

        boolean isTemplateApproved = templateManager.isTemplateApproved(escrowAccessSecretStoreTemplate.getContractAddress()).send();
        log.debug("Is escrowAccessSecretStoreTemplate approved? " + isTemplateApproved);
    }

    private static AssetRewards getTestAssetRewards()  {
        final AssetRewards assetRewards = new AssetRewards(
                Map.ofEntries(
                        new AbstractMap.SimpleEntry<>(apiArtist.getMainAccount().address, "10"),
                        new AbstractMap.SimpleEntry<>(config.getString("provider.address"), "2")
                )
        );
        assetRewards.numberNFTs = BigInteger.ONE;
        return assetRewards;
    }

    @Test
    public void createMintable() throws Exception {

        String clientAddress = apiArtist.getMainAccount().getAddress();
        final AssetRewards assetRewards = getTestAssetRewards();
        metadataBase.attributes.main.dateCreated = new Date();
        DDO ddo = apiArtist.getAssetsAPI().createMintableDID(metadataBase, providerConfig, assetRewards, BigInteger.TEN, BigInteger.ZERO);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = apiArtist.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertEquals(4, resolvedDDO.services.size());

        assertEquals(BigInteger.ZERO, apiArtist.getNFTsAPI().balance(clientAddress, did));
        apiArtist.getNFTsAPI().mint(did, BigInteger.TEN);
        assertEquals(BigInteger.TEN, apiArtist.getNFTsAPI().balance(clientAddress, did));

        apiArtist.getNFTsAPI().burn(did, BigInteger.TWO);
        assertEquals(BigInteger.valueOf(8), apiArtist.getNFTsAPI().balance(clientAddress, did));
    }

    @Test
    public void e2eNFTsFlow() throws Exception  {
        String artistAddress = apiArtist.getMainAccount().getAddress();
        final AssetRewards assetRewards = getTestAssetRewards();
        metadataBase.attributes.main.dateCreated = new Date();

        // 1. The ARTIST defines the services to be offered associated to the Asset
        final NFTAccessService nftAccess = ServiceBuilder.buildNFTAccessService(
                providerConfig.setAccessEndpoint(NFT_ACCESS_ENDPOINT), TEMPLATE_ID_NFT_ACCESS, assetRewards, artistAddress);

        final NFTSalesService nftSales = ServiceBuilder.buildNFTSalesService(
                providerConfig.setAccessEndpoint(NFT_SALES_ENDPOINT), TEMPLATE_ID_NFT_SALES, assetRewards, artistAddress);

        final ServiceDescriptor nftSalesDesc = new ServiceDescriptor(nftSales, assetRewards);
        final ServiceDescriptor nftAccessDesc = new ServiceDescriptor(nftAccess, assetRewards);

        // 2. The ARTIST publish the asset
        DDO ddo = apiArtist.getAssetsAPI().create(
                metadataBase, Arrays.asList(nftSalesDesc, nftAccessDesc), providerConfig, BigInteger.TEN, BigInteger.ZERO);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = apiArtist.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
        assertEquals(5,resolvedDDO.services.size());

        // 3. As COLLECTOR I want to buy some art
        // 3.1. I am setting an agreement for buying a NFT
        final Service nftSalesService = resolvedDDO.getServiceByType(Service.ServiceTypes.NFT_SALES);
        final BigInteger numberNFTs = nftSalesService.fetchNumberNFTs();
        final OrderResult salesOrderResult = apiCollector.getNFTsAPI().order(did, nftSalesService.index);
        assertTrue(salesOrderResult.isAccessGranted());

        assertFalse(apiArtist.getNFTsAPI().balance(artistAddress, did).compareTo(numberNFTs) >= 0);
        apiArtist.getNFTsAPI().mint(did, numberNFTs);
        assertTrue(apiArtist.getNFTsAPI().balance(artistAddress, did).compareTo(numberNFTs) >= 0);

        final AgreementStatus status = apiArtist.getAgreementsAPI().status(salesOrderResult.getServiceAgreementId());
        final BigInteger lockStatus = status.conditions.get(0).conditions.get(Condition.ConditionTypes.lockPayment.toString());
        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(), lockStatus);

        // The ARTIST can transfer the NFT to the collector
        final Agreement salesAgreement = apiArtist.getAgreementsAPI().getAgreement(salesOrderResult.getServiceAgreementId());
        final boolean transferOk = apiArtist.getConditionsAPI().transferNFT(
                salesOrderResult.getServiceAgreementId(),
                did,
                apiCollector.getMainAccount().getAddress(),
                numberNFTs,
                EncodingHelper.toHexString(salesAgreement.conditions.get(0))
        );
        assertTrue(transferOk);

        // The ARTIST ask and receives the payment
        final boolean releaseRewardOk = apiArtist.getConditionsAPI().releaseReward(salesOrderResult.getServiceAgreementId());
        assertTrue(releaseRewardOk);

        // 4. As ARTIST I want to give exclusive access to the collectors owning a specific NFT
        // 4.1. As COLLECTOR I want get access to a exclusive service provided by the artist
        final OrderResult accessOrderResult = apiCollector.getNFTsAPI().order(did, nftAccess.index);
        assertTrue(accessOrderResult.isAccessGranted());

        InputStream result = apiCollector.getAssetsAPI().downloadBinary(
                accessOrderResult.getServiceAgreementId(),
                did,
                nftAccess.index,
                0);

        assertNotNull(result);

        // TODO: Extend the test
        // 5. As COLLECTOR I want to sell my NFT to a GALLERY for a higher price
        // 5.1. As GALLERY I setup an agreement for buying an NFT to COLLECTOR
        // 5.2. As ARTIST I want to receive royalties for the NFT I created and was sold in the secondary market
    }

}
