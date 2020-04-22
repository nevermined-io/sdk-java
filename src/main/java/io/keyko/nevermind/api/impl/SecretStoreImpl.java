package io.keyko.nevermind.api.impl;

import io.keyko.nevermind.api.SecretStoreAPI;
import io.keyko.nevermind.exceptions.EncryptionException;
import io.keyko.nevermind.manager.SecretStoreManager;

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
