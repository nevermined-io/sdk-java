/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.core.sla;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.squid.manager.ManagerHelper;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.service.types.AccessService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.web3j.crypto.Hash;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ServiceAgreementHandlerTest {

    private static final Logger log= LogManager.getLogger(ServiceAgreementHandlerTest.class);

    private static final Config config = ConfigFactory.load();
    private static KeeperService keeper;
    private static Account account;

    private static final String TEMPLATE_ID= "";
    private static final String SERVICEAGREEMENT_ID= "0xf136d6fadecb48fdb2fc1fb420f5a5d1c32d22d9424e47ab9461556e058fefaa";

    private static final String EXPECTED_HASH= "0x66652d0f8f8ec464e67aa6981c17fa1b1644e57d9cfd39b6f1b58ad1b71d61bb";
    private static final String EXPECTED_SIGNATURE= "0x28fbc30b05fe7caf6d8082778ef3aabd17ceeb31d1bba2908354f999da55bb1878d7e2f7242f591d112fe7e93f9a98ef7d7a85af76e54c53f0b8ee5ced96e4271b";

    private static final String DDO_JSON_SAMPLE = "src/test/resources/examples/ddo-generated-example-2.json";

    private static String jsonContent;
    private static DDO ddo;

    @BeforeClass
    public static void setUp() throws Exception {
        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity);

        String accountAddress = config.getString("account." + ManagerHelper.VmClient.parity.toString() + ".address");
        String accountPassword = config.getString("account." + ManagerHelper.VmClient.parity.toString() + ".password");

        account = new Account(accountAddress, accountPassword);

        jsonContent = new String(Files.readAllBytes(Paths.get(DDO_JSON_SAMPLE)));
        ddo= DDO.fromJSON(new TypeReference<DDO>() {}, jsonContent);

    }


    @Test
    public void generateConditionIds()  throws Exception {

        String agreementId ="0xc4a15b18fcf343a4b2cda85800a454f6153c1478db8f4b7bacba3ef21129cc26";
        String consumerAddress = "0x068Ed00cF0441e4829D9784fCBe7b9e26D4BD8d0";
        String publisherAddress = "0x00Bd138aBD70e2F00903268F3Db08f2D25677C9e";
        String escrowRewardConditionAddress = "0xe22570D8ea2D8004023A928FbEb36f14738C62c9";
        String lockRewardConditionAddress = "0x7BE62247c8ea441947e1e3350496401E6523dBc4";
        String accessSecretStoreConditionAddress = "0xb29Dd6383fb5786fea1172026CC181Ff5Cc553B9";

        AccessService accessService= (AccessService) ddo.services.get(1);

        String lockRewardId = accessService.generateLockRewardId(agreementId, escrowRewardConditionAddress, lockRewardConditionAddress);
        assertEquals("0x816926c38a73dae566c2b539a2cbac9ff36cda7573fc3b7f30b49a8e5bdfb21c", lockRewardId);

        String accessSecretStoreId = accessService.generateAccessSecretStoreConditionId(agreementId, consumerAddress, accessSecretStoreConditionAddress);
        assertEquals("0x55301caec9ece3cc2e9540f6711e6d8cf28842e4f0d84b7e4a7930a64ca7a18b", accessSecretStoreId);

        String escrowRewardId = accessService.generateEscrowRewardConditionId(agreementId, consumerAddress, publisherAddress, escrowRewardConditionAddress, lockRewardId, accessSecretStoreId);
        assertEquals("0x1792957bac8bf96011ced782d28281bce5a17d8d07ba7f6d3c5474201087c62f", escrowRewardId);

    }


    @Test
    public void generateSASignature()  throws Exception {

        String agreementId ="0xc4a15b18fcf343a4b2cda85800a454f6153c1478db8f4b7bacba3ef21129cc26";
        String consumerAddress = "0x068Ed00cF0441e4829D9784fCBe7b9e26D4BD8d0";
        String publisherAddress = "0x00Bd138aBD70e2F00903268F3Db08f2D25677C9e";
        String escrowRewardConditionAddress = "0xe22570D8ea2D8004023A928FbEb36f14738C62c9";
        String lockRewardConditionAddress = "0x7BE62247c8ea441947e1e3350496401E6523dBc4";
        String accessSecretStoreConditionAddress = "0xb29Dd6383fb5786fea1172026CC181Ff5Cc553B9";

        AccessService accessService= (AccessService) ddo.services.get(1);
        accessService.templateId = "0x044852b2a670ade5407e78fb2863c51de9fcb96542a07186fe3aeda6bb8a116d";

        Map<String, String> conditionsAddresses = new HashMap<>();
        conditionsAddresses.put("escrowRewardAddress", escrowRewardConditionAddress);
        conditionsAddresses.put("lockRewardConditionAddress", lockRewardConditionAddress);
        conditionsAddresses.put("accessSecretStoreConditionAddress", accessSecretStoreConditionAddress);

        String hash= accessService.generateServiceAgreementHash(agreementId, consumerAddress, publisherAddress,conditionsAddresses);
        assertEquals("0xa25575970920e439cc076f1489e0b820cb2fd91b7a8643165fd26d296fa69ee6", hash);

    }

    @Ignore
    @Test
    public void generateServiceAgreementSignature() throws Exception {


        AccessService accessService= (AccessService) ddo.services.get(1);

        Map<String, String> conditionsAddresses = new HashMap<>();
        conditionsAddresses.put("escrowRewardAddress", "escrowRewardAddredd");
        conditionsAddresses.put("lockRewardConditionAddress", "lockRewardAddress");
        conditionsAddresses.put("accessSecretStoreConditionAddress", "accessSecretStoreADdress");

        String hash= accessService.generateServiceAgreementHash(SERVICEAGREEMENT_ID, "consumerAddress", "publisherAddress",  conditionsAddresses);
        String signature= accessService.generateServiceAgreementSignatureFromHash(keeper.getWeb3(), keeper.getAddress(), account.password, hash);

        final String hashTemplateId= Hash.sha3(TEMPLATE_ID);
        //final String hashConditionKeys= Hash.sha3256(accessService.fetchConditionKeys());
        final String hashConditionValues= Hash.sha3(accessService.fetchConditionValues());
        final String hashTimeouts= Hash.sha3(accessService.fetchTimeout());
        final String hashServiceAgreementId= Hash.sha3(SERVICEAGREEMENT_ID);

        log.debug("Hash templateId: " + hashTemplateId);
        //log.debug("Hash conditionKeys: " + hashConditionKeys);
        log.debug("Hash conditionValues: " + hashConditionValues);
        log.debug("Hash Timeouts: " + hashTimeouts);
        log.debug("Hash ServiceAgreementId: " + hashServiceAgreementId);


        log.debug("\n-------\n");

        log.debug("Hash: " + hash);
        log.debug("Signature: " + signature);

        assertEquals("hashTemplateId doesn't match", "0x40105d5bc10105c17fd72b93a8f73369e2ee6eee4d4714b7bf7bf3c2f156e601", hashTemplateId);
        //assertEquals("hashConditionKeys Hash doesn't match", "0x5b0fbb997b36bcc10d1543e071c2a859fe21ad8a9f18af6bdeb366a584d091b3", hashConditionKeys);
        assertEquals("hashConditionValues doesn't match", "0xfbb8894170e025ff7aaf7c5278c16fa17f4ea3d1126623ebdac87bd91e70acc2", hashConditionValues);
        assertEquals("hashTimeouts doesn't match", "0x4a0dd5c0cd0686c8feff15f4ec2ff2b3b7009451ee56eb3d10d75d8a7da95c7f", hashTimeouts);
        assertEquals("hashServiceAgreementId doesn't match", "0x922c3379f6140ee422c40a900f23479d22737270ec1439ca87fcb321c6c0c692", hashServiceAgreementId);

        assertEquals(EXPECTED_HASH.length(), hash.length());
        assertEquals(EXPECTED_SIGNATURE.length(), signature.length());

        assertEquals("Error matching the HASH", EXPECTED_HASH, hash);
        assertEquals("Error matching the SIGNATURE", EXPECTED_SIGNATURE, signature);
    }

}