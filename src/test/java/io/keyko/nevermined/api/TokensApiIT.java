package io.keyko.nevermined.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.Balance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TokensApiIT {

    private static final Logger log = LogManager.getLogger(TokensApiIT.class);

    private static NeverminedAPI neverminedAPI;
    private static Config config;

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();
        neverminedAPI = NeverminedAPI.getInstance(config);

        assertNotNull(neverminedAPI.getAccountsAPI());
        assertNotNull(neverminedAPI.getMainAccount());

    }

    @Test
    public void transfer() throws Exception {

        String receiverAddress= config.getString("account.parity.address2");
        String receiverPasswd= config.getString("account.parity.password2");

        Account receiverAccount= new Account(receiverAddress, receiverPasswd);

        neverminedAPI.getTokensAPI().request(BigInteger.ONE);
        Balance balanceBefore = neverminedAPI.getAccountsAPI().balance(receiverAccount);
        assertNotNull(balanceBefore);

        neverminedAPI.getTokensAPI().transfer(receiverAddress, BigInteger.ONE);

        Balance balanceAfter = neverminedAPI.getAccountsAPI().balance(receiverAccount);

        log.debug("Balance Before is: " + balanceBefore);
        log.debug("Balance After is: " + balanceAfter);

        assertEquals(-1, balanceBefore.getNeverminedTokens().compareTo(balanceAfter.getNeverminedTokens()));

    }

    @Test
    public void requestTokens() throws Exception {

        BigInteger tokens = BigInteger.ONE;

        Balance balanceBefore = neverminedAPI.getAccountsAPI().balance(neverminedAPI.getMainAccount());
        log.debug("Balance before: " + balanceBefore.toString());

        TransactionReceipt receipt = neverminedAPI.getTokensAPI().request(tokens);

        assertTrue(receipt.isStatusOK());

        Balance balanceAfter = neverminedAPI.getAccountsAPI().balance(neverminedAPI.getMainAccount());

        log.debug("Balance after: " + balanceAfter.toString());

        BigDecimal before= balanceBefore.getNeverminedTokens();
        BigDecimal after= balanceAfter.getNeverminedTokens();
        assertEquals(0, after.compareTo(before.add(BigDecimal.ONE)));
    }

}
