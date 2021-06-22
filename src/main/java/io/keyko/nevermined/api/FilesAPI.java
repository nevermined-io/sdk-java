package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.UploadServiceException;
import io.keyko.nevermined.models.service.ProviderConfig;

/**
 * Exposes the Public API related with Files
 */
public interface FilesAPI {

    /**
     * Upload a file to Filecoin
     *
     * @param filePath the path of the file to upload
     * @param config the provider configuration
     * @return the url of the uploaded file
     * @throws UploadServiceException UploadServiceException
     */
    String uploadFilecoin(String filePath, ProviderConfig config) throws UploadServiceException;
}
