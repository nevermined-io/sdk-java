package com.oceanprotocol.squid.api.impl;

import com.oceanprotocol.squid.api.AgreementsAPI;
import com.oceanprotocol.squid.exceptions.ServiceAgreementException;
import com.oceanprotocol.squid.manager.AgreementsManager;
import com.oceanprotocol.squid.manager.OceanManager;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import com.oceanprotocol.squid.models.service.Service;
import com.oceanprotocol.squid.models.service.types.AccessService;
import com.oceanprotocol.squid.models.service.types.ComputingService;
import org.web3j.crypto.Keys;
import org.web3j.tuples.generated.Tuple2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oceanprotocol.squid.core.sla.handlers.ServiceAgreementHandler.generateSlaId;

public class AgreementsImpl implements AgreementsAPI {

    private AgreementsManager agreementsManager;
    private OceanManager oceanManager;


    /**
     * Constructor
     *
     * @param agreementsManager the accountsManager
     * @param oceanManager an instance of oceanManager
     */
    public AgreementsImpl(AgreementsManager agreementsManager, OceanManager oceanManager) {
        this.oceanManager = oceanManager;
        this.agreementsManager = agreementsManager;
    }

    @Override
    public Tuple2<String, String> prepare(DID did, int serviceDefinitionId, Account consumerAccount) throws ServiceAgreementException {

        String agreementId = "";
        String signature;

        try {
            agreementId = generateSlaId();
            signature = this.sign(agreementId, did, serviceDefinitionId, consumerAccount);
            return new Tuple2<String, String>(agreementId, signature);
        }catch (Exception e) {
            throw new ServiceAgreementException(agreementId, "There was a problem preparing the agreement", e);
        }
    }

    @Override
    public boolean create(DID did, String agreementId, int index, String consumerAddress) throws ServiceAgreementException {

        try {
            DDO ddo = oceanManager.resolveDID(did);
            Service service = ddo.getService(index);

            List<byte[]> conditionsId = oceanManager.generateServiceConditionsId(agreementId, Keys.toChecksumAddress(consumerAddress), ddo, index);

            if (service.type.equals(Service.ServiceTypes.access.name()))
                return agreementsManager.createAccessAgreement(agreementId,
                        ddo,
                        conditionsId,
                        Keys.toChecksumAddress(consumerAddress),
                        service
                );
            else if (service.type.equals(Service.ServiceTypes.compute.name()))
                return agreementsManager.createComputeAgreement(agreementId,
                        ddo,
                        conditionsId,
                        Keys.toChecksumAddress(consumerAddress),
                        service
                );
            else
                throw new ServiceAgreementException(agreementId, "There was a problem creating the agreement. Service Type not supported");
        } catch (Exception e){
            throw new ServiceAgreementException(agreementId, "There was a problem creating the agreement", e);
        }

    }

    @Override
    public AgreementStatus status(String agreementId) throws ServiceAgreementException {
        try {
            return agreementsManager.getStatus(agreementId);
        }catch (Exception e) {
            throw new ServiceAgreementException(agreementId, "There was a problem getting the status of the agreement", e);
        }
    }

    public String sign(String agreementId, DID did, int serviceDefinitionId, Account consumerAccount) throws Exception {

        DDO ddo = oceanManager.resolveDID(did);
        Service service = ddo.getService(serviceDefinitionId);

        Map<String, String> conditionsAddresses = new HashMap<>();
        conditionsAddresses.put("escrowRewardAddress", this.agreementsManager.getEscrowReward().getContractAddress());
        conditionsAddresses.put("lockRewardConditionAddress", this.agreementsManager.getLockRewardCondition().getContractAddress());

        if (service.type.equals(Service.ServiceTypes.access.name())) {
            service = (AccessService) service;
            conditionsAddresses.put("accessSecretStoreConditionAddress",  this.agreementsManager.getAccessSecretStoreCondition().getContractAddress());
        }
        else if (service.type.equals(Service.ServiceTypes.compute.name())) {
            service = (ComputingService) service;
            conditionsAddresses.put("computeExecutionCondition", this.agreementsManager.getComputeExecutionCondition().getContractAddress());
        }
        else throw new ServiceAgreementException(agreementId, "Service type not supported");

        String hash = service.generateServiceAgreementHash(agreementId, consumerAccount.address, ddo.proof.creator, conditionsAddresses);
        return service.generateServiceAgreementSignatureFromHash(this.agreementsManager.getKeeperService().getWeb3(), this.agreementsManager.getKeeperService().getAddress(), consumerAccount.password, hash);
    }
}