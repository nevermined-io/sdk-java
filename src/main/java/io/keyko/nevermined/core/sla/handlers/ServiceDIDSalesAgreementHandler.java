package io.keyko.nevermined.core.sla.handlers;


public class ServiceDIDSalesAgreementHandler extends ServiceAgreementHandler {

    private static final String CONDITIONS_FILE_TEMPLATE = "sla-did-sales-conditions-template.json";

    public  String getConditionFileTemplate() {
        return CONDITIONS_FILE_TEMPLATE;
    }

}
