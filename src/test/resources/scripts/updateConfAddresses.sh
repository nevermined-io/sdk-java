#!/bin/bash

network="spree"

declare -a contracts=(
    "SignCondition"
    "HashLockCondition"
    "LockRewardCondition"
    "AccessSecretStoreCondition"
    "EscrowReward"
    "EscrowAccessSecretStoreTemplate"
    "OceanToken"
    "Dispenser"
    "DIDRegistry"
    "ConditionStoreManager"
    "TemplateStoreManager"
    "AgreementStoreManager"
    "ComputeExecutionCondition"
    "EscrowComputeExecutionTemplate"
)

for c in "${contracts[@]}"
do
   address=$(jq -r .address "${HOME}/.ocean/keeper-contracts/artifacts/$c.$network.json")
   echo "Setting up $c address to $address"
   sed -i  "s/contract.$c.address=.*/contract.$c.address=\"$address\"/g" src/test/resources/application.conf

done

