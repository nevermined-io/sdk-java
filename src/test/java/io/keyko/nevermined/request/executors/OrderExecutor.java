package io.keyko.nevermined.request.executors;

import io.keyko.nevermined.api.NeverminedAPI;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.asset.OrderResult;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderExecutor extends BaseOceanExecutor{

    private static final Logger log = LogManager.getLogger(OrderExecutor.class);


    private static DDO ddo;

    @Override
    public void setUp() {

        super.setUp();

        try {
            log.info("Creating DDO...");
            ddo = neverminedAPIPublisher.getAssetsAPI().create(metadataBase, providerConfig);
            log.info("DDO Created");
        } catch (DDOException e) {
            log.error(e.getMessage());
        }

    }


    @Override
    public Boolean executeRequest() throws Exception {

        if (ddo == null || ddo.getDID() == null)
            throw new Exception("DDO is not created");

        log.info("Executing Request");

        NeverminedAPI neverminedAPI = getNextNeverminedAPI();
        log.info("Using NeverminedAPI Instance with the main Address: " + neverminedAPI.getMainAccount().address);

        Flowable<OrderResult> result = neverminedAPI.getAssetsAPI().purchaseOrder(ddo.getDID(), 1);
        OrderResult orderResult = result.blockingFirst();

        log.info("Result of the purchaseOrder flow. AccessGranted: " + orderResult.isAccessGranted() + ". ServiceAgreementId: " + orderResult.getServiceAgreementId());

        if (orderResult.isAccessGranted() && orderResult.getServiceAgreementId()!= null)
            return true;

        return false;

    }
}
