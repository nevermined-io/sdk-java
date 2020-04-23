package io.keyko.nevermind.exceptions;


/**
 * Business Exception related with Service Agreement issues
 */
public class ServiceAgreementException extends NevermindException {

    private String serviceAgreementId;

    public ServiceAgreementException(String serviceAgreementId, String message, Throwable e) {

        super(message, e);
        this.serviceAgreementId = serviceAgreementId;

    }

    public ServiceAgreementException(String serviceAgreementId, String message) {

        super(message);
        this.serviceAgreementId = serviceAgreementId;

    }
}
