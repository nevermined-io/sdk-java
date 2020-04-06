package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
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
        OEP7_WORKFLOW_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP7_WORKFLOW_EXAMPLE_URL), "utf-8");
        OEP7_SERVICE_EXAMPLE_CONTENT = IOUtils.toString(new URI(OEP7_SERVICE_EXAMPLE_URL), "utf-8");

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
        assertEquals("5eae9f7640383f27c3bfb1ec14b76a2660c9e4f7d24a8c978f07cb34cb465968", checksums.get("0"));
        assertEquals("dc905c7f8d7adef28ae671ae2b54532af2ee70bc96fc1018743adfc31f4621be", checksums.get("2"));

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
        assertEquals("c2b420addae81a9eb3e0c727e6de60a89904a37f6f221260a1e60d63a1814f0a", checksums.get("0"));

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
        assertEquals("33a47aa39545c5417d78d1850ed58d55fc10e81c0ba7d624a5926b778a994c46", checksums.get("0"));
        assertEquals("f12b645393c7640da2cbb172ba27206d9a0c610890f118b1c4bc98c506703e27", checksums.get("1"));

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
        assertEquals("eef1f12599a70e9e9e155c010fa99703eb1a8ebf779dcfcb1f1df7da942015c1", checksums.get("0"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:op:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }

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
