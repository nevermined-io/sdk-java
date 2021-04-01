package io.keyko.nevermined.exceptions;

/**
 * Exception related to uploading files using the SDK
 */
public class UploadServiceException extends NeverminedException {

    public UploadServiceException(String message, Throwable e) {
        super(message, e);
    }

    public UploadServiceException(String message) {
        super(message);
    }

}
