package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with issues during the download of a service
 */
public class DownloadServiceException extends NeverminedException {

    public DownloadServiceException(String message, Throwable e) {
        super(message, e);
    }

    public DownloadServiceException(String message) {
        super(message);
    }
}
