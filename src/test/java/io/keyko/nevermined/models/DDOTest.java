package io.keyko.nevermined.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.exceptions.DIDFormatException;
import io.keyko.nevermined.models.service.Service;
import io.keyko.nevermined.models.service.types.AccessService;
import io.keyko.nevermined.models.service.types.AuthorizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class DDOTest {

    private static final Logger log = LogManager.getLogger(DDOTest.class);

    // DDO example downloaded from w3c site
    private static final String DDO_JSON_SAMPLE = "src/test/resources/examples/ddo-example.json";
    private static String DDO_JSON_CONTENT;

    private static final String DDO_JSON_AUTH_SAMPLE = "src/test/resources/examples/ddo-example-authorization.json";
    private static String DDO_JSON_AUTH_CONTENT;

    private static final String DDO_JSON_WORKFLOW_SAMPLE = "src/test/resources/examples/ddo-example-workflow.json";
    private static String DDO_JSON_WORKFLOW_CONTENT;

    private static final String DDO_JSON_ALGORITHM_SAMPLE = "src/test/resources/examples/ddo-example-algorithm.json";
    private static String DDO_JSON_ALGORITHM_CONTENT;

    private static final String DDO_JSON_SERVICE_SAMPLE = "src/test/resources/examples/ddo-example-service.json";
    private static String DDO_JSON_SERVICE_CONTENT;

    private static final Config config = ConfigFactory.load();

    private static Credentials credentials;

    @BeforeClass
    public static void setUp() throws Exception {
        DDO_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_SAMPLE)));
        DDO_JSON_AUTH_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_AUTH_SAMPLE)));
        DDO_JSON_WORKFLOW_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_WORKFLOW_SAMPLE)));
        DDO_JSON_ALGORITHM_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_ALGORITHM_SAMPLE)));
        DDO_JSON_SERVICE_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_SERVICE_SAMPLE)));
        credentials = WalletUtils.loadCredentials(
                config.getString("account.main.password"),
                config.getString("account.main.credentialsFile"));

    }

    @Test
    public void testDID() throws Exception {
        assertEquals(0, new DID().toString().length());
        assertEquals(0, new DID().setEmptyDID().toString().length());
        assertEquals("did:nv:123", DID.getFromHash("123").toString());
    }

    @Test(expected = DIDFormatException.class)
    public void badDID() throws Exception {
        new DID("did:kkdid:123");
    }

    @Test
    public void generateDID() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        assertTrue(ddo.id.startsWith(DID.PREFIX));
        assertEquals(64, ddo.getDID().getHash().length());
    }


    @Test
    public void generateDIDFromChecksums() throws Exception {

        SortedMap<String, String> checksums= new TreeMap<>();
        checksums.put("0", "0x52b5c93b82dd9e7ecc3d9fdf4755f7f69a54484941897dc517b4adfe3bbc3377");
        checksums.put("1",  "0x999999952b5c93b82dd9e7ecc3d9fdf4755f7f69a54484941897dc517b4adfe3");

        DDO ddo = new DDO();
        String json = ddo.toJson(checksums);

        DID did = DID.builder(json);
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
    }

    @Test
    public void generateChecksums() throws Exception {

        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);
        SortedMap<String, String> checksums = DDO.generateChecksums(ddo);

        assertEquals(2, checksums.size());

    }

    @Test
    public void checkDateFormat() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        log.debug("Date found: " + AbstractModel.DATE_FORMAT.format(ddo.created));
        log.debug("Date String: " + ddo.created.toString());
        assertTrue(AbstractModel.DATE_FORMAT.format(ddo.created).startsWith("20"));
        assertTrue(AbstractModel.DATE_FORMAT.format(ddo.updated).startsWith("20"));

        DDO newDDO= new DDO();
        log.debug("Date found: " + AbstractModel.DATE_FORMAT.format(newDDO.created));
        assertTrue(AbstractModel.DATE_FORMAT.format(newDDO.created).startsWith("20"));
        assertTrue(AbstractModel.DATE_FORMAT.format(newDDO.updated).startsWith("20"));
    }

    @Test
    public void checkFiles() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);
        String ddoJson = ddo.toJson();
        ddo = DDO.fromJSON(new TypeReference<DDO>() {
        }, ddoJson);

        assertEquals(1, ddo.services.get(0).attributes.main.files.size());
        assertEquals("https://raw.githubusercontent.com/tbertinmahieux/MSongsDB/master/Tasks_Demos/CoverSongs/shs_dataset_test.txt",
                ddo.services.get(0).attributes.main.files.get(0).url);
        assertEquals("shs_dataset_test.txt", ddo.services.get(0).attributes.main.files.get(0).name);
    }

    @Test
    public void testWorkflow() throws Exception {
        // This tests both serialization and deserialization
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_WORKFLOW_CONTENT);
        String ddoJson = ddo.toJson(ddo);
        ddo = DDO.fromJSON(new TypeReference<DDO>() {
        }, ddoJson);

        Service metadataService = ddo.getMetadataService();

        assertEquals("workflow", metadataService.attributes.main.type);
        assertEquals("tensorflow/tensorflow", metadataService.attributes.main.workflow.stages.get(0).requirements.container.image);
        assertEquals("latest",  metadataService.attributes.main.workflow.stages.get(0).requirements.container.tag);
        assertEquals("sha256:cb57ecfa6ebbefd8ffc7f75c0f00e57a7fa739578a429b6f72a0df19315deadc",
                metadataService.attributes.main.workflow.stages.get(0).requirements.container.checksum);

        assertEquals(2,  metadataService.attributes.main.workflow.stages.get(0).input.size());
        assertEquals(0,  metadataService.attributes.main.workflow.stages.get(0).input.get(0).index.intValue());
        assertEquals("did:nv:12345",  metadataService.attributes.main.workflow.stages.get(0).input.get(0).id.toString());
        assertEquals("did:nv:abcde",  metadataService.attributes.main.workflow.stages.get(0).transformation.id.toString());
        assertEquals("https://localhost:5000/api/v1/metadata/assets/ddo/",
                metadataService.attributes.main.workflow.stages.get(0).output.metadataUrl);
    }

    @Test
    public void testAlgorithm() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_ALGORITHM_CONTENT);

        Service metadataService = ddo.getMetadataService();

        assertEquals("algorithm",  metadataService.attributes.main.type);
        assertEquals("python", metadataService.attributes.main.algorithm.language);
        assertEquals("py", metadataService.attributes.main.algorithm.format);
        assertEquals("0.1", metadataService.attributes.main.algorithm.version);
        assertEquals("3.8-alpine", metadataService.attributes.main.algorithm.requirements.container.tag);

    }

    @Test
    public void testService() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_SERVICE_CONTENT);

        Service metadataService = ddo.getMetadataService();

        assertEquals("service",  metadataService.attributes.main.type);
        assertEquals("https://my.service.inet:8080/spec",  metadataService.attributes.main.spec);
        assertEquals("859486596784567856758aaaa",  metadataService.attributes.main.specChecksum);
        assertEquals("basic",  metadataService.attributes.main.definition.auth.type);

        assertEquals(1,  metadataService.attributes.main.definition.endpoints.size());
        assertEquals(0,  metadataService.attributes.main.definition.endpoints.get(0).index.intValue());
        assertEquals("POST",  metadataService.attributes.main.definition.endpoints.get(0).method);
        assertEquals(1,  metadataService.attributes.main.definition.endpoints.get(0).contentTypes.size());

    }

    @Test
    public void testChecksum() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {
        }, DDO_JSON_CONTENT);

        String checksum1= ddo.services.get(0).attributes.main.checksum();
        log.debug("Checksum: " + checksum1);
        assertEquals(64, checksum1.length());

        String checksum2= ddo.services.get(1).attributes.main.checksum();
        log.debug("Checksum: " + checksum2);
        assertEquals(64, checksum2.length());
    }


    @Test
    public void testIntegrityBuilder() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {
        }, DDO_JSON_CONTENT);

        DDO newDDO= DDO.integrityBuilder(ddo, credentials);
        log.debug("DDO generated with DID: " + newDDO.getDID().did);

        log.debug(ddo.toJson(newDDO.proof));
        assertEquals(2, newDDO.proof.checksum.size());
        assertEquals(64 + DID.PREFIX.length(), newDDO.getDID().did.length());
        assertEquals(newDDO.id, newDDO.getDID().did);
    }


    @Test
    public void generateRandomDID() throws Exception {
        DID did= DID.builder();
        assertEquals(64, did.getHash().length());
    }

    @Test
    public void jsonToModel() throws Exception {

        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {
        }, DDO_JSON_CONTENT);

        assertEquals("https://w3id.org/did/v1", ddo.context);
        assertEquals("did:nv:0bc278fee025464f8012b811d1bce8e22094d0984e4e49139df5d5ff7a028bdf", ddo.id.toString());
        assertEquals(3, ddo.publicKeys.size());
        assertTrue(ddo.publicKeys.get(0).id.startsWith("did:nv:b6e2eb5eff1a093ced9826315d5a4ef6c5b5c8bd3c49890ee284231d7e1d0aaa"));

        assertEquals(1, ddo.authentication.size());
        assertTrue(ddo.authentication.get(0).publicKey.startsWith("did:nv:0ebed8226ada17fde24b6bf2b95d27f8f05fcce09139ff5cec31f6d81a7cd2ea"));

        assertEquals(2, ddo.services.size());
        assertTrue(ddo.services.get(1).serviceEndpoint.startsWith("http"));

    }

    @Test
    public void jsonToModelWithAuth() throws Exception {

        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {
        }, DDO_JSON_AUTH_CONTENT);

        assertEquals("https://w3id.org/did/v1", ddo.context);
        assertEquals("did:nv:0bc278fee025464f8012b811d1bce8e22094d0984e4e49139df5d5ff7a028bdf", ddo.id.toString());
        assertEquals(3, ddo.publicKeys.size());
        assertTrue(ddo.publicKeys.get(0).id.startsWith("did:nv:b6e2eb5eff1a093ced9826315d5a4ef6c5b5c8bd3c49890ee284231d7e1d0aaa"));

        assertEquals(1, ddo.authentication.size());
        assertTrue(ddo.authentication.get(0).publicKey.startsWith("did:nv:0ebed8226ada17fde24b6bf2b95d27f8f05fcce09139ff5cec31f6d81a7cd2ea"));

        assertEquals(3, ddo.services.size());

        AuthorizationService authorizationService = ddo.getAuthorizationService();
        assertTrue(authorizationService.serviceEndpoint.contains("localhost:8030"));
        assertEquals(Service.ServiceTypes.AUTHORIZATION.toString(), authorizationService.type);
    }

    @Test
    public void modelToJson() throws Exception {
//        String did = "did:nv:12345";
        DID did = DID.builder();
        DDO ddo = new DDO();

        DDO.PublicKey pk = new DDO.PublicKey();
        pk.id = did.getDid();
        pk.type = "RsaVerificationKey2018";
        pk.owner = did.getDid() + "owner";

        ddo.publicKeys.add(pk);
        ddo.publicKeys.add(pk);

        DDO.Authentication auth = new DDO.Authentication(did.getDid());
        auth.type = "AuthType";
        auth.publicKey = "AuthPK";

        ddo.authentication.add(auth);
        ddo.authentication.add(auth);
        ddo.authentication.add(auth);

        Service metadataService = new Service(Service.ServiceTypes.METADATA, "http://disney.com", 0);
        metadataService.attributes.main.name = "test name";

        AccessService accessService = new AccessService("http://nevermined.io", 1, "0x00000000");

        ddo.services.add(metadataService);
        ddo.services.add(accessService);


        String modelJson = ddo.toJson();
        log.debug(modelJson);

        JSONObject json = new JSONObject(modelJson);
        assertEquals(2, (json.getJSONArray("publicKey").length()));
        assertEquals(did.getDid(), ((JSONObject) (json.getJSONArray("publicKey").get(0))).getString("id"));

        assertEquals(3, (json.getJSONArray("authentication").length()));
        assertEquals("AuthType", ((JSONObject) (json.getJSONArray("authentication").get(1))).getString("type"));

        assertEquals(2, (json.getJSONArray("service").length()));
        assertEquals("test name", ((JSONObject) (json.getJSONArray("service").get(0))).getJSONObject("attributes").getJSONObject("main").getString("name"));

    }

    @Test
    public void cleanUrls() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        DDO newDdo= DDO.cleanFileUrls(ddo);
        assertNull(newDdo.getMetadataService().attributes.main.files.get(0).url);
    }

}