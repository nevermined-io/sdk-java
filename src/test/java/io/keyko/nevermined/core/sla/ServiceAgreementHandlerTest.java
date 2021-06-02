package io.keyko.nevermined.core.sla;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.manager.ManagerHelper;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.DDO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;

import java.nio.file.Files;
import java.nio.file.Paths;

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

}