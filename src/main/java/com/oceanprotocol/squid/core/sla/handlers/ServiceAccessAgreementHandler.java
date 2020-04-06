package com.oceanprotocol.squid.core.sla.handlers;


public class ServiceAccessAgreementHandler extends ServiceAgreementHandler {

    private static final String ACCESS_CONDITIONS_FILE_TEMPLATE = "sla-access-conditions-template.json";

    public  String getConditionFileTemplate() {
        return ACCESS_CONDITIONS_FILE_TEMPLATE;
    }

}
