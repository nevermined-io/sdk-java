package io.keyko.ocean.api;

import io.keyko.common.helpers.CryptoHelper;
import io.keyko.ocean.keeper.contracts.TemplateStoreManager;
import io.keyko.ocean.exceptions.EthereumException;
import io.keyko.common.web3.KeeperService;
import io.keyko.ocean.manager.ManagerHelper;
import io.keyko.ocean.models.service.template.TemplateSEA;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TemplatesApiIT {

    private static final Logger log = LogManager.getLogger(TemplatesApiIT.class);

    private static KeeperService keeper;
    private static OceanAPI oceanAPI;
    private static Config config;
    private static TemplateStoreManager templateStoreManager;
    private static String owner;
    private static long timeout= 2000l;

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();

        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getTemplatesAPI());
        assertNotNull(oceanAPI.getMainAccount());

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");

        templateStoreManager= ManagerHelper.deployTemplateStoreManager(keeper);
        templateStoreManager.initialize(keeper.getAddress()).send();

        oceanAPI.setTemplateStoreManagerContract(templateStoreManager);
        owner= templateStoreManager.owner().send();

        log.debug("OceanAPI main account: " + oceanAPI.getMainAccount().getAddress());
        log.debug("TemplateStoreManager Owner: " + owner);

    }


    @Test
    public void templatesLifecycle() throws Exception {

        final String templateAddress= "0xBd7e5fFf4Eb8d67111227C9541080a74c634d643";
        final String templateName= "MyTestTemplate";

        final String escrowConditionAddress = config.getString("contract.EscrowReward.address");

        BigInteger numberTemplates= oceanAPI.getTemplatesAPI().getListSize();
        log.debug("Number of existing templates: " + numberTemplates.toString());

        log.debug("Registering actor type");
        oceanAPI.getTemplatesAPI().registerActorType("consumer");
        final byte[] consumers = templateStoreManager.getTemplateActorTypeId("consumer").send();
        assertTrue(consumers.length > 0);

        // new Bytes32(Hash.sha3(io.keyko.common.helpers.EncodingHelper.hexStringToBytes("Bd7e5fFf4Eb8d67111227C9541080a74c634d643")))
        log.debug("Proposing template: " + templateAddress);
        oceanAPI.getTemplatesAPI().propose(
                CryptoHelper.keccak256(templateAddress),
                Arrays.asList(escrowConditionAddress),
                Arrays.asList(CryptoHelper.keccak256(owner)),
                templateName);


        for (int counter= 0; counter<10; counter++) {
            log.debug("Waiting for the template proposal ...");
            TemplateSEA template= oceanAPI.getTemplatesAPI().getTemplate(templateAddress);
            if (template.state.compareTo(TemplateSEA.TemplateState.Proposed.getStatus()) == 0) {
                log.debug("Template " + templateAddress + " in Proposed state");
                break;
            }
            wait(timeout);
        }

        assertFalse(oceanAPI.getTemplatesAPI().isApproved(templateAddress));

        log.debug("Approving template: " + templateAddress);
        oceanAPI.getTemplatesAPI().approve(templateAddress);

        for (int counter= 0; counter<10; counter++) {
            log.debug("Waiting for the template approval ...");
            TemplateSEA template= oceanAPI.getTemplatesAPI().getTemplate(templateAddress);
            if (template.state.compareTo(TemplateSEA.TemplateState.Approved.getStatus()) == 0) {
                log.debug("Template " + templateAddress + " in Approved state");
                break;
            }
            wait(timeout);
        }

        assertTrue(oceanAPI.getTemplatesAPI().isApproved(templateAddress));

        log.debug("Revoking template: " + templateAddress);
        oceanAPI.getTemplatesAPI().revoke(templateAddress);

        assertFalse(oceanAPI.getTemplatesAPI().isApproved(templateAddress));

        Assert.assertEquals(0, oceanAPI.getTemplatesAPI().getTemplate(templateAddress)
                .state.compareTo(TemplateSEA.TemplateState.Revoked.getStatus()));
    }

    @Test(expected = EthereumException.class)
    public void templatesLifecycleException() throws Exception {

        String templateAddress= "0x0000000000000000000000000000aaaaaaaaaaaa";

        log.debug("Approving a no existing template: " + templateAddress);
        oceanAPI.getTemplatesAPI().approve(templateAddress);
    }




}
