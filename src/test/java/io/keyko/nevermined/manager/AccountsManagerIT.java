package io.keyko.nevermined.manager;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.contracts.Dispenser;
import io.keyko.nevermined.contracts.OceanToken;
import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.Balance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountsManagerIT {

    private static final Logger log = LogManager.getLogger(AccountsManagerIT.class);

    private static AccountsManager manager;
    private static AccountsManager managerError;
    private static KeeperService keeper;
    private static KeeperService keeperError;
    private static MetadataApiService metadataApiService;

    private static OceanToken oceanToken;
    private static Dispenser dispenser;

    private static final Config config = ConfigFactory.load();
    private static String TEST_ADDRESS;

    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        keeper= ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity);

        metadataApiService = ManagerHelper.getMetadataService(config);
        manager= AccountsManager.getInstance(keeper, metadataApiService);

        // Loading Smart Contracts required
        oceanToken= ManagerHelper.loadOceanTokenContract(keeper, config.getString("contract.OceanToken.address"));
        dispenser= ManagerHelper.loadDispenserContract(keeper, config.getString("contract.Dispenser.address"));
        manager.setTokenContract(oceanToken);
        manager.setDispenserContract(dispenser);

        TEST_ADDRESS= config.getString("account.parity.address");

    }

    @Test
    public void getInstance() {
        // Checking if web3j driver included in KeeperService implements the Web3j interface
        Class[] interfaces = manager.getKeeperService().getWeb3().getClass().getInterfaces();
        boolean implementsClass = false;
        for (Class c : interfaces) {
            if ("Web3j".equals(c.getSimpleName())) {
                implementsClass = true;
            }
        }
        assertTrue(implementsClass);
        assertTrue(
                manager.getMetadataApiService().getClass().isAssignableFrom(MetadataApiService.class));
    }

    @Test
    public void getAccounts() throws IOException, EthereumException {
        List<Account> accounts= manager.getAccounts();
        assertTrue(accounts.size()>0);
    }

    @Test
    public void getAccountsBalance() throws EthereumException {
        manager.requestTokens(BigInteger.TEN);
        log.debug("OceanToken Address: " + manager.tokenContract.getContractAddress());

        log.debug("Requesting " + BigInteger.ONE + " ocean tokens for " + TEST_ADDRESS);

        Balance balance= manager.getAccountBalance(TEST_ADDRESS);

        log.debug("OCEAN Balance is " + balance.getDrops().toString());
        log.debug("ETH balance is " + balance.getEth().toString());

        assertEquals(1, balance.getEth().compareTo(BigInteger.ZERO));
        assertEquals(1, balance.getDrops().compareTo(BigInteger.ZERO));

    }

    @Test(expected = EthereumException.class)
    public void getAccountsException() throws IOException, EthereumException, CipherException {
        Config badConfig= config.withValue(
                "keeper.url", ConfigValueFactory.fromAnyRef("http://fdasdfsa.dasx:8545"));

        keeperError= ManagerHelper.getKeeper(badConfig, ManagerHelper.VmClient.parity);
        managerError= AccountsManager.getInstance(keeperError, metadataApiService);
        managerError.setTokenContract(
                ManagerHelper.loadOceanTokenContract(keeperError, config.getString("contract.OceanToken.address"))
        );
        managerError.setDispenserContract(
                ManagerHelper.loadDispenserContract(keeperError, config.getString("contract.Dispenser.address"))
        );

        List<Account> accounts= managerError.getAccounts();
        assertTrue(accounts.size()>0);
    }

    @Test(expected = EthereumException.class)
    public void getAccountsBalanceError() throws EthereumException, CipherException, IOException {
        Config badConfig= config.withValue(
                "keeper.url", ConfigValueFactory.fromAnyRef("http://fdasdfsa.dasx:8545"));

        keeperError= ManagerHelper.getKeeper(badConfig, ManagerHelper.VmClient.parity);
        managerError= AccountsManager.getInstance(keeperError, metadataApiService);
        managerError.setTokenContract(
                ManagerHelper.loadOceanTokenContract(keeperError, config.getString("contract.OceanToken.address"))
        );
        managerError.setDispenserContract(
                ManagerHelper.loadDispenserContract(keeperError, config.getString("contract.Dispenser.address"))
        );

        managerError.requestTokens(BigInteger.valueOf(100));

        managerError.getAccountBalance(TEST_ADDRESS);
    }

}