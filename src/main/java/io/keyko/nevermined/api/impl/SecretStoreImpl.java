package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.SecretStoreAPI;
import io.keyko.nevermined.exceptions.EncryptionException;
import io.keyko.nevermined.manager.SecretStoreManager;

/**
 * Implementation of SecretStoreAPI
 */
public class SecretStoreImpl implements SecretStoreAPI {


    private SecretStoreManager secretStoreManager;

    /**
     * Constructor
     *
     * @param secretStoreManager the secretStore Manager
     */
    public SecretStoreImpl(SecretStoreManager secretStoreManager) {

        this.secretStoreManager = secretStoreManager;
    }


    @Override
    public String encrypt(String documentId, String content, int threshold) throws EncryptionException {

        return secretStoreManager.encryptDocument(documentId, content, threshold);

    }

    @Override
    public String decrypt(String documentId, String encryptedContent) throws EncryptionException {

        return secretStoreManager.decryptDocument(documentId, encryptedContent);
    }

}
