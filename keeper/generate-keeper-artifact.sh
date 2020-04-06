#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=en_US.UTF-8

DIR_KEEPER=${1:-"../keeper-contracts"}
DIR_TARGET=${2:-"keeper"}

KEEPER_TMP=$DIR_TARGET/src/main/resources/
WEB3J_BIN=web3j

shopt -s nullglob # Avoid literal evaluation if not files

rm -rf $KEEPER_TMP $DIR_TARGET/src/main/java
mkdir -p $KEEPER_TMP

for file in $DIR_KEEPER/artifacts/*.development.json
do
    artifactFile=$(basename $file)
    cleanName=${artifactFile//.development/}
    cp $DIR_KEEPER/artifacts/$artifactFile $KEEPER_TMP/$cleanName

    $WEB3J_BIN truffle generate --javaTypes $KEEPER_TMP/$cleanName -o $DIR_TARGET/src/main/java -p io.keyko.ocean.keeper.contracts
done




