package io.keyko.nevermined.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.contracts.TemplateStoreManager;
import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.manager.ManagerHelper;
import io.keyko.nevermined.models.service.template.TemplateSEA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class TemplatesApiIT {

    private static final Logger log = LogManager.getLogger(TemplatesApiIT.class);

    private static KeeperService keeper;
    private static NeverminedAPI neverminedAPI;
    private static Config config;
    private static TemplateStoreManager templateStoreManager;
    private static String owner;
    private static long timeout= 2000l;

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();

        neverminedAPI = NeverminedAPI.getInstance(config);

        assertNotNull(neverminedAPI.getTemplatesAPI());
        assertNotNull(neverminedAPI.getMainAccount());

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");

        templateStoreManager= ManagerHelper.deployTemplateStoreManager(keeper);
        templateStoreManager.initialize(keeper.getAddress()).send();

        neverminedAPI.setTemplateStoreManagerContract(templateStoreManager);
        owner= templateStoreManager.owner().send();

        log.debug("NeverminedAPI main account: " + neverminedAPI.getMainAccount().getAddress());
        log.debug("TemplateStoreManager Owner: " + owner);

    }


    @Test
    public void templatesLifecycle() throws Exception {



        String templateAddress= "0x0990484293948238943289428394328943234233";

        BigInteger numberTemplates= neverminedAPI.getTemplatesAPI().getListSize();
        log.debug("Number of existing templates: " + numberTemplates.toString());

        log.debug("Proposing template: " + templateAddress);
        neverminedAPI.getTemplatesAPI().propose(templateAddress);


        for (int counter= 0; counter<10; counter++) {
            log.debug("Waiting for the template proposal ...");
            TemplateSEA template= neverminedAPI.getTemplatesAPI().getTemplate(templateAddress);
            if (template.state.compareTo(TemplateSEA.TemplateState.Proposed.getStatus()) == 0) {
                log.debug("Template " + templateAddress + " in Proposed state");
                break;
            }
            wait(timeout);
        }

        assertFalse(neverminedAPI.getTemplatesAPI().isApproved(templateAddress));

        log.debug("Approving template: " + templateAddress);
        neverminedAPI.getTemplatesAPI().approve(templateAddress);

        for (int counter= 0; counter<10; counter++) {
            log.debug("Waiting for the template approval ...");
            TemplateSEA template= neverminedAPI.getTemplatesAPI().getTemplate(templateAddress);
            if (template.state.compareTo(TemplateSEA.TemplateState.Approved.getStatus()) == 0) {
                log.debug("Template " + templateAddress + " in Approved state");
                break;
            }
            wait(timeout);
        }

        assertTrue(neverminedAPI.getTemplatesAPI().isApproved(templateAddress));

        log.debug("Revoking template: " + templateAddress);
        neverminedAPI.getTemplatesAPI().revoke(templateAddress);

        assertFalse(neverminedAPI.getTemplatesAPI().isApproved(templateAddress));

        assertEquals(0, neverminedAPI.getTemplatesAPI().getTemplate(templateAddress)
                .state.compareTo(TemplateSEA.TemplateState.Revoked.getStatus()));
    }

    @Test(expected = EthereumException.class)
    public void templatesLifecycleException() throws Exception {

        String templateAddress= "0x0000000000000000000000000000aaaaaaaaaaaa";

        log.debug("Approving a no existing template: " + templateAddress);
        neverminedAPI.getTemplatesAPI().approve(templateAddress);
    }




}
