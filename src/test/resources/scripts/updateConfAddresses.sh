#!/bin/bash

network="spree"

declare -a contracts=(
    "AccessSecretStoreCondition"
    "AgreementStoreManager"
    "ComputeExecutionCondition"
    "ConditionStoreManager"
    "DIDRegistry"
    "DIDRegistryLibrary"
    "Dispenser"
    "EpochLibrary"
    "EscrowReward"
    "HashLockCondition"
    "LockRewardCondition"
    "OceanToken"
    "SignCondition"
    "TemplateStoreManager"
    "ThresholdCondition"
    "WhitelistingCondition"
    "EscrowAccessSecretStoreTemplate"
    "EscrowComputeExecutionTemplate"
)

for c in "${contracts[@]}"
do
   address=$(jq -r .address "${HOME}/.nevermind/nevermind-contracts/artifacts/$c.$network.json")
   echo "Setting up $c address to $address"
   sed -i  "s/contract.$c.address=.*/contract.$c.address=\"$address\"/g" src/test/resources/application.conf

done

