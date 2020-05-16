package io.keyko.nevermined.models.service;

import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.models.gateway.Status;
import io.keyko.nevermined.models.service.types.AuthorizationService;

import java.io.IOException;

public class AuthConfig {

    private String serviceEndpoint;
    private AuthorizationService.AuthTypes service;
    private String publicKey = "";
    private int threshold = AuthorizationService.DEFAULT_SS_THRESHOLD;

    public AuthConfig(String serviceEndpoint) {
        this(serviceEndpoint, AuthorizationService.DEFAULT_SERVICE);
    }

    public AuthConfig(String serviceEndpoint, AuthorizationService.AuthTypes service) {
        this.serviceEndpoint = serviceEndpoint;
        this.service = service;

        try {
            final Status status = GatewayService.getStatus(serviceEndpoint);
            if (this.service.equals(AuthorizationService.AuthTypes.PSK_RSA))
                this.publicKey = status.rsaPublicKey;
            else if (this.service.equals(AuthorizationService.AuthTypes.PSK_ECDSA))
                this.publicKey = status.ecdsaPublicKey;
        } catch (IOException e) {
        }
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public AuthConfig setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
        return this;
    }

    public AuthorizationService.AuthTypes getService() {
        return service;
    }

    public AuthConfig setService(AuthorizationService.AuthTypes service) {
        this.service = service;
        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public AuthConfig setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public int getThreshold() {
        return threshold;
    }

    public AuthConfig setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }
}
