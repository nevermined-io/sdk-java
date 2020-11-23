package io.keyko.nevermined.manager;

import io.keyko.secretstore.auth.ConsumerWorker;
import io.keyko.secretstore.auth.PublisherWorker;
import io.keyko.secretstore.core.EvmDto;
import io.keyko.secretstore.core.SecretStoreDto;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.helpers.StringsHelper;
import io.keyko.nevermined.models.DID;
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