package io.keyko.nevermind.api;

import io.keyko.nevermind.exceptions.EthereumException;
import io.keyko.nevermind.models.service.template.TemplateSEA;
import io.keyko.nevermind.contracts.TemplateStoreManager;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermind.manager.ManagerHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class TemplatesApiIT {

    private static final Logger log = LogManager.getLogger(TemplatesApiIT.class);

    private static KeeperService keeper;
    private static NevermindAPI nevermindAPI;
    private static Config config;
    private static TemplateStoreManager templateStoreManager;
    private static String owner;
    private static long timeout= 2000l;

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();

        nevermindAPI = NevermindAPI.getInstance(config);

        assertNotNull(nevermindAPI.getTemplatesAPI());
        assertNotNull(nevermindAPI.getMainAccount());

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");

        templateStoreManager= ManagerHelper.deployTemplateStoreManager(keeper);
        templateStoreManager.initialize(keeper.getAddress()).send();

        nevermindAPI.setTemplateStoreManagerContract(templateStoreManager);
        owner= templateStoreManager.owner().send();

        log.debug("NevermindAPI main account: " + nevermindAPI.getMainAccount().getAddress());
        log.debug("TemplateStoreManager Owner: " + owner);

    }


    @Test
    public void templatesLifecycle() throws Exception {



        String templateAddress= "0x0990484293948238943289428394328943234233";

        BigInteger numberTemplates= nevermindAPI.getTemplatesAPI().getListSize();
        log.debug("Number of existing templates: " + numberTemplates.toString());

        log.debug("Proposing template: " + templateAddress);
        nevermindAPI.getTemplatesAPI().propose(templateAddress);


        for (int counter= 0; counter<10; counter++) {
            log.debug("Waiting for the template proposal ...");
            TemplateSEA template= nevermindAPI.getTemplatesAPI().getTemplate(templateAddress);
            if (template.state.compareTo(TemplateSEA.TemplateState.Proposed.getStatus()) == 0) {
                log.debug("Template " + templateAddress + " in Proposed state");
                break;
            }
            wait(timeout);
        }

        assertFalse(nevermindAPI.getTemplatesAPI().isApproved(templateAddress));

        log.debug("Approving template: " + templateAddress);
        nevermindAPI.getTemplatesAPI().approve(templateAddress);

        for (int counter= 0; counter<10; counter++) {
            log.debug("Waiting for the template approval ...");
            TemplateSEA template= nevermindAPI.getTemplatesAPI().getTemplate(templateAddress);
            if (template.state.compareTo(TemplateSEA.TemplateState.Approved.getStatus()) == 0) {
                log.debug("Template " + templateAddress + " in Approved state");
                break;
            }
            wait(timeout);
        }

        assertTrue(nevermindAPI.getTemplatesAPI().isApproved(templateAddress));

        log.debug("Revoking template: " + templateAddress);
        nevermindAPI.getTemplatesAPI().revoke(templateAddress);

        assertFalse(nevermindAPI.getTemplatesAPI().isApproved(templateAddress));

        assertEquals(0, nevermindAPI.getTemplatesAPI().getTemplate(templateAddress)
                .state.compareTo(TemplateSEA.TemplateState.Revoked.getStatus()));
    }

    @Test(expected = EthereumException.class)
    public void templatesLifecycleException() throws Exception {

        String templateAddress= "0x0000000000000000000000000000aaaaaaaaaaaa";

        log.debug("Approving a no existing template: " + templateAddress);
        nevermindAPI.getTemplatesAPI().approve(templateAddress);
    }




}
