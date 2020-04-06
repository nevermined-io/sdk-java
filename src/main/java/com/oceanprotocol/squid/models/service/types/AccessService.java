/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.oceanprotocol.common.helpers.EthereumHelper;
import com.oceanprotocol.squid.models.service.Condition;
import com.oceanprotocol.squid.models.service.Service;
import org.web3j.crypto.Hash;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AccessService extends Service {

    @JsonIgnore
    public static final int DEFAULT_INDEX = 3;

    public AccessService() {
        this.index = DEFAULT_INDEX;
        this.type= ServiceTypes.access.toString();

    }

    public AccessService(String serviceEndpoint, int index, String templateId) {
        super(ServiceTypes.access, serviceEndpoint, index);
        this.type= ServiceTypes.access.toString();
        this.templateId = templateId;
    }


    public AccessService(String serviceEndpoint, int index,
                         ServiceAgreementTemplate serviceAgreementTemplate,
                         String templateId
    ) {
        super(ServiceTypes.access, serviceEndpoint, index);
        this.type= ServiceTypes.access.toString();
        this.templateId = templateId;
        this.attributes.serviceAgreementTemplate = serviceAgreementTemplate;

    }


    public String generateAccessSecretStoreConditionId(String serviceAgreementId, String consumerAddress, String accessSecretStoreConditionAddress) throws UnsupportedEncodingException {

        Condition accessSecretStoreCondition = this.getConditionbyName("accessSecretStore");

        Condition.ConditionParameter documentId = accessSecretStoreCondition.getParameterByName("_documentId");
        Condition.ConditionParameter grantee = accessSecretStoreCondition.getParameterByName("_grantee");


        String params = EthereumHelper.add0x(EthereumHelper.encodeParameterValue(documentId.type, documentId.value)
                + EthereumHelper.encodeParameterValue(grantee.type, consumerAddress));

        String valuesHash = Hash.sha3(params);

        return Hash.sha3(
                EthereumHelper.add0x(
                        EthereumHelper.encodeParameterValue("bytes32", serviceAgreementId)
                                + EthereumHelper.encodeParameterValue("address", accessSecretStoreConditionAddress)
                                + EthereumHelper.encodeParameterValue("bytes32", valuesHash)
                )
        );

    }


    @Override
    public List<String> generateConditionIds(String agreementId, Map<String, String> conditionsAddresses, String publisherAddress, String consumerAddress)  throws Exception{

        String escrowRewardAddress = conditionsAddresses.get("escrowRewardAddress");
        String lockRewardConditionAddress = conditionsAddresses.get("lockRewardConditionAddress");
        String accessSecretStoreConditionAddress = conditionsAddresses.get("accessSecretStoreConditionAddress");

        List<String> conditionIds = new ArrayList<>();
        String lockRewardId = generateLockRewardId(agreementId, escrowRewardAddress,lockRewardConditionAddress);
        String accessSecretStoreId = generateAccessSecretStoreConditionId(agreementId, consumerAddress,accessSecretStoreConditionAddress);
        String escrowRewardId = generateEscrowRewardConditionId(agreementId, consumerAddress, publisherAddress, escrowRewardAddress, lockRewardId, accessSecretStoreId);
        conditionIds.add(accessSecretStoreId);
        conditionIds.add(lockRewardId);
        conditionIds.add(escrowRewardId);
        return conditionIds;
    }

}