/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.oceanprotocol.secretstore.auth.ConsumerWorker;
import com.oceanprotocol.secretstore.auth.PublisherWorker;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.oceanprotocol.common.helpers.StringsHelper;
import com.oceanprotocol.squid.models.DID;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SecretStoreManagerIT {


    private static final Logger log= LogManager.getLogger(SecretStoreManagerIT.class);

    private static PublisherWorker publisherWorker;
    private static ConsumerWorker consumerWorker;

    private static final Config config = ConfigFactory.load();

    private static final String URL1= "https://i.giphy.com/media/3o6Zt481isNVuQI1l6/giphy.webp";
    private static final String URL2= "https://disney.com";

    @BeforeClass
    public static void setUp() throws Exception {
        SecretStoreDto ssDto= SecretStoreDto.builder(config.getString("secretstore.url"));
        EvmDto publisherEvmDto= EvmDto.builder(
                config.getString("keeper.url"),
                config.getString("account.parity.address"),
                config.getString("account.parity.password")
        );

        EvmDto consumerEvmDto= EvmDto.builder(
                config.getString("keeper.url"),
                config.getString("account.parity.address2"),
                config.getString("account.parity.password2")
        );

        publisherWorker= new PublisherWorker(ssDto, publisherEvmDto);
        consumerWorker= new ConsumerWorker(ssDto, consumerEvmDto);
    }

    // This IT only works if Secret Store is started without the `acl_contract` option set to none
    @Test
    @Ignore
    public void encryptDocument() throws Exception {
        String did= DID.builder().getHash();

        List<String> contentUrls= new ArrayList<>();
        contentUrls.add(URL1);
        contentUrls.add(URL2);

        String urls= "[" + StringsHelper.wrapWithQuotesAndJoin(contentUrls) + "]";

        log.debug("Encrypting did: " + did + " and urls: " + urls);
        String encryptedContent= publisherWorker.encryptDocument(did, urls, 0);

        String decryptedContent= consumerWorker.decryptDocument(did, encryptedContent);

        assertEquals(urls, decryptedContent);

    }
}