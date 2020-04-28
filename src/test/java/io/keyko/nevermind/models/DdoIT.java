package io.keyko.nevermind.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.net.URI;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;

public class DdoIT {

    private static final Logger log = LogManager.getLogger(DdoIT.class);

    private static final String DDO_COMPUTING_EXAMPLE_URL = "https://github.com/keyko-io/nevermind-docs/raw/master/architecture/specs/examples/compute/v0.1/ddo.computing.json";
    private static final String DDO_WORKFLOW_EXAMPLE_URL = "https://github.com/keyko-io/nevermind-docs/raw/master/architecture/specs/examples/compute/v0.1/ddo.workflow.json";

    private static final String ACCESS_DATASET_EXAMPLE_URL = "https://github.com/keyko-io/nevermind-docs/raw/master/architecture/specs/examples/metadata/v0.1/ddo-example-access.json";
    private static final String ACCESS_ALGORITHM_EXAMPLE_URL = "https://github.com/keyko-io/nevermind-docs/raw/master/architecture/specs/examples/metadata/v0.1/ddo-example-algorithm.json";
    private static final String ACCESS_WORKFLOW_EXAMPLE_URL = "https://github.com/keyko-io/nevermind-docs/raw/master/architecture/specs/examples/metadata/v0.1/ddo-example-workflow.json";
    private static final String ACCESS_SERVICE_EXAMPLE_URL = "https://github.com/keyko-io/nevermind-docs/raw/master/architecture/specs/examples/metadata/v0.1/ddo-example-service.json";

    private static  String ACCESS_DATASET_EXAMPLE_CONTENT;
    private static  String ACCESS_ALGORITHM_EXAMPLE_CONTENT;
    private static  String COMPUTE_COMPUTING_EXAMPLE_CONTENT;
    private static  String COMPUTE_WORKFLOW_EXAMPLE_CONTENT;
    private static  String ACCESS_WORKFLOW_EXAMPLE_CONTENT;
    private static  String ACCESS_SERVICE_EXAMPLE_CONTENT;

    private static final Config config = ConfigFactory.load();

    private static Credentials credentials;


    @BeforeClass
    public static void setUp() throws Exception {

        ACCESS_DATASET_EXAMPLE_CONTENT = IOUtils.toString(new URI(ACCESS_DATASET_EXAMPLE_URL), "utf-8");
        ACCESS_ALGORITHM_EXAMPLE_CONTENT = IOUtils.toString(new URI(ACCESS_ALGORITHM_EXAMPLE_URL), "utf-8");
        COMPUTE_COMPUTING_EXAMPLE_CONTENT = IOUtils.toString(new URI(DDO_COMPUTING_EXAMPLE_URL), "utf-8");
        COMPUTE_WORKFLOW_EXAMPLE_CONTENT = IOUtils.toString(new URI(DDO_WORKFLOW_EXAMPLE_URL), "utf-8");

        ACCESS_WORKFLOW_EXAMPLE_CONTENT = IOUtils.toString(new URI(ACCESS_WORKFLOW_EXAMPLE_URL), "utf-8");
        ACCESS_SERVICE_EXAMPLE_CONTENT = IOUtils.toString(new URI(ACCESS_SERVICE_EXAMPLE_URL), "utf-8");

        credentials = WalletUtils.loadCredentials(
                config.getString("account.main.password"),
                config.getString("account.main.credentialsFile"));
    }

    @Test
    public void testDDOServicesOrder() throws Exception {

        log.debug("testDDOServicesOrder");
        DDO ddoFromJson = DDO.fromJSON(new TypeReference<DDO>() {}, ACCESS_DATASET_EXAMPLE_CONTENT);
        DDO ddo= ddoFromJson.integrityBuilder(credentials);

        Assert.assertEquals("metadata", ddo.services.get(0).type);
        Assert.assertEquals("access", ddo.services.get(1).type);

        Assert.assertEquals(0, ddo.services.get(0).index);
        Assert.assertEquals(1, ddo.services.get(1).index);
    }

    @Test
    public void testAccessDatasetMetadata() throws Exception {

        log.debug("testAccessDatasetMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, ACCESS_DATASET_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(2, checksums.size());
        assertEquals("9ca5f006901cced5d5b02fd3691b12b619d6a083bb3fca39b8c90bd60f194cf9", checksums.get("0"));
        assertEquals("4294cb191438237940fe389fee1dcb5a0806432ac92622d50375a01d3406fa62", checksums.get("1"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:nv:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }

    @Test
    public void testAccessAlgorithmMetadata() throws Exception {

        log.debug("testAccessAlgorithmMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, ACCESS_ALGORITHM_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(1, checksums.size());
        assertEquals("955892c1ed06ce0f0c6dd05b73f134d56028f873cad393b885425143122b635d", checksums.get("0"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:nv:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }

    @Test
    public void testDDOComputingMetadata() throws Exception {

        log.debug("testDDOComputingMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, COMPUTE_COMPUTING_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(2, checksums.size());
        assertEquals("574b45e304c190c293855548f19b02ad0c826aee4b76fb3b3c25b27d8317efd7", checksums.get("0"));
        assertEquals("603317133c7949ea3ba57255dcdb2a44b3427aea6af3dd358a19a0c9ce4193d3", checksums.get("2"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:nv:3ea445410f608e6453cdcb7dbe42d57a89aca018993d7e87da85993cbccc6308", did.did);

    }

    @Test
    public void testComputeWorkflowMetadata() throws Exception {

        log.debug("testComputeWorkflowMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, COMPUTE_WORKFLOW_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(1, checksums.size());
        assertEquals("4c17580e13c74d5244dfe5bc574595978e43d9a544e8dd9287707d01a4a9687c", checksums.get("0"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:nv:3ea445410f608e6453cdcb7dbe42d57a89aca018993d7e87da85993cbccc6308", did.did);

    }


    @Test
    public void testAccessWorkflowMetadata() throws Exception {

        log.debug("testAccessWorkflowMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, ACCESS_WORKFLOW_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(1, checksums.size());
        assertEquals("3c52cc15f444afcdace3643358212bb3b5dcc2bcda7bd16fe5624c75f1509c98", checksums.get("0"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:nv:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }

    @Test
    public void testAccessServiceMetadata() throws Exception {

        log.debug("testAccessServiceMetadata");
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, ACCESS_SERVICE_EXAMPLE_CONTENT);
        SortedMap<String, String> checksums = ddo.generateChecksums();
        assertEquals(2, checksums.size());
        assertEquals("a80a2e50a3ad5c50a6cb111cf34cab806ebc2440eb13fe50744ecbee52c13807", checksums.get("0"));
        assertEquals("4294cb191438237940fe389fee1dcb5a0806432ac92622d50375a01d3406fa62", checksums.get("1"));

        DID did = DID.builder(ddo.toJson(ddo.proof.checksum));
        log.debug("Did generated from checksums: " + did.did);
        assertEquals(64, did.getHash().length());
        assertEquals("did:nv:138fccf336883ae6312c9b8b375745a90be369454080e90985fb3e314ab0df25", did.did);

    }


}
