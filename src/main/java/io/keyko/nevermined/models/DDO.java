package io.keyko.nevermined.models;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.api.client.util.Base64;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.exceptions.DIDFormatException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.manager.SecretStoreManager;
import io.keyko.nevermined.models.gateway.EncryptionResponse;
import io.keyko.nevermined.models.service.AuthConfig;
import io.keyko.nevermined.models.service.Service;
import io.keyko.nevermined.models.service.types.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static io.keyko.nevermined.models.DDO.PublicKey.ETHEREUM_KEY_TYPE;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class
DDO extends AbstractModel implements FromJsonToModel {

    private static final Logger log = LogManager.getLogger(DDO.class);

    private static final String UUID_PROOF_TYPE = "DDOIntegritySignature";

    private static final String AUTHENTICATION_TYPE = "RsaSignatureAuthentication2018";

    @JsonProperty("@context")
    public String context = "https://w3id.org/did/v1";

    @JsonProperty
    public String id;

    @JsonIgnore
    private DID did;

    @JsonProperty("publicKey")
    public List<PublicKey> publicKeys = new ArrayList<>();

    @JsonProperty
    public List<Authentication> authentication = new ArrayList<>();

    @JsonIgnore
    public List<Service> services = new ArrayList<>();

    @JsonProperty
    public Proof proof;

    //@JsonProperty
    //public List<VerifiableCredential> verifiableCredential;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    public Date created;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    @JsonProperty
    public Date updated;


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    static class PublicKey {

        public static final String ETHEREUM_KEY_TYPE = "EthereumECDSAKey";

        @JsonProperty
        public String id;

        @JsonProperty
        public String type;

        @JsonProperty
        public String owner;

        @JsonProperty
        public String publicKeyPem;

        @JsonProperty
        public String publicKeyBase58;

        public PublicKey() {
        }

        public PublicKey(String id, String type, String owner) {
            this.id = id;
            this.type = type;
            this.owner = owner;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    static class Authentication {

        @JsonProperty
        public String type;

        @JsonProperty
        public String publicKey;

        public Authentication() {
        }

        public Authentication(String id) {
            this.publicKey = id;
            this.type = AUTHENTICATION_TYPE;
        }

    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    static public class Proof {

        @JsonProperty
        public String type;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
        @JsonDeserialize(using = CustomDateDeserializer.class)
        public Date created;

        @JsonProperty
        public String creator;

        @JsonProperty
        public String signatureValue;

        @JsonProperty
        public Map<String, String> checksum;

        public Proof() {
        }

        public Proof(String type, String creator, String signature) {
            this.type = type;
            this.creator = creator;
            this.signatureValue = signature;
            this.created = getDateNowFormatted();
        }

        public Proof(String type, String creator, byte[] signature) {
            this(type, creator, Base64.encodeBase64URLSafeString(signature));
        }
    }

    /*
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class VerifiableCredential {

        public enum Types {read, update, deactivate}

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonPropertyOrder(alphabetic = true)
        public static class CredentialSubject {

            @JsonProperty
            public String id;

            public CredentialSubject(){}
        }

        @JsonProperty("@context")
        public List<String> context = List.of("https://www.w3.org/2018/credentials/v1", "https://www.w3.org/2018/credentials/examples/v1");

        @JsonProperty
        public String id;

        @JsonProperty
        public List<Types> type;

        @JsonProperty
        public String issuer;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
        @JsonDeserialize(using = CustomDateDeserializer.class)
        public Date issuanceDate;

        @JsonProperty
        public CredentialSubject credentialSubject;

        @JsonProperty
        Proof proof;

        public VerifiableCredential(){}

    }*/

    public DDO() {
        this.did = null;
        if (null == this.created)
            this.created = getDateNowFormatted();
        if (null == this.updated)
            this.updated = getDateNowFormatted();

        this.id = "{did}";
    }


    public DDO(MetadataService metadataService, String publicKey, String signature) throws DIDFormatException {

        if (null == this.created)
            this.created = getDateNowFormatted();
        if (null == this.updated)
            this.updated = this.created;

        this.services.add(metadataService);

        this.proof = new Proof(UUID_PROOF_TYPE, publicKey, signature);
        this.publicKeys.add(new DDO.PublicKey(publicKey, ETHEREUM_KEY_TYPE, publicKey));
    }


    @JsonSetter("id")
    public void didSetter(String id) throws DIDFormatException {
        this.id = id;
        this.did = new DID(id);
    }

    public DDO setDID(DID _did) {
        this.did = _did;
        this.id = _did.getDid();
        return this;
    }

    public DDO addAuthentication(String id) {
        this.authentication.add(new Authentication(id));
        return this;
    }


    private void sortServices()    {
        Collections.sort(services, new DDOServiceIndexSorter());
    }


    public DDO addService(Service service) {
        services.add(service);
        return this;
    }

    @JsonSetter("service")
    public void servicesSetter(ArrayList<LinkedHashMap> services) {

        try {
            for (LinkedHashMap service : services) {
                if (service.containsKey("type")) {
                    if (service.get("type").equals(Service.ServiceTypes.METADATA.toString()) && service.containsKey("metadata")) {
                        this.services.add(getMapperInstance().convertValue(service, MetadataService.class));
                    } else if (service.get("type").equals(Service.ServiceTypes.PROVENANCE.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, ProvenanceService.class));
                    } else if (service.get("type").equals(Service.ServiceTypes.ACCESS.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, AccessService.class));
                    } else if (service.get("type").equals(Service.ServiceTypes.COMPUTE.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, ComputingService.class));
                    } else if (service.get("type").equals(Service.ServiceTypes.AUTHORIZATION.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, AuthorizationService.class));
                    } else if (service.get("type").equals(Service.ServiceTypes.NFT_ACCESS.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, NFTAccessService.class));
                    } else if (service.get("type").equals(Service.ServiceTypes.NFT_SALES.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, NFTSalesService.class));
                    } else if (service.get("type").equals(Service.ServiceTypes.DID_SALES.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, DIDSalesService.class));
                    } else {
                        this.services.add(getMapperInstance().convertValue(service, Service.class));
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Unable to parse the DDO(services): " + services + ", Exception: " + ex.getMessage());
        }
    }

    @JsonGetter("service")
    public List<Service> servicesGetter() {

        int counter = 0;
        for (Service service : services) {
            if (service.type != null) {
                services.set(counter, service);
                counter++;
            }
        }

        return this.services;
    }

    /**
     * Given a user credentials this method do the following:
     * - Calculate the individual DDO.services checksums
     * - Calculate the DID using the hash of the DDO.services checksums
     * - Generate the DDO.proof entry signing the DID generated and adding the credentials information
     *
     * @param ddo the DDO used as starting point to calculate the checksums
     * @param credentials account credentials
     * @return DDO
     * @throws DDOException if there is an error calculating anything
     */
    public static DDO integrityBuilder(DDO ddo, Credentials credentials) throws DDOException {
        try {
            // 1. Sorting services
            Collections.sort(ddo.services, new DDOServiceIndexSorter());

            // 2. Setting up the checksums in the DDO.proof.checksum entry
            ddo.proof.checksum= DDO.generateChecksums(ddo);

            // 3. Calculating the DID Seed as a Hash of the DDO.services checksums
            String _seed = EthereumHelper.remove0x(Hash.sha3(ddo.toJson(ddo.proof.checksum)));
            DID didFromSeed = DID.getFromSeed(_seed, credentials.getAddress());
            final String _id = didFromSeed.getDid();

            // 4. Completing the DDO.proof signing the DID and adding the rest of the values
            Sign.SignatureData signatureData= EthereumHelper.signMessage(_id, credentials);
            ddo.proof.signatureValue= EncodingHelper.signatureToString(signatureData);
            ddo.proof.creator= Keys.toChecksumAddress(credentials.getAddress());
            ddo.proof.created= getDateNowFormatted();

            // Replace any {did} entry in the JSON by the real DID generated
            DDO newDDO = fromJSON(new TypeReference<DDO>() {},
                    ddo.toJson().replaceAll("\\{did\\}", _id));
            newDDO.id = _id;
            newDDO.did = didFromSeed;
            return newDDO;

        } catch (Exception ex)  {
            throw new DDOException("Unable to generate service checksum: " + ex.getMessage());
        }
    }

    public DDO secretStoreLocalEncryptFiles(SecretStoreManager secretStoreManager, AuthConfig authConfig) throws DDOException {

        try {
            Service metadataService = this.getMetadataService();
            Service authService = this.getAuthorizationService();

            String filesJson = metadataService.toJson(metadataService.attributes.main.files);
            EncryptionResponse encryptionResponse = GatewayService.encrypt(
                    authConfig.getServiceEndpoint(), filesJson, authConfig.getService());

            metadataService.attributes.encryptedFiles = encryptionResponse.hash;
            authService.attributes.main.publicKey = encryptionResponse.publicKey;

        }catch (JsonProcessingException | ServiceException e) {
            throw new DDOException("Unable to encrypt files using SecretStore: " + e.getMessage(), e);
        }

        return this;
    }

    public DDO gatewayEncryptFiles(AuthConfig authConfig) throws DDOException {
        try {
            Service metadataService = this.getMetadataService();
            Service authService = this.getAuthorizationService();

            String filesJson = metadataService.toJson(metadataService.attributes.main.files);

            EncryptionResponse encryptionResponse = GatewayService.encrypt(
                    authConfig.getServiceEndpoint(), filesJson, authConfig.getService());

            metadataService.attributes.encryptedFiles = encryptionResponse.hash;
            authService.attributes.main.publicKey = encryptionResponse.publicKey;
        }catch (JsonProcessingException | ServiceException e) {
            throw new DDOException("Unable to encrypt files via Gateway: " + e.getMessage(), e);
        }

        return this;
    }

    public DDO setEncryptedFiles(String hash)   {
        Service metadataService = this.getMetadataService();
        metadataService.attributes.encryptedFiles = hash;
        return this;
    }

    public static SortedMap<String, String> generateChecksums(DDO ddo) throws DDOException {

        SortedMap<String, String> checksums = new TreeMap<>();
        try {
            for (Service service : ddo.services) {
                checksums.put(
                        String.valueOf(service.index),
                        service.attributes.main.checksum());
            }
        } catch (Exception ex)  {
            throw new DDOException("Unable to generate service checksum: " + ex.getMessage());
        }

        return checksums;

    }

    public static DID generateDID() throws DIDFormatException {
        DID did = DID.builder();
        log.debug("Id generated: " + did.toString());
        return did;
    }


    public DID getDID() {
        return did;
    }

    public DID fetchDIDSeed() throws DIDFormatException {
        return DID.getFromHash(did.seed);
    }

    public AccessService getAccessService(int index) throws ServiceException {
        for (Service service : services) {
            if (service.index == index && service.type.equals(Service.ServiceTypes.ACCESS.toString())) {
                return (AccessService) service;
            }
        }
        throw new ServiceException("Access Service with index=" + index + " not found");
    }

    @JsonIgnore
    public Service getService(int index) throws ServiceException {

        return services.stream()
                .filter(s -> s.index == index)
                .findFirst()
                .orElseThrow(() -> new ServiceException("Service with index=" + index + " not found"));

    }

    @JsonIgnore
    public Service getServiceByTemplate(String templateId) throws ServiceException {

        // We assume there is only one service in the DDO with a specific templateId
        return services.stream()
                .filter(s -> s.templateId instanceof String && s.templateId.toLowerCase().equals(templateId.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Service with template=" + templateId + " not found"));
    }

    @JsonIgnore
    public Service getServiceByType(Service.ServiceTypes serviceType) throws ServiceException {
        // We assume there is only one service in the DDO with a specific templateId
        return services.stream()
                .filter(s -> s.type instanceof String && s.type.toLowerCase().equals(serviceType.toString()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Service with type=" + serviceType.toString() + " not found"));
    }

    @JsonIgnore
    public AuthorizationService getAuthorizationService(int serviceDefinitionId) {
        for (Service service : services) {
            if (service.index == serviceDefinitionId && service.type.equals(Service.ServiceTypes.AUTHORIZATION.toString())) {
                return (AuthorizationService) service;
            }
        }
        return null;
    }

    @JsonIgnore
    public AuthorizationService getAuthorizationService() {
        for (Service service : services) {
            if (service.type.equals(Service.ServiceTypes.AUTHORIZATION.toString())) {
                return (AuthorizationService) service;
            }
        }

        return null;
    }

    @JsonIgnore
    public Service getMetadataService() {
        for (Service service : services) {
            if (service.type.equals(Service.ServiceTypes.METADATA.toString())) {
                return service;
            }
        }
        return null;
    }

    @JsonIgnore
    public AccessService getAccessService() {
        for (Service service : services) {
            if (service.type.equals(Service.ServiceTypes.ACCESS.toString())) {
                return (AccessService) service;
            }
        }
        return null;
    }

    @JsonIgnore
    public ComputingService getComputeService() {
        for (Service service : services) {
            if (service.type.equals(Service.ServiceTypes.COMPUTE.toString())) {
                return (ComputingService) service;
            }
        }
        return null;
    }

    @JsonIgnore
    public DIDSalesService getDIDSalesService() {
        for (Service service : services) {
            if (service.type.equals(Service.ServiceTypes.DID_SALES.toString())) {
                return (DIDSalesService) service;
            }
        }
        return null;
    }

    @JsonIgnore
    public NFTSalesService getNFTSalesService() {
        for (Service service : services) {
            if (service.type.equals(Service.ServiceTypes.NFT_SALES.toString())) {
                return (NFTSalesService) service;
            }
        }
        return null;
    }

    @JsonIgnore
    public NFTAccessService getNFTAccessService() {
        for (Service service : services) {
            if (service.type.equals(Service.ServiceTypes.NFT_ACCESS.toString())) {
                return (NFTAccessService) service;
            }
        }
        return null;
    }

    @JsonIgnore
    public static DDO cleanFileUrls(DDO ddo) {

        ddo.services.forEach(service -> {
            if (service.type.equals(Service.ServiceTypes.METADATA.toString())) {
               service.attributes.main.files.forEach(f -> {
                    f.url = null;
                });
            }
        });

        return ddo;
    }

    @JsonIgnore
    public static DDO replaceConditionVariables(DDO ddo, String name, String value) {
        try {
            return DDO.fromJSON(new TypeReference<DDO>() {},
                    ddo.toJson().replaceAll("\\{parameter." + name + "\\}", value)
            );
        } catch (JsonProcessingException e) {
            log.error("Unable to parse DDO");
        } catch (IOException e) {
            log.error("Unable to generate DDO");
        }
        return ddo;
    }

}