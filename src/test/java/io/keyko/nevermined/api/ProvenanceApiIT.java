package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.helpers.CryptoHelper;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.contracts.DIDRegistry;
import io.keyko.nevermined.exceptions.ProvenanceException;
import io.keyko.nevermined.manager.ManagerHelper;
import io.keyko.nevermined.manager.ProvenanceManager;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.contracts.ProvenanceEntry;
import io.keyko.nevermined.models.contracts.ProvenanceEvent;
import io.keyko.nevermined.models.service.ProviderConfig;
import io.keyko.nevermined.models.service.types.ComputingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.web3j.crypto.Sign;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class ProvenanceApiIT {

    private static final Logger log = LogManager.getLogger(ProvenanceApiIT.class);

    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;

    private static ComputingService.Provider computingProvider;
    private static ProviderConfig providerConfig;
    private static NeverminedAPI neverminedAPI;

    private static KeeperService keeper;
    private static ProvenanceManager provenanceManager;
    private static DIDRegistry didRegistry;
    private static Config config = ConfigFactory.load();

    private static final String DEFAULT_ACCOUNT = config.getString("account.main.address");
    private static final String DELEGATED_ACCOUNT = config.getString("account.parity.address");


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

        neverminedAPI = NeverminedAPI.getInstance(config);
        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.main);
        didRegistry = ManagerHelper.loadDIDRegistryContract(keeper, config.getString("contract.DIDRegistry.address"));

        provenanceManager= ProvenanceManager.getInstance(keeper);
        provenanceManager.setDidRegistryContract(didRegistry);

        assertNotNull(neverminedAPI.getAssetsAPI());
        assertNotNull(neverminedAPI.getMainAccount());

    }

    @Test
    public void getDIDProvenanceEventsTest() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        String activityId = generateRandomID();
        String provenanceId = generateRandomID();

        final DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        final boolean success = neverminedAPI.getProvenanceAPI().used(
                provenanceId, ddo.getDid(), DEFAULT_ACCOUNT, activityId, "", "used test");
        assertTrue(success);

        final List<ProvenanceEvent> provenanceEvents = neverminedAPI.getProvenanceAPI()
                .getDIDProvenanceEvents(ddo.getDid());

        Thread.sleep(3000);
        assertEquals(2, provenanceEvents.size());
    }

    @Test
    public void getDIDProvenanceFromRegistry() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        String activityId = generateRandomID();
        String provenanceId = generateRandomID();

        final DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        final boolean success = neverminedAPI.getProvenanceAPI().used(
                provenanceId, ddo.getDid(), DEFAULT_ACCOUNT, activityId, "", "used test");
        assertTrue(success);

        final ProvenanceEntry provenanceEntry = neverminedAPI.getProvenanceAPI().getProvenanceEntry(provenanceId);
        assertEquals("0x" + activityId, provenanceEntry.activityId);
        assertEquals(ddo.getDid().getHash(), provenanceEntry.did.getHash());
        assertEquals(ProvenanceEntry.ProvenanceMethod.USED, provenanceEntry.method);
        assertEquals(DEFAULT_ACCOUNT, provenanceEntry.createdBy);

        final String provenanceOwner = neverminedAPI.getProvenanceAPI().getProvenanceOwner(provenanceId);
        assertEquals(DEFAULT_ACCOUNT, provenanceOwner);
    }


    @Test
    public void delegatesManagementTest() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();

        final DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        assertFalse(neverminedAPI.getProvenanceAPI().isProvenanceDelegate(ddo.getDid(), DELEGATED_ACCOUNT));
        assertFalse(neverminedAPI.getProvenanceAPI().isProvenanceDelegate(ddo.getDid(), DEFAULT_ACCOUNT));
        assertTrue(neverminedAPI.getProvenanceAPI().addDIDProvenanceDelegate(ddo.getDid(), DELEGATED_ACCOUNT));
        assertTrue(neverminedAPI.getProvenanceAPI().isProvenanceDelegate(ddo.getDid(), DELEGATED_ACCOUNT));
        assertTrue(neverminedAPI.getProvenanceAPI().removeDIDProvenanceDelegate(ddo.getDid(), DELEGATED_ACCOUNT));
        assertFalse(neverminedAPI.getProvenanceAPI().isProvenanceDelegate(ddo.getDid(), DELEGATED_ACCOUNT));

    }

    @Test
    public void searchMultipleProvenanceEventsTest() throws Exception {
        metadataBase.attributes.main.dateCreated = new Date();
        String activityId = generateRandomID();
        String provenanceId = generateRandomID();
        DID derivedDid = DID.builder();

        final DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        neverminedAPI.getProvenanceAPI().used(
                provenanceId, ddo.getDid(), DEFAULT_ACCOUNT, activityId, "", "used test");
        neverminedAPI.getProvenanceAPI().wasDerivedFrom(
                generateRandomID(), derivedDid, ddo.getDid(), DEFAULT_ACCOUNT, activityId, "derived test");
        neverminedAPI.getProvenanceAPI().wasAssociatedWith(
                generateRandomID(),  ddo.getDid(), DEFAULT_ACCOUNT, activityId, "associated test");

        List<ProvenanceEvent> provenanceEvents = neverminedAPI.getProvenanceAPI()
                .getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod.WAS_GENERATED_BY, ddo.getDid());
        assertEquals(1, provenanceEvents.size());

        provenanceEvents = neverminedAPI.getProvenanceAPI()
                .getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod.USED, ddo.getDid());
        assertEquals(1, provenanceEvents.size());

        provenanceEvents = neverminedAPI.getProvenanceAPI()
                .getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod.WAS_DERIVED_FROM, derivedDid);
        assertEquals(1, provenanceEvents.size());

        provenanceEvents = neverminedAPI.getProvenanceAPI()
                .getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod.WAS_ASSOCIATED_WITH, ddo.getDid());
        assertEquals(1, provenanceEvents.size());
    }

    @Test
    public void callingUsedWithSignature() throws Exception {
        String activityId = generateRandomID();
        String provenanceId = generateRandomID();

        final String signature = EthereumHelper.ethSignMessage(
                keeper.getWeb3Admin(), "0x" + provenanceId, keeper.getAddress(), config.getString("account.main.password"));

        final DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        final boolean success = neverminedAPI.getProvenanceAPI().used(
                provenanceId, ddo.getDid(), DEFAULT_ACCOUNT, activityId, signature, "used test");
        assertTrue(success);
    }

    @Test
    public void callingActedOnBehalf() throws Exception {
        String activityId = generateRandomID();
        String provenanceId = generateRandomID();

        Sign.SignatureData signatureData = EthereumHelper.signMessage(
                provenanceId, provenanceManager.getKeeperService().getCredentials());
        String signature= EncodingHelper.signatureToString(signatureData);

        final DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        final boolean success = neverminedAPI.getProvenanceAPI().actedOnBehalf(
                provenanceId, ddo.getDid(), DEFAULT_ACCOUNT, DEFAULT_ACCOUNT, activityId, signature, "actedOnBehalf test");
        assertTrue(success);

        List<ProvenanceEvent> provenanceEvents = neverminedAPI.getProvenanceAPI()
                .getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod.ACTED_ON_BEHALF, ddo.getDid());
        assertEquals(1, provenanceEvents.size());

    }

    @Test(expected = ProvenanceException.class)
    public void callingTwiceSameProvenanceId() throws Exception {
        String provenanceId = generateRandomID();
        String activityId = generateRandomID();

        final DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        neverminedAPI.getProvenanceAPI().used(
                provenanceId, ddo.getDid(), DEFAULT_ACCOUNT, activityId, "", "used test");
        boolean success = neverminedAPI.getProvenanceAPI().used(
                provenanceId, ddo.getDid(), DEFAULT_ACCOUNT, activityId, "", "used test");
        assertFalse(success);
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

    @Ignore
    @Test
    public void signAndValidateMessages() throws Exception {
        String signingAdders = provenanceManager.getKeeperService().getAddress();
        String _sourceMessage = "hi there";
        String message = CryptoHelper.sha3256(_sourceMessage).replace("0x", "");
        String prefix = "\u0019Ethereum Signed Message:\n32";
        String _message = CryptoHelper.sha3256(prefix + message);
        String _messageHash = CryptoHelper.sha3256("\u0019Ethereum Signed Message:\n" + _message.length() + _message);

        Sign.SignatureData signatureData =
                EthereumHelper.signMessage(_message, provenanceManager.getKeeperService().getCredentials());

        String _signature = EncodingHelper.signatureToString(signatureData);
        log.info("Source Message Hash: " + CryptoHelper.sha3256(_sourceMessage));
        log.info("Message: " + _message);
        log.info("Message Hash: " + _messageHash);
        log.info("Signature: " + _signature);

        assertTrue(EthereumHelper.wasSignedByAddress(signingAdders, signatureData, _messageHash.getBytes()));

        byte[] signatureBytes = EncodingHelper.signatureToString(signatureData).getBytes();

        final Boolean result = didRegistry.provenanceSignatureIsCorrect(
                signingAdders,
                _messageHash.getBytes(),
                signatureBytes
        ).send();
        assertTrue(result);

    }

    private String generateRandomID() {
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        return token.replaceAll("-", "");
    }

}
