package com.oceanprotocol.squid.core.sla.handlers;


public class ServiceComputingAgreementHandler extends ServiceAgreementHandler{

    private static final String COMPUTING_CONDITIONS_FILE_TEMPLATE = "sla-computing-conditions-template.json";


    public  String getConditionFileTemplate() {

        return COMPUTING_CONDITIONS_FILE_TEMPLATE;
    }

}
