package io.keyko.nevermined.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.keyko.common.helpers.HttpHelper;
import io.keyko.common.helpers.HttpHelper.DownloadResult;
import io.keyko.common.helpers.StringsHelper;
import io.keyko.common.models.HttpResponse;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.gateway.*;
import io.keyko.nevermined.models.service.Service;
import io.keyko.nevermined.models.service.types.AuthorizationService;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Gateway's Integration
 */
public class GatewayService {

    private static final Logger log = LogManager.getLogger(GatewayService.class);

    private static final String ENCRYPT_URI = "/api/v1/gateway/services/encrypt";

    public static class ServiceAgreementResult {

        private Boolean ok;
        private Integer code;

        public Boolean getOk() {
            return ok;
        }

        public void setOk(Boolean ok) {
            this.ok = ok;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }
    }

    public static class ServiceExecutionResult extends AbstractModel {

        private Boolean ok;
        private String workflowId;
        private Integer code;

        public Boolean getOk() {
            return ok;
        }

        public void setOk(Boolean ok) {
            this.ok = ok;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getExecutionId() {
            return workflowId;
        }

        public void setExecutionId(String executionId) {
            this.workflowId = executionId;
        }

    }

    private static final String HEADER_CONSUMER_ADDRESS = "X-Consumer-Address";
    private static final String HEADER_SIGNATURE = "X-Signature";
    private static final String HEADER_DID = "X-DID";
    private static final String HEADER_WORKFLOW_DID = "X-Workflow-DID";


    /**
     * This method is Deprecated and will be removed in further versions
     * Calls a Gateway's endpoint to request the initialization of a new Service Agreement
     *
     * @param url     the url
     * @param payload the payload
     * @return an object that indicates if the Gateway initialized the Service Agreement correctly
     */
    @Deprecated
    public static ServiceAgreementResult initializeAccessServiceAgreement(String url, InitializeAccessSLA payload) {

        log.debug("Initializing SLA[" + payload.serviceAgreementId + "]: " + url);

        ServiceAgreementResult result = new ServiceAgreementResult();


        try {
            String payloadJson = payload.toJson();
            log.debug(payloadJson);

            HttpResponse response = HttpHelper.httpClientPost(
                    url, new ArrayList<>(), payloadJson);

            result.setCode(response.getStatusCode());

            if (response.getStatusCode() != 201) {
                log.debug("Unable to Initialize SLA: " + response.toString());
                result.setOk(false);
                return result;
            }
        } catch (Exception e) {
            log.error("Exception Initializing SLA: " + e.getMessage());
            result.setOk(false);
            return result;
        }

        result.setOk(true);
        return result;
    }


    /**
     * Calls a Gateway´s endpoint to download an asset
     *
     * @param serviceEndpoint    the service endpoint
     * @param consumerAddress    the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url                the url
     * @param destinationPath    the path to download the resource
     * @return DownloadResult Instance of DownloadResult that indicates if the download was correct
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static DownloadResult consumeUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId, String url, String destinationPath) throws IOException, URISyntaxException {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, serviceAgreementId);
        parameters.put(Service.URL_PARAM, url);

        String endpoint = StringsHelper.formUrl(serviceEndpoint, parameters);

        log.debug("Consuming URL[" + url + "]: for service Agreement " + serviceAgreementId);

        return HttpHelper.downloadResource(endpoint, destinationPath);

    }

    /**
     * Calls a Gateway endpoint to download an asset
     *
     * @param serviceEndpoint    the service endpoint
     * @param consumerAddress    the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url                the url
     * @param destinationPath    the path to download the resource
     * @throws IOException Exception during the download process
     */
    public static void downloadUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId, String url, String destinationPath) throws IOException {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, serviceAgreementId);
        parameters.put(Service.URL_PARAM, url);

        String endpoint = StringsHelper.formUrl(serviceEndpoint, parameters);

        log.debug("Consuming URL[" + url + "]: for service Agreement " + serviceAgreementId);

        HttpHelper.download(endpoint, destinationPath);

    }

    /**
     * Calls a Gateway endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param did asset decentralized identifier
     * @param index index position of the file in the DDO
     * @param signature User signature of the service agreement
     * @param startRange  the start of the bytes range
     * @param endRange  the end of the bytes range
     * @param isRangeRequest indicates if is a range request
     * @return an InputStream that represents the binary content
     * @throws IOException Exception during the download process
     */
    public static InputStream downloadUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId,
                                          String did, int index, String signature, Boolean isRangeRequest,
                                          Integer startRange, Integer endRange ) throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONSUMER_ADDRESS, consumerAddress);
        headers.put(HEADER_DID, did);
        headers.put(HEADER_SIGNATURE, signature);

        String endpoint = serviceEndpoint + "/" + serviceAgreementId + "/" + index;

        log.debug("Downloading from URL[" + endpoint + "]: for service Agreement " + serviceAgreementId);

        return HttpHelper.download(endpoint, headers, isRangeRequest, startRange, endRange);

    }

    /**
     * Calls a Gateway endpoint by the owner of an asset to to download it
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param did asset decentralized identifier
     * @param index index position of the file in the DDO
     * @param signature User signature of the service agreement
     * @param startRange  the start of the bytes range
     * @param endRange  the end of the bytes range
     * @param isRangeRequest indicates if is a range request
     * @return an InputStream that represents the binary content
     * @throws IOException Exception during the download process
     */
    public static InputStream downloadUrlByOwner(String serviceEndpoint, String consumerAddress,
                                          String did, int index, String signature, Boolean isRangeRequest,
                                          Integer startRange, Integer endRange ) throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONSUMER_ADDRESS, consumerAddress);
        headers.put(HEADER_DID, did);
        headers.put(HEADER_SIGNATURE, signature);

        String endpoint = serviceEndpoint + "/" + + index;

        log.debug("Owner downloading from URL[" + endpoint + "]");

        return HttpHelper.download(endpoint, headers, isRangeRequest, startRange, endRange);

    }

    /**
     * Calls a Gateway endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param did asset decentralized identifier
     * @param index index position of the file in the DDO
     * @param signature User signature of the service agreement
     * @param destinationPath path where the downloaded asset will be stored
     * @param startRange  the start of the bytes range
     * @param endRange  the end of the bytes range
     * @param isRangeRequest indicates if is a range request
     * @throws IOException Exception during the download process
     */
    public static void downloadToPath(String serviceEndpoint, String consumerAddress, String serviceAgreementId,
                                                String did, int index, String signature, String destinationPath,
                                                Boolean isRangeRequest, Integer startRange, Integer endRange) throws IOException {


        InputStream inputStream = downloadUrl(serviceEndpoint, consumerAddress, serviceAgreementId, did, index,
                signature, isRangeRequest, startRange, endRange);
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        FileOutputStream fileOutputStream = FileUtils.openOutputStream(new File(destinationPath));

        fileOutputStream.getChannel()
                .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

    }

    /**
     * Calls a Gateway endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param did asset decentralized identifier
     * @param index index position of the file in the DDO
     * @param signature User signature of the service agreement
     * @param destinationPath path where the downloaded asset will be stored
     * @param startRange  the start of the bytes range
     * @param endRange  the end of the bytes range
     * @param isRangeRequest indicates if is a range request
     * @throws IOException Exception during the download process
     */
    public static void downloadToPathByOwner(String serviceEndpoint, String consumerAddress, String did, int index,
                                             String signature, String destinationPath, Boolean isRangeRequest,
                                             Integer startRange, Integer endRange) throws IOException {


        InputStream inputStream = downloadUrlByOwner(serviceEndpoint, consumerAddress, did, index, signature,
                isRangeRequest, startRange, endRange);
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        FileOutputStream fileOutputStream = FileUtils.openOutputStream(new File(destinationPath));

        fileOutputStream.getChannel()
                .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

    }

    /**
     * Calls a Gateway endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @return an InputStream that represents the binary content
     * @throws IOException Exception during the download process
     */
    public static Status getStatus(String serviceEndpoint) throws IOException {

        log.debug("Getting Gateway Status");
        try {
            final HttpResponse httpResponse = HttpHelper.httpClientGet(serviceEndpoint);
            if (httpResponse.getStatusCode() != 200) {
                throw new IOException("Invalid http response from Gateway: " + httpResponse.getStatusCode());
            }
            return Status.fromJSON(new TypeReference<>() {}, httpResponse.getBody());
        } catch (HttpException e) {
            throw new IOException("Unable to fetch status page", e);
        } catch (Exception e) {
            throw new IOException("Unable to parse status page from gateway", e);
        }
    }

    /**
     * Calls a Gateway endpoint to request the execution of a Compute Service
     *
     * @param gatewayUrl encryption endpoint
     * @param message the message to encrypt
     * @param authType AuthType to use for encryption
     * @throws ServiceException Service Exception
     * @return EncryptionResponse an object that indicates if Gateway initialized the Execution of the Service correctly
     */
    public static EncryptionResponse encrypt(String gatewayUrl, String message, AuthorizationService.AuthTypes authType)
            throws ServiceException {
        return encrypt(gatewayUrl, message, authType, null);
    }

    /**
     * Calls a Gateway endpoint to request the execution of a Compute Service
     *
     * @param gatewayUrl encryption endpoint
     * @param message the message to encrypt
     * @param authType AuthType to use for encryption
     * @param did DID used to encrypt when using SecretStore
     * @throws ServiceException Service Exception
     * @return EncryptionResponse an object with the gateway encryption response
     */
    public static EncryptionResponse encrypt(String gatewayUrl, String message, AuthorizationService.AuthTypes authType, String did)
        throws ServiceException {

        log.debug("Encrypting message using " + authType.name());

        EncryptionRequest encryptionReq = new EncryptionRequest(message, authType.getName());
        final String endpoint = gatewayUrl + ENCRYPT_URI;
        if (authType.equals(AuthorizationService.AuthTypes.SECRET_STORE) && !did.isEmpty())
            encryptionReq.did = did;

        HttpResponse response;

        try {
            String payloadJson = encryptionReq.toJson();

            response = HttpHelper.httpClientPost(
                    endpoint, new ArrayList<>(), payloadJson);

            if (response.getStatusCode() != 200) {
                log.error("Unable to Encrypt Message: " + response.toString());
                throw new ServiceException("Unable to Encrypt Message");
            }
            return EncryptionResponse.fromJSON(new TypeReference<>() {}, response.getBody());

        } catch (Exception e) {
            log.error("Error encrypting message: " + e.getMessage());
            throw new ServiceException("Error Encrypting Message: " + e.getMessage());
        }
    }


    /**
     * Calls a Gateway endpoint to request the execution of a Compute Service
     *
     * @param serviceEndpoint the serviceEndpoint
     * @param executeService the payload
     * @return an object that indicates if Gateway initialized the Execution of the Service correctly
     */
    public static ServiceExecutionResult initializeServiceExecution(String serviceEndpoint, ExecuteService executeService) {

        log.debug("Initializing Execution of Service. Agreement Id: [" + executeService.agreementId + "]: " + serviceEndpoint);

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONSUMER_ADDRESS, executeService.consumerAddress);
        headers.put(HEADER_WORKFLOW_DID, executeService.workflowId);
        headers.put(HEADER_SIGNATURE, executeService.signature);

        String endpoint = serviceEndpoint + "/" + executeService.agreementId;

        ServiceExecutionResult result = new ServiceExecutionResult();
        HttpResponse response;

        try {

            response = HttpHelper.httpClientPost(endpoint, headers);

            result.setCode(response.getStatusCode());

            if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
                log.debug("Unable to Initialize Execution of the Service: " + response.toString());
                result.setOk(false);
                return result;
            }

            result.setOk(true);
            result.setExecutionId(getExecutionId(response.getBody()));
            return result;

        } catch (Exception e) {
            log.error("Exception Initializing Execution of the Service: " + e.getMessage());
            result.setOk(false);
            return result;
        }
    }

    private static String getExecutionId(String bodyResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseMap = mapper.readValue(bodyResponse, new TypeReference<Map<String, String>>(){});

        return responseMap.get("workflowId");
    }

    /**
     * Calls a gateway endpoint to get the compute logs
     *
     * @param serviceEndpoint the gateway service endpoint
     * @param consumerAddress the address of the consumer of the compute to the data job
     * @param signature the signature of the executionId
     * @return a list of compute logs.
     * @throws ServiceException ServiceException
     */
    public static List<ComputeLogs> getComputeLogs(String serviceEndpoint, String consumerAddress, String signature)
            throws ServiceException {

        HttpResponse response;
        List<ComputeLogs> logs;
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONSUMER_ADDRESS, consumerAddress);
        headers.put(HEADER_SIGNATURE, signature);

        try {
            response = HttpHelper.httpClientGet(serviceEndpoint, headers);
        } catch (HttpException e) {
            log.error("Exception getting the compute logs: " + e.getMessage());
            throw new ServiceException("Exception getting the compute logs", e);
        }

        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            log.error("Unable to get the compute logs: " + response.toString());
            throw new ServiceException("Unable to get logs: " + response.toString());
        }

        try {
            logs = ComputeLogs.fromJSON(new TypeReference<>() {}, response.getBody());
        } catch (IOException e) {
            log.error("Exception parsing the compute logs: " + e.getMessage());
            throw new ServiceException("Unable to parse logs", e);
        }
        return logs;
    }

}
