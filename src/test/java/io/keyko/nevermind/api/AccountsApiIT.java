package io.keyko.nevermind.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermind.models.Account;
import io.keyko.nevermind.models.Balance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

public class AccountsApiIT {

    private static NevermindAPI nevermindAPI;
    private static final Logger log = LogManager.getLogger(AccountsApiIT.class);

    @BeforeClass
    public static void setUp() throws Exception {

        Config config = ConfigFactory.load();
        nevermindAPI = NevermindAPI.getInstance(config);

        assertNotNull(nevermindAPI.getAccountsAPI());
        assertNotNull(nevermindAPI.getMainAccount());

    }

    @Test
    public void list() throws Exception {

        List<Account> accounts = nevermindAPI.getAccountsAPI().list();
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
    }

    @Test
    public void balance() throws Exception {

        Balance balance = nevermindAPI.getAccountsAPI().balance(nevermindAPI.getMainAccount());
        assertNotNull(balance);

        log.debug("Balance of " + nevermindAPI.getMainAccount().address);
        log.debug("Eth: " + balance.getEth());
        log.debug("Drops: " + balance.getDrops());

        assertEquals(1, balance.getEth().compareTo(BigInteger.ZERO));
        assertEquals(1, balance.getDrops().compareTo(BigInteger.ZERO));
    }

}
