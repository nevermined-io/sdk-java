package io.keyko.nevermined.api.impl;

import java.io.File;

import io.keyko.nevermined.api.FilesAPI;
import io.keyko.nevermined.exceptions.UploadServiceException;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.external.GatewayService.UploadToFilecoinResult;
import io.keyko.nevermined.models.service.ProviderConfig;

/**
 * Implementation of FilesAPI
 */
public class FilesImpl implements FilesAPI {

    /**
     * Upload a file to Filecoin
     *
     * @param filePath the path of the file to upload
     * @return the url of the uploaded file
     * @throws UploadServiceException UploadServiceException
     */
    public String uploadFilecoin(String filePath, ProviderConfig config) throws UploadServiceException {
        File file = new File(filePath);
        String serviceEndpoint = config.getUploadToFilecoinEndpoint();

        if(!file.exists()) {
            throw new UploadServiceException("File " + filePath + " does not exist or is not a file");
        }

        UploadToFilecoinResult result = GatewayService.uploadToFilecoin(serviceEndpoint, file);

        if(!result.getOk()) {
            throw new UploadServiceException("Could not upload to Filecoin: " + result.getMsg());
        }

        return result.getUrl();
    }

}
