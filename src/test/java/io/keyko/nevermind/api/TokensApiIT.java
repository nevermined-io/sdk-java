package io.keyko.nevermind.api;

import io.keyko.nevermind.models.Account;
import io.keyko.nevermind.models.Balance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

    private static NevermindAPI nevermindAPI;
    private static Config config;

    @BeforeClass
    public static void setUp() throws Exception {


        config = ConfigFactory.load();
        nevermindAPI = NevermindAPI.getInstance(config);

        assertNotNull(nevermindAPI.getAccountsAPI());
        assertNotNull(nevermindAPI.getMainAccount());

    }

    @Test
    public void transfer() throws Exception {

        String receiverAddress= config.getString("account.parity.address2");
        String receiverPasswd= config.getString("account.parity.password2");

        Account receiverAccount= new Account(receiverAddress, receiverPasswd);

        nevermindAPI.getTokensAPI().request(BigInteger.ONE);
        Balance balanceBefore = nevermindAPI.getAccountsAPI().balance(receiverAccount);
        assertNotNull(balanceBefore);

        nevermindAPI.getTokensAPI().transfer(receiverAddress, BigInteger.ONE);

        Balance balanceAfter = nevermindAPI.getAccountsAPI().balance(receiverAccount);

        log.debug("Balance Before is: " + balanceBefore);
        log.debug("Balance After is: " + balanceAfter);

        assertEquals(-1, balanceBefore.getOceanTokens().compareTo(balanceAfter.getOceanTokens()));

    }

    @Test
    public void requestTokens() throws Exception {

        BigInteger tokens = BigInteger.ONE;

        Balance balanceBefore = nevermindAPI.getAccountsAPI().balance(nevermindAPI.getMainAccount());
        log.debug("Balance before: " + balanceBefore.toString());

        TransactionReceipt receipt = nevermindAPI.getTokensAPI().request(tokens);

        assertTrue(receipt.isStatusOK());

        Balance balanceAfter = nevermindAPI.getAccountsAPI().balance(nevermindAPI.getMainAccount());

        log.debug("Balance after: " + balanceAfter.toString());

        BigDecimal before= balanceBefore.getOceanTokens();
        BigDecimal after= balanceAfter.getOceanTokens();
        assertEquals(0, after.compareTo(before.add(BigDecimal.ONE)));
    }

}
