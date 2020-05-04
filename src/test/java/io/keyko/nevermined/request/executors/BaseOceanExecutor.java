package io.keyko.nevermined.request.executors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.keyko.nevermined.api.NeverminedAPI;
import io.keyko.nevermined.exceptions.InitializationException;
import io.keyko.nevermined.exceptions.InvalidConfiguration;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.ProviderConfig;

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
    protected static NeverminedAPI neverminedAPI;

    protected static List<NeverminedAPI> neverminedAPIList = new ArrayList<>();
    protected static NeverminedAPI neverminedAPIPublisher;

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

        String metadataUrl= config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl= config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String consumeUrl= config.getString("gateway.url") + "/api/v1/gateway/services/consume";
        String secretStoreEndpoint= config.getString("secretstore.url");
        String providerAddress= config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        try {

            neverminedAPIPublisher =  NeverminedAPI.getInstance(config);

            config = config.withValue("account.main.address", ConfigValueFactory.fromAnyRef(config.getString("account.parity.address2")))
                    .withValue("account.main.password", ConfigValueFactory.fromAnyRef(config.getString("account.parity.password2")))
                    .withValue("account.main.credentialsFile", ConfigValueFactory.fromAnyRef(config.getString("account.parity.file2")));
            neverminedAPIList.add(NeverminedAPI.getInstance(config));

            config = config.withValue("account.main.address", ConfigValueFactory.fromAnyRef(config.getString("account.parity.address3")))
                    .withValue("account.main.password", ConfigValueFactory.fromAnyRef(config.getString("account.parity.password3")))
                    .withValue("account.main.credentialsFile", ConfigValueFactory.fromAnyRef(config.getString("account.parity.file3")));
            neverminedAPIList.add(NeverminedAPI.getInstance(config));

            config = config.withValue("account.main.address", ConfigValueFactory.fromAnyRef(config.getString("account.parity.address4")))
                    .withValue("account.main.password", ConfigValueFactory.fromAnyRef(config.getString("account.parity.password4")))
                    .withValue("account.main.credentialsFile", ConfigValueFactory.fromAnyRef(config.getString("account.parity.file4")));
            neverminedAPIList.add(NeverminedAPI.getInstance(config));


        }
        catch (InitializationException e) {
            e.printStackTrace();
        } catch (InvalidConfiguration invalidConfiguration) {
            invalidConfiguration.printStackTrace();
        }


    }

    protected synchronized NeverminedAPI getNextNeverminedAPI() {

        if (apiIndex == neverminedAPIList.size()) {
            apiIndex = 0;
            return neverminedAPIList.get(apiIndex);
        }

        return neverminedAPIList.get(apiIndex++);
    }
}
