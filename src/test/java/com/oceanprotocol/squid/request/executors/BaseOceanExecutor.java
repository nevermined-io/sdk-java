package com.oceanprotocol.squid.request.executors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.squid.api.OceanAPI;
import com.oceanprotocol.squid.exceptions.InitializationException;
import com.oceanprotocol.squid.exceptions.InvalidConfiguration;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.service.ProviderConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseOceanExecutor implements Executor {

    protected static Config config;

    protected static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    protected static String METADATA_JSON_CONTENT;
    protected static AssetMetadata metadataBase;
    protected static ProviderConfig providerConfig;
    protected static OceanAPI oceanAPI;

    protected static List<OceanAPI> oceanAPIList = new ArrayList<>();
    protected static OceanAPI oceanAPIPublisher;

    protected volatile Integer apiIndex;

    @Override
    public void setUp() {

        config = ConfigFactory.load();
        apiIndex = 0;

        try {

            METADATA_JSON_CONTENT =  new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
            metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {}, METADATA_JSON_CONTENT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String metadataUrl= config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl= config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl= config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint= config.getString("secretstore.url");
        String providerAddress= config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        try {

            oceanAPIPublisher =  OceanAPI.getInstance(config);

            config = config.withValue("account.main.address", ConfigValueFactory.fromAnyRef(config.getString("account.parity.address2")))
                    .withValue("account.main.password", ConfigValueFactory.fromAnyRef(config.getString("account.parity.password2")))
                    .withValue("account.main.credentialsFile", ConfigValueFactory.fromAnyRef(config.getString("account.parity.file2")));
            oceanAPIList.add(OceanAPI.getInstance(config));

            config = config.withValue("account.main.address", ConfigValueFactory.fromAnyRef(config.getString("account.parity.address3")))
                    .withValue("account.main.password", ConfigValueFactory.fromAnyRef(config.getString("account.parity.password3")))
                    .withValue("account.main.credentialsFile", ConfigValueFactory.fromAnyRef(config.getString("account.parity.file3")));
            oceanAPIList.add(OceanAPI.getInstance(config));

            config = config.withValue("account.main.address", ConfigValueFactory.fromAnyRef(config.getString("account.parity.address4")))
                    .withValue("account.main.password", ConfigValueFactory.fromAnyRef(config.getString("account.parity.password4")))
                    .withValue("account.main.credentialsFile", ConfigValueFactory.fromAnyRef(config.getString("account.parity.file4")));
            oceanAPIList.add(OceanAPI.getInstance(config));


        }
        catch (InitializationException e) {
            e.printStackTrace();
        } catch (InvalidConfiguration invalidConfiguration) {
            invalidConfiguration.printStackTrace();
        }


    }

    protected synchronized OceanAPI getNextOceanApi() {

        if (apiIndex == oceanAPIList.size()) {
            apiIndex = 0;
            return oceanAPIList.get(apiIndex);
        }

        return oceanAPIList.get(apiIndex++);
    }
}
