package io.keyko.nevermined.manager;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthAccounts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountsManagerTest {

    private static final Logger log = LogManager.getLogger(AccountsManagerTest.class);

    private static MetadataApiService metadataApiService;
    private static final Config config = ConfigFactory.load();

    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        metadataApiService = ManagerHelper.getMetadataService(config);
    }

    @Test
    public void getAccounts()  throws IOException, CipherException, EthereumException {
        List<String> expectedAccounts= new ArrayList<>();
        expectedAccounts.add("0x123");
        expectedAccounts.add("0x456");
        expectedAccounts.add("0x789");

        KeeperService _keeper= mock(KeeperService.class);
        Admin _web3j= mock(Admin.class);
        Credentials _credentials= mock(Credentials.class);

        Request<?, EthAccounts> _request= (Request<?, EthAccounts>) mock(Request.class);
        EthAccounts _response= mock(EthAccounts.class);

        when(_response.getAccounts()).thenReturn(expectedAccounts);
        when(_request.send()).thenReturn(_response);
        Mockito.doReturn(_request).when(_web3j).ethAccounts();
        when(_keeper.getWeb3()).thenReturn(_web3j);
        when(_keeper.getCredentials()).thenReturn(_credentials);

        AccountsManager fakeManager= AccountsManager.getInstance(_keeper, metadataApiService);

        List<Account> accounts= fakeManager.getAccounts();

        assertTrue(accounts.size() == expectedAccounts.size());
        assertTrue(accounts.get(0).address.startsWith("0x"));
    }

}