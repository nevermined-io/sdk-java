package io.keyko.nevermined.api.helper;

import org.web3j.crypto.*;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public abstract class AccountsHelper {

    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

    public static String createAccount(String password, String destinationDirectory) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException {
        return WalletUtils.generateNewWalletFile(password, new File(destinationDirectory));
    }

    public static String getAddressFromFilePath(String walletFileName)    {
        try {
            String[] fetchAddress = walletFileName.split("--");
            return "0x" + fetchAddress[fetchAddress.length - 1].split("\\.")[0];
        } catch (Exception e)   {
            return null;
        }
    }

    public static boolean isValidAddress(String address)    {
        return WalletUtils.isValidAddress(address);
    }

    public static Credentials loadCredentialsFromMnemonic(String mnemonic, String password) {
        return loadCredentialsFromMnemonic(mnemonic, password, 0);
    }

    public static Credentials loadCredentialsFromMnemonic(String mnemonic, String password, int index) {
        //Derivation path: // m/44'/60'/0'/0
        int[] derivationPath = {
                44 | Bip32ECKeyPair.HARDENED_BIT,
                60 | Bip32ECKeyPair.HARDENED_BIT,
                0 | Bip32ECKeyPair.HARDENED_BIT,
                0,
                index
        };

        // Generate a BIP32 master keypair from the mnemonic phrase
        Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(
                MnemonicUtils.generateSeed(mnemonic, password));

        // Derived the key using the derivation path
        Bip32ECKeyPair derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, derivationPath);

        // Load the wallet for the derived key
        return Credentials.create(derivedKeyPair);
    }

}
