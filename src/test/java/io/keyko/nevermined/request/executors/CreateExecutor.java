package io.keyko.nevermined.request.executors;

import io.keyko.nevermined.api.NeverminedAPI;
import io.keyko.nevermined.models.DDO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CreateExecutor extends BaseOceanExecutor {

    private static final Logger log = LogManager.getLogger(CreateExecutor.class);


    @Override
    public Boolean executeRequest()  throws Exception {

        NeverminedAPI neverminedAPI = getNextOceanApi();

        log.debug("index:" + this.apiIndex);
        log.debug("Main Account: " + neverminedAPI.getMainAccount().address);

         DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
         return ddo.id != null;
    }
}
