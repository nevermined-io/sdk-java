package io.keyko.nevermined.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.models.gateway.Status;
import io.keyko.nevermined.models.service.Service;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AuthorizationService extends Service {


    /**
     * Type of authorization services in the DDO
     */
    public enum AuthTypes {
        SECRET_STORE("SecretStore"),
        PSK_RSA("PSK-RSA"),
        PSK_ECDSA("PSK-ECDSA");

        private final String name;

        AuthTypes(String name) {
            this.name = name.replaceAll("_", "-");
        }
        public String getName()    {
            return this.name;
        }
    }

    @JsonIgnore
    public static final int DEFAULT_INDEX = 2;

    @JsonIgnore
    public static final AuthTypes DEFAULT_SERVICE = AuthTypes.PSK_RSA;

    @JsonIgnore
    public static final int DEFAULT_SS_THRESHOLD = 0;


    public AuthorizationService() {
        this("http://localhost:8030", DEFAULT_INDEX, DEFAULT_SERVICE);
    }

    public AuthorizationService(String serviceEndpoint, int index, AuthTypes service) {
        super(ServiceTypes.authorization, serviceEndpoint, index);
        this.type= ServiceTypes.authorization.toString();
        this.attributes.main.service = service.name;
    }

    public AuthorizationService(String serviceEndpoint, int index) {
        this(serviceEndpoint, index, DEFAULT_SERVICE);
    }

    public static AuthorizationService buildSecretStoreAuthService(String serviceEndpoint, int index) {
        return buildSecretStoreAuthService(serviceEndpoint, index, DEFAULT_SS_THRESHOLD);
    }

    public static AuthorizationService buildSecretStoreAuthService(String serviceEndpoint, int index, int threshold)    {
        AuthorizationService authorizationService = new AuthorizationService(
                serviceEndpoint, index, AuthTypes.SECRET_STORE);
        authorizationService.attributes.main.threshold = String.valueOf(threshold);
        return authorizationService;
    }

    public static AuthorizationService buildECDSAAuthService(String serviceEndpoint, int index) {
        AuthorizationService authorizationService = new AuthorizationService(
                serviceEndpoint, index, AuthTypes.PSK_ECDSA);
        return authorizationService;

    }

    public static AuthorizationService buildRSAAuthService(String serviceEndpoint, int index) {
        AuthorizationService authorizationService = new AuthorizationService(
                serviceEndpoint, index, AuthTypes.PSK_RSA);
        return authorizationService;
    }

    public static AuthorizationService buildDefaultAuthService(String serviceEndpoint, int index) {
        return buildRSAAuthService(serviceEndpoint, index);
    }

}
