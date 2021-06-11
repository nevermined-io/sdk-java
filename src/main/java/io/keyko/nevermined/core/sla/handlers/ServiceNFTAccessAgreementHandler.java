package io.keyko.nevermined.core.sla.handlers;


public class ServiceNFTAccessAgreementHandler extends ServiceAgreementHandler {

    private static final String CONDITIONS_FILE_TEMPLATE = "sla-nft-access-conditions-template.json";

    public  String getConditionFileTemplate() {
        return CONDITIONS_FILE_TEMPLATE;
    }

}
