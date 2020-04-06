/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.squid.exceptions.DIDFormatException;
import com.oceanprotocol.squid.models.service.Service;
import com.oceanprotocol.squid.models.service.types.AccessService;
import com.oceanprotocol.squid.models.service.types.AuthorizationService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

import static com.oceanprotocol.squid.models.AbstractModel.DATE_FORMAT;
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
        assertEquals("did:op:123", DID.getFromHash("123").toString());
    }

    @Test(expected = DIDFormatException.class)
    public void badDID() throws Exception {
        new DID("did:kkdid:123");
    }

    @Test
    public void generateDID() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        assertTrue(ddo.id.startsWith(DID.PREFIX));
        assertEquals(64, ddo.getDid().getHash().length());
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
        SortedMap<String, String> checksums = ddo.generateChecksums();

        assertEquals(2, checksums.size());

    }

    @Test
    public void checkDateFormat() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        log.debug("Date found: " + DATE_FORMAT.format(ddo.created));
        log.debug("Date String: " + ddo.created.toString());
        assertTrue(DATE_FORMAT.format(ddo.created).startsWith("20"));
        assertTrue(DATE_FORMAT.format(ddo.updated).startsWith("20"));

        DDO newDDO= new DDO();
        log.debug("Date found: " + DATE_FORMAT.format(newDDO.created));
        assertTrue(DATE_FORMAT.format(newDDO.created).startsWith("20"));
        assertTrue(DATE_FORMAT.format(newDDO.updated).startsWith("20"));
    }



    @Test
    public void testWorkflow() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_WORKFLOW_CONTENT);

        Service metadataService = ddo.getMetadataService();

        assertEquals("workflow", metadataService.attributes.main.type);
        assertEquals("tensorflow/tensorflow", metadataService.attributes.main.workflow.stages.get(0).requirements.container.image);
        assertEquals("latest",  metadataService.attributes.main.workflow.stages.get(0).requirements.container.tag);
        assertEquals("sha256:cb57ecfa6ebbefd8ffc7f75c0f00e57a7fa739578a429b6f72a0df19315deadc",
                metadataService.attributes.main.workflow.stages.get(0).requirements.container.checksum);

        assertEquals(2,  metadataService.attributes.main.workflow.stages.get(0).input.size());
        assertEquals(0,  metadataService.attributes.main.workflow.stages.get(0).input.get(0).index.intValue());
        assertEquals("did:op:12345",  metadataService.attributes.main.workflow.stages.get(0).input.get(0).id.toString());
        assertEquals("did:op:abcde",  metadataService.attributes.main.workflow.stages.get(0).transformation.id.toString());
        assertEquals("https://localhost:5000/api/v1/aquarius/assets/ddo/",
                metadataService.attributes.main.workflow.stages.get(0).output.metadataUrl);
    }

    @Test
    public void testAlgorithm() throws Exception {
        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_ALGORITHM_CONTENT);

        Service metadataService = ddo.getMetadataService();

        assertEquals("algorithm",  metadataService.attributes.main.type);
        assertEquals("scala", metadataService.attributes.main.algorithm.language);
        assertEquals("jar", metadataService.attributes.main.algorithm.format);
        assertEquals("0.1", metadataService.attributes.main.algorithm.version);

        assertEquals(2, metadataService.attributes.main.algorithm.requirements.size());
        assertEquals("scala", metadataService.attributes.main.algorithm.requirements.get(0).requirement);
        assertEquals("1.8", metadataService.attributes.main.algorithm.requirements.get(1).version);

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

        DDO newDDO= ddo.integrityBuilder(credentials);
        log.debug("DDO generated with DID: " + newDDO.getDid().did);

        log.debug(ddo.toJson(newDDO.proof));
        assertEquals(2, newDDO.proof.checksum.size());
        assertEquals(64 + DID.PREFIX.length(), newDDO.getDid().did.length());
        assertEquals(newDDO.id, newDDO.getDid().did);
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
        assertEquals("did:op:0bc278fee025464f8012b811d1bce8e22094d0984e4e49139df5d5ff7a028bdf", ddo.id.toString());
        assertEquals(3, ddo.publicKeys.size());
        assertTrue(ddo.publicKeys.get(0).id.startsWith("did:op:b6e2eb5eff1a093ced9826315d5a4ef6c5b5c8bd3c49890ee284231d7e1d0aaa"));

        assertEquals(1, ddo.authentication.size());
        assertTrue(ddo.authentication.get(0).publicKey.startsWith("did:op:0ebed8226ada17fde24b6bf2b95d27f8f05fcce09139ff5cec31f6d81a7cd2ea"));

        assertEquals(2, ddo.services.size());
        assertTrue(ddo.services.get(1).serviceEndpoint.startsWith("http"));

    }

    @Test
    public void jsonToModelWithAuth() throws Exception {

        DDO ddo = DDO.fromJSON(new TypeReference<DDO>() {
        }, DDO_JSON_AUTH_CONTENT);

        assertEquals("https://w3id.org/did/v1", ddo.context);
        assertEquals("did:op:0bc278fee025464f8012b811d1bce8e22094d0984e4e49139df5d5ff7a028bdf", ddo.id.toString());
        assertEquals(3, ddo.publicKeys.size());
        assertTrue(ddo.publicKeys.get(0).id.startsWith("did:op:b6e2eb5eff1a093ced9826315d5a4ef6c5b5c8bd3c49890ee284231d7e1d0aaa"));

        assertEquals(1, ddo.authentication.size());
        assertTrue(ddo.authentication.get(0).publicKey.startsWith("did:op:0ebed8226ada17fde24b6bf2b95d27f8f05fcce09139ff5cec31f6d81a7cd2ea"));

        assertEquals(3, ddo.services.size());

        AuthorizationService authorizationService = ddo.getAuthorizationService();
        assertEquals("http://localhost:12001", authorizationService.serviceEndpoint);
        assertEquals(Service.ServiceTypes.authorization.name(), authorizationService.type);
    }

    @Test
    public void modelToJson() throws Exception {
        String did = "did:op:12345";
        DDO ddo = new DDO();

        DDO.PublicKey pk = new DDO.PublicKey();
        pk.id = did;
        pk.type = "RsaVerificationKey2018";
        pk.owner = did + "owner";

        ddo.publicKeys.add(pk);
        ddo.publicKeys.add(pk);

        DDO.Authentication auth = new DDO.Authentication(did);
        auth.type = "AuthType";
        auth.publicKey = "AuthPK";

        ddo.authentication.add(auth);
        ddo.authentication.add(auth);
        ddo.authentication.add(auth);

        Service metadataService = new Service(Service.ServiceTypes.metadata, "http://disney.com", 0);
        metadataService.attributes.main.name = "test name";

        AccessService accessService = new AccessService("http://ocean.com", 1, "0x00000000");

        ddo.services.add(metadataService);
        ddo.services.add(accessService);


        String modelJson = ddo.toJson();
        log.debug(modelJson);

        JSONObject json = new JSONObject(modelJson);
        assertEquals(2, (json.getJSONArray("publicKey").length()));
        assertEquals(did, ((JSONObject) (json.getJSONArray("publicKey").get(0))).getString("id"));

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