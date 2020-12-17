package io.keyko.nevermined.api.helper;

import org.junit.Test;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.junit.Assert.*;

public class AccountsHelperTest {

    static String PASSWORD = null; // no encryption
    static String MNEMONIC  = "taxi music thumb unique chat sand crew more leg another off lamp";


    @Test
    public void createAccount() throws IOException, CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        final String walletFile = AccountsHelper.createAccount("1234", "/tmp");
        final String address = AccountsHelper.getAddressFromFilePath(walletFile);
        assertTrue(AccountsHelper.isValidAddress(address));
    }

    @Test
    public void loadCredentialsFromMnemonic() {

        final Credentials credentials = AccountsHelper.loadCredentialsFromMnemonic(MNEMONIC, PASSWORD);
        assertTrue(AccountsHelper.isValidAddress(credentials.getAddress()));
        assertEquals(
                "0xe2DD09d719Da89e5a3D0F2549c7E24566e947260".toLowerCase(),
                credentials.getAddress().toLowerCase()
        );
    }

    @Test
    public void loadCredentialsFromMnemonicWithIndex() {
        int addresses = 10;
        for (int index=0; index<addresses; index++) {
            Credentials credentials = AccountsHelper.loadCredentialsFromMnemonic(MNEMONIC, PASSWORD, index);
            System.out.println(("Address from mnemonic [" + index + "] = " + credentials.getAddress()));
            assertTrue(AccountsHelper.isValidAddress(credentials.getAddress()));
        }
    }

}