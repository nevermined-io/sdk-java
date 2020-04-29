package io.keyko.nevermined.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.Balance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

public class AccountsApiIT {

    private static NeverminedAPI neverminedAPI;
    private static final Logger log = LogManager.getLogger(AccountsApiIT.class);

    @BeforeClass
    public static void setUp() throws Exception {

        Config config = ConfigFactory.load();
        neverminedAPI = NeverminedAPI.getInstance(config);

        assertNotNull(neverminedAPI.getAccountsAPI());
        assertNotNull(neverminedAPI.getMainAccount());

    }

    @Test
    public void list() throws Exception {

        List<Account> accounts = neverminedAPI.getAccountsAPI().list();
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
    }

    @Test
    public void balance() throws Exception {

        Balance balance = neverminedAPI.getAccountsAPI().balance(neverminedAPI.getMainAccount());
        assertNotNull(balance);

        log.debug("Balance of " + neverminedAPI.getMainAccount().address);
        log.debug("Eth: " + balance.getEth());
        log.debug("Drops: " + balance.getDrops());

        assertEquals(1, balance.getEth().compareTo(BigInteger.ZERO));
        assertEquals(1, balance.getDrops().compareTo(BigInteger.ZERO));
    }

}
