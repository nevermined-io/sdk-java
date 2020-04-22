package io.keyko.nevermind.api;

import io.keyko.common.helpers.CryptoHelper;
import io.keyko.nevermind.exceptions.EthereumException;
import io.keyko.nevermind.models.service.template.TemplateSEA;
import io.keyko.nevermind.contracts.TemplateStoreManager;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermind.manager.ManagerHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
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



        String templateAddress= "0x0990484293948238943289428394328943234233";

        BigInteger numberTemplates= oceanAPI.getTemplatesAPI().getListSize();
        log.debug("Number of existing templates: " + numberTemplates.toString());

        log.debug("Proposing template: " + templateAddress);
        oceanAPI.getTemplatesAPI().propose(templateAddress);


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

        assertEquals(0, oceanAPI.getTemplatesAPI().getTemplate(templateAddress)
                .state.compareTo(TemplateSEA.TemplateState.Revoked.getStatus()));
    }

    @Test(expected = EthereumException.class)
    public void templatesLifecycleException() throws Exception {

        String templateAddress= "0x0000000000000000000000000000aaaaaaaaaaaa";

        log.debug("Approving a no existing template: " + templateAddress);
        oceanAPI.getTemplatesAPI().approve(templateAddress);
    }




}
