/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.FromJsonToModel;
import com.oceanprotocol.squid.models.service.types.AccessService;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AccessTemplate extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String type = "Access";

    @JsonProperty
    public String id = "";

    @JsonProperty
    public String name = "dataAssetAccessServiceAgreement";

    @JsonProperty
    public String description;

    @JsonProperty
    public String creator;


    @JsonProperty
    public AccessService.ServiceAgreementTemplate serviceAgreementTemplate;


    public AccessTemplate() {

    }


}