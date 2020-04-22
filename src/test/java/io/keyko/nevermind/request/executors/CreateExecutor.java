package io.keyko.nevermind.request.executors;

import io.keyko.nevermind.api.OceanAPI;
import io.keyko.nevermind.models.DDO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CreateExecutor extends BaseOceanExecutor {

    private static final Logger log = LogManager.getLogger(CreateExecutor.class);


    @Override
    public Boolean executeRequest()  throws Exception {

        OceanAPI oceanAPI = getNextOceanApi();

        log.debug("index:" + this.apiIndex);
        log.debug("Main Account: " + oceanAPI.getMainAccount().address);

         DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
         return ddo.id != null;
    }
}
