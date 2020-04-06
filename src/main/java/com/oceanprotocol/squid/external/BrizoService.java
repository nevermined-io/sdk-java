/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanprotocol.common.helpers.HttpHelper;
import com.oceanprotocol.common.helpers.HttpHelper.DownloadResult;
import com.oceanprotocol.common.helpers.StringsHelper;
import com.oceanprotocol.common.models.HttpResponse;
import com.oceanprotocol.squid.models.brizo.ExecuteService;
import com.oceanprotocol.squid.models.brizo.InitializeAccessSLA;
import com.oceanprotocol.squid.models.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for Brizo's Integration
 */
public class BrizoService {

    private static final Logger log = LogManager.getLogger(BrizoService.class);

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

    public static class ServiceExecutionResult {

        private Boolean ok;
        private String executionId;
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
            return executionId;
        }

        public void setExecutionId(String executionId) {
            this.executionId = executionId;
        }

    }


    /**
     * Calls a Brizo's endpoint to request the initialization of a new Service Agreement
     *
     * @param url     the url
     * @param payload the payload
     * @return an object that indicates if Brizo initialized the Service Agreement correctly
     */
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
     * Calls a Brizo´s endpoint to download an asset
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
     * Calls a Brizo´s endpoint to download an asset
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
     * Calls a Brizo´s endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url the url
     * @param startRange  the start of the bytes range
     * @param endRange  the end of the bytes range
     * @param isRangeRequest indicates if is a range request
     * @return an InputStream that represents the binary content
     * @throws IOException Exception during the download process
     */
    public static InputStream downloadUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId, String url, Boolean isRangeRequest, Integer startRange, Integer endRange ) throws IOException {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, serviceAgreementId);
        parameters.put(Service.URL_PARAM, url);

        String endpoint = StringsHelper.formUrl(serviceEndpoint, parameters);

        log.debug("Consuming URL[" + url + "]: for service Agreement " + serviceAgreementId);

        return HttpHelper.download(endpoint, isRangeRequest, startRange, endRange);

    }

    /**
     * Calls a Brizo's endpoint to request the execution of a Compute Service
     *
     * @param serviceEndpoint the serviceEndpoint
     * @param payload the payload
     * @return an object that indicates if Brizo initialized the Execution of the Service correctly
     */
    public static ServiceExecutionResult initializeServiceExecution(String serviceEndpoint, ExecuteService payload) {

        log.debug("Initializing Execution of Service. Agreement Id: [" + payload.agreementId + "]: " + serviceEndpoint);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, payload.consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, payload.agreementId);
        parameters.put(Service.WORKFLOWID_PARAM, payload.workflowId);
        parameters.put(Service.SIGNATURE_PARAM, payload.signature);

        String endpoint = StringsHelper.formUrl(serviceEndpoint, parameters);

        ServiceExecutionResult result = new ServiceExecutionResult();
        HttpResponse response;

        try {
            String payloadJson = payload.toJson();
            log.debug(payloadJson);

            response = HttpHelper.httpClientPost(
                    endpoint, new ArrayList<>(), payloadJson);

            result.setCode(response.getStatusCode());

            if (response.getStatusCode() != 200) {
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

}
