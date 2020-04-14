package io.keyko.ocean.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.net.URI;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;

public class DdoIT {

    private static final Logger log = LogManager.getLogger(DdoIT.class);

    private static final String OEP12_COMPUTING_EXAMPLE_URL = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/12/ddo.computing.json";
    private static final String OEP12_WORKFLOW_EXAMPLE_URL = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/12/ddo.workflow.json";

    private static final String OEP7_DATASET_EXAMPLE_URL = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/8/v0.4/ddo-example-access.json";
    private static final String OEP7_ALGORITHM_EXAMPLE_URL = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/8/v0.4/ddo-example-algorithm.json";
    private static final String OEP7_WORKFLOW_EXAMPLE_URL = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/8/v0.4/ddo-example-workflow.json";
    private static final String OEP7_SERVICE_EXAMPLE_URL = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/8/v0.4/ddo-example-service.json";

    private static  String OEP12_COMPUTING_EXAMPLE_CONTENT;
    private static  String OEP12_WORKFLOW_EXAMPLE_CONTENT;
    private static  String OEP7_DATASET_EXAMPLE_CONTENT;
    private static  String OEP7_ALGORITHM_EXAMPLE_CONTENT;
    private static  String OEP7_WORKFLOW_EXAMPLE_CONTENT;
    private static  String OEP7_SERVICE_EXAMPLE_CONTENT;

    private static final Config config = ConfigFactory.load();

    private static Credentials credentials;


    @BeforeClass
    public static void setUp() throws Exception {

        OEP12_COMPUTING_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP12_COMPUTING_EXAMPLE_URL), "utf-8");
        OEP12_WORKFLOW_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP12_WORKFLOW_EXAMPLE_URL), "utf-8");

        OEP7_DATASET_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP7_DATASET_EXAMPLE_URL), "utf-8");
        OEP7_ALGORITHM_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP7_ALGORITHM_EXAMPLE_URL), "utf-8");
//        OEP7_WORKFLOW_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP7_WORKFLOW_EXAMPLE_URL), "utf-8");
//        OEP7_SERVICE_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP7_SERVICE_EXAMPLE_URL), "utf-8");

        credentials = WalletUtils.loadCredentials(
                config.getString("account.main.password"),
                config.getString("account.main.credentialsFile"));
    }

    @Test
    public void testOEP12Computing() throws Exception {

        log.debug("TestOEP12Computing");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, OEP12_COMPUTING_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(2, checksums.size());
        assertEquals("574b45e304c190c293855548f19b02ad0c826aee4b76fb3b3c25b27d8317efd7", checksums.get("0"));
        assertEquals("603317133c7949ea3ba57255dcdb2a44b3427aea6af3dd358a19a0c9ce4193d3", checksums.get("2"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:op:3ea445410f608e6453cdcb7dbe42d57a89aca018993d7e87da85993cbccc6308", did.did);

    }


    @Test
    public void testOEP12Workflow() throws Exception {

        log.debug("TestOEP12Workflow");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, OEP12_WORKFLOW_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(1, checksums.size());
        assertEquals("f548c344c09c91883a85207f8eaadde4b448d03fb89208ee7137a73b3894a682", checksums.get("0"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:op:3ea445410f608e6453cdcb7dbe42d57a89aca018993d7e87da85993cbccc6308", did.did);

    }


    @Test
    public void testDDOServicesOrder() throws Exception {

        log.debug("testDDOServicesOrder");
        DDO ddoFromJson = DDO.fromJSON(new TypeReference<DDO>() {}, OEP7_DATASET_EXAMPLE_CONTENT);
        DDO ddo= ddoFromJson.integrityBuilder(credentials);

        assertEquals("metadata", ddo.services.get(0).type);
        assertEquals("access", ddo.services.get(1).type);

        assertEquals(0, ddo.services.get(0).index);
        assertEquals(1, ddo.services.get(1).index);
    }

    @Test
    public void testOEP7DatasetMetadata() throws Exception {

        log.debug("testOEP7DatasetMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, OEP7_DATASET_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(2, checksums.size());
        assertEquals("9ca5f006901cced5d5b02fd3691b12b619d6a083bb3fca39b8c90bd60f194cf9", checksums.get("0"));
        assertEquals("4294cb191438237940fe389fee1dcb5a0806432ac92622d50375a01d3406fa62", checksums.get("1"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:op:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }

    @Test
    public void testOEP7AlgorithmMetadata() throws Exception {

        log.debug("testOEP7AlgorithmMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, OEP7_ALGORITHM_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(1, checksums.size());
        assertEquals("cac1a3df1d0dbbda8ced1f166ea46287536f15edb89705da11714cbeb588e43f", checksums.get("0"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:op:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }

    // Test ignored because workflows were removed of OEP
    @Ignore
    @Test
    public void testOEP7WorkflowMetadata() throws Exception {

        log.debug("testOEP7WorkflowMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, OEP7_WORKFLOW_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(1, checksums.size());
        assertEquals("58649304d489f460bd71c675e177dcab2b78692dc9b329cb1c0c27bb1735eaa5", checksums.get("0"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:op:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }

    // Test ignored because services were removed of OEP
    @Ignore
    @Test
    public void testOEP7ServiceMetadata() throws Exception {

        log.debug("testOEP7ServiceMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, OEP7_SERVICE_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(2, checksums.size());
        assertEquals("52eb261dc24117d78ffdd2c5b3409f94d013601f1a45aa43da6d1611671c74a3", checksums.get("0"));
        assertEquals("f12b645393c7640da2cbb172ba27206d9a0c610890f118b1c4bc98c506703e27", checksums.get("1"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:op:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }




}
