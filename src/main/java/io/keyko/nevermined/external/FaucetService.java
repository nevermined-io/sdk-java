package io.keyko.nevermined.external;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.common.helpers.HttpHelper;
import io.keyko.common.models.HttpResponse;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.models.faucet.FaucetRequest;
import io.keyko.nevermined.models.faucet.FaucetResponse;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Service for Gateway's Integration
 */
public class FaucetService {

    private static final Logger log = LogManager.getLogger(FaucetService.class);

    private static final String FAUCET_URI = "/faucet";


    /**
     * Requests Network ETH to the faucet for paying the transactions gas
     *
     * @param address address requesting ETH from the faucet
     * @return boolean
     * @throws ServiceException if there is an error communicating with the Faucet
     */
    public static FaucetResponse requestEthFromFaucet(String faucetUrl, String address) throws ServiceException {
        try {

            final FaucetRequest faucetRequest = new FaucetRequest(address);
            HttpResponse response = HttpHelper.httpClientPost(
                    faucetUrl + FAUCET_URI, new ArrayList<>(), faucetRequest.toJson());

            if (response.getStatusCode() != HttpStatus.SC_OK && response.getStatusCode() != HttpStatus.SC_CREATED) {
                log.warn("Error getting funds from faucet " + response.getBody());
            }

            return FaucetResponse.fromJSON(new TypeReference<>() {
            }, response.getBody());

        } catch (Exception ex) {
            String msg = "Error requesting eth from faucet for address " + address;
            log.error(msg + ": " + ex.getMessage());
            throw new ServiceException("Exception getting ETH from faucet: " + ex.getMessage());
        }
    }

}
