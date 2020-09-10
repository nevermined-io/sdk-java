[![banner](https://raw.githubusercontent.com/keyko-io/assets/master/images/logo/nevermined_logo_1.png)](https://nevermined.io)


# Java API for Nevermined Data platform

> Java SDK for connecting with Nevermined Data Platform
> [keyko.io](https://keyko.io)

![Java Maven CI](https://github.com/keyko-io/nevermined-sdk-java/workflows/Java%20Maven%20CI/badge.svg)
![Release](https://github.com/keyko-io/nevermined-sdk-java/workflows/Release/badge.svg)
[![javadoc](https://javadoc.io/badge2/io.keyko.nevermined/api/javadoc.svg)](https://javadoc.io/doc/io.keyko.nevermined/api)

---

## Table of Contents

   * [Java API for Nevermined Data platform](#java-api-for-nevermined-data-platform)
      * [Table of Contents](#table-of-contents)
      * [Features](#features)
      * [Installation](#installation)
      * [Configuration](#configuration)
         * [Using the SDK with the Nevermined Tools](#using-the-sdk-with-the-nevermined-tools)
         * [Dealing with Flowables](#dealing-with-flowables)
      * [Documentation](#documentation)
      * [Testing](#testing)
         * [Unit Tests](#unit-tests)
         * [Integration Tests](#integration-tests)
         * [Documentation](#documentation-1)
         * [All the tests](#all-the-tests)
         * [Code Coverage](#code-coverage)
      * [New Release](#new-release)
      * [Attribution](#attribution)
      * [License](#license)


---

## Features

This library enables to integrate the Nevermined Data Platform based in Ocean Protocol.

## Installation

Typically in Maven you can add nevermined sdk as a dependency:

```xml
<dependency>
  <groupId>io.keyko.nevermined</groupId>
  <artifactId>api</artifactId>
  <version>0.3.2</version>
</dependency>
```

Nevermined SDK requires Java 11 and Maven >= 3.5.2

## Configuration

You can configure the library using TypeSafe Config or a Java Properties Object

In case you want to use TypeSafe Config you would need an application.conf file with this shape:

```
keeper.url="http://localhost:8545"
keeper.gasLimit=4712388
keeper.gasPrice=100000000000
keeper.tx.attempts=50
keeper.tx.sleepDuration=2000


metadata.url="http://localhost:5000"
# Used in the internal communications between Docker containers (Spree network)
metadata-internal.url="http://172.15.0.15:5000" # Running Nevermined tools
# metadata-internal.url="http://localhost:5000" # Running local metadata
gateway.url="http://localhost:8030"
secretstore.url="http://localhost:12001"
#secretstore.url="https://secret-store.dev-drops.com"

provider.address="0x068ed00cf0441e4829d9784fcbe7b9e26d4bd8d0"

# Contracts addresses

contract.SignCondition.address="0x8F006DbB3727d18f032C5618595ecDD2EDE13b61"
contract.HashLockCondition.address="0x19513460bc16254c74AE806683E906478A42B543"
contract.LockRewardCondition.address="0x4999A8428d1D42fc955FbBC2f1E22323a55B6f86"
contract.AccessSecretStoreCondition.address="0x3Ef2ebF03002D828943EB1AbbFC470D1A53c6B21"
contract.EscrowReward.address="0xE6E685823Ddd2e0D0B29917D84D687E5431136F6"
contract.EscrowAccessSecretStoreTemplate.address="0x3c83D8E1F1BF33Ebb1E8D5A2ac56cE482e54caCd"
contract.NeverminedToken.address="0xEEE56e2a630DD29F9A628d618E58bb173911F393"
contract.Dispenser.address="0x85cCa2B01adddCA8Df221e6027EE0D7716224202"
contract.DIDRegistry.address="0xfeA10BBb093d7fcb1EDf575Aa7e28d37b9DcFcE9"
contract.ConditionStoreManager.address="0x645439117eB378a6d35148452E287a038666Ed67"
contract.TemplateStoreManager.address="0x3a3926f3f88F1eE05164404f93FDb3887cbE8e35"
contract.AgreementStoreManager.address="0x15338ade17C4b6F65E4ff7b3aCE22AAdED00aC4d"
contract.ComputeExecutionCondition.address="0x884AAAAf48D4A7B4Dc4CB9B2cf47a150b3d535A6"
contract.EscrowComputeExecutionTemplate.address="0x02175de5A7F168517688e3E93f55936C9c2C7A19"

consume.basePath = "/tmp"

## Main account
account.main.address="0x0207cb2f99eb2e005893d6108e2633641ca9dd3e"
account.main.password="pass"
account.main.credentialsFile="/accounts/parity/0x0207cb2f99eb2e005893d6108e2633641ca9dd3e.json.testaccount"
```

And you can instantiate the API with the following lines:

```java
 Config config = ConfigFactory.load();
 NeverminedAPI neverminedAPI = NeverminedAPI.getInstance(config);
```

Remember that TypeSafe Config allows you to overwrite the values using environment variables or arguments passed to 
the JVM.

If you want to use Java's Properties, you just need to create a Properties object with the same properties of the 
`application.conf`. You can read these Properties from a properties file, or define the values of these properties 
in your code:

```java
// Default values for KEEPER_URL, KEEPER_GAS_LIMIT, KEEPER_GAS_PRICE, Nevermined_URL, SECRETSTORE_URL, CONSUME_BASE_PATH
Properties properties = new Properties();
properties.put(NeverminedConfig.MAIN_ACCOUNT_ADDRESS, "0x0207cb2f99eb2e005893d6108e2633641ca9dd3e");
properties.put(NeverminedConfig.MAIN_ACCOUNT_PASSWORD,"pass");
properties.put(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE,"/accounts/parity/0x0207cb2f99eb2e005893d6108e2633641ca9dd3e.json.testaccount");
properties.put(NeverminedConfig.DID_REGISTRY_ADDRESS,"0x4A0f7F763B1A7937aED21D63b2A78adc89c5Db23");
properties.put(NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS, "0x62f84700b1A0ea6Bfb505aDC3c0286B7944D247C");
properties.put(NeverminedConfig.LOCKREWARD_CONDITIONS_ADDRESS, "0xE30FC30c678437e0e8F78C52dE9db8E2752781a0");
properties.put(NeverminedConfig.ESCROWREWARD_CONDITIONS_ADDRESS, "0xeD4Ef53376C6f103d2d7029D7E702e082767C6ff");
properties.put(NeverminedConfig.ACCESS_SS_CONDITIONS_ADDRESS, "0x45DE141F8Efc355F1451a102FB6225F1EDd2921d");
properties.put(NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS, "0x9768c8ae44f1dc81cAA98F48792aA5730cAd2F73");
properties.put(NeverminedConfig.TOKEN_ADDRESS, "0x9861Da395d7da984D5E8C712c2EDE44b41F777Ad");
properties.put(NeverminedConfig.DISPENSER_ADDRESS, "0x865396b7ddc58C693db7FCAD1168E3BD95Fe3368");
properties.put(NeverminedConfig.PROVIDER_ADDRESS, "0x413c9ba0a05b8a600899b41b0c62dd661e689354");

NeverminedAPI NeverminedAPIFromProperties = NeverminedAPI.getInstance(properties);
```

Once you have initialized the API you can call the methods through their corresponding API class. For instance:

```java
 Balance balance = neverminedAPI.getAccountsAPI().balance(neverminedAPI.getMainAccount());

 String filesJson = metadataBase.toJson(metadataBase.base.files);
 String did = DID.builder().getHash();
 String encryptedDocument = neverminedAPI.getSecretStoreAPI().encrypt(did, filesJson, 0);

 Flowable<OrderResult> response = neverminedAPI.getAssetsAPI().order(did, SERVICE_DEFINITION_ID);
 boolean result = neverminedAPI.getAssetsAPI().consume(orderResult.getServiceAgreementId(), did, SERVICE_DEFINITION_ID, "/tmp");
```

### Using the SDK with the Nevermined Tools

If you are using [Nevermined Tools](https://github.com/keyko-io/nevermined-tools/) for playing with the Nevermined stack, 
you can use the following command to run the components necessary to have a fully functional environment:

`bash start_nevermined.sh --no-commons --local-spree-node`

After a few minutes, when Keeper has deployed the contracts, the ABI files describing the Smart Contracts can be found 
in the `${HOME}/.nevermined/nevermined-contracts/artifacts/` folder. Depending on the network you are using, each ABI includes the 
address where the Smart Contract is deployed in each network.

If you want to run the integration tests on your local machine, you can execute the Bash script `src/test/resources/scripts/updateConfAddresses.sh`
to update the addresses to use in your `src/test/resources/application.conf` file.

### Dealing with Flowables

The library uses web3j to interact with Solidity's Smart Contracts. It relies on 
[RxJava](https://github.com/ReactiveX/RxJava) to deal with asynchronous calls.

The order method in AssetsAPI returns a Flowable over an OrderResult object. It's your choice if you want to handle 
this in a synchronous or asynchronous fashion.

If you prefer to deal with this method in a synchronous way, you will need to block the current thread until you get 
a response:

```java
 Flowable<OrderResult> response = neverminedAPI.getAssetsAPI().order(did, SERVICE_DEFINITION_ID);
 OrderResult orderResult = response.blockingFirst();
```

On the contrary, if you want to handle the response asynchronously, you will need to subscribe to the Flowable:

```java
response.subscribe(
     orderResultEvent -> {
         if (orderResultEvent.isAccessGranted())
             System.out.println("Access Granted for Service Agreement " + orderResultEvent.getServiceAgreementId());
         else if (orderResultEvent.isRefund())
             System.out.println("There was a problem with Service Agreement " + orderResultEvent.getServiceAgreementId() + " .Payment Refund");
     }
 );
```

The subscribe method will launch a new Thread to react to the events of the Flowable.
More information: [RxJava](https://github.com/ReactiveX/RxJava/wiki) , [Flowable](http://reactivex.io/RxJava/2.x/javadoc/)

## Documentation

All the API documentation is hosted on javadoc.io:

- **[https://www.javadoc.io/doc/io.keyko.nevermined/api](https://www.javadoc.io/doc/io.keyko.nevermined/api)**

You can also generate the Javadoc locally using the following command:

```bash
mvn javadoc:javadoc
```

## Testing

You can run both, the unit and integration tests by using:

```bash
mvn clean verify -P all-tests
```

### Unit Tests

You can execute the unit tests only using the following command:

```bash
mvn clean test
```

### Integration Tests

The execution of the integration tests require to have running the complete Nevermined stack using 
[Nevermined Tools](https://github.com/keyko-io/nevermined-tools/).

After having the tools in your environment, you can run the components needed running:

```bash
KEEPER_VERSION=v0.13.2 bash start_nevermined.sh --latest --no-commons --force-pull
```

If you have older versions of the docker images is recommended to delete all them to be sure you are running the 
last version of the stack.

You can execute the integration tests using the following command:

```bash
mvn clean verify -P integration-test
```

### Documentation

You can generate the Javadoc using the following command:

```bash
mvn javadoc:javadoc
```

### All the tests

You can run the unit and integration tests running:

```bash
mvn clean verify -P all-tests
```

You can run the integration tests in Nile environment using the command:
```bash
mvn verify  -P integration-test -Dconfig.file=src/test/resources/networks/integration-application.conf
```

### Code Coverage

The code coverage reports are generated using the JaCoCo Maven plugin. Reports are generated in the `target/site` 
folder.

## New Release

The `bumpversion.sh` script helps to bump the project version. You can execute the script using as first argument 
{major|minor|patch} to bump accordingly the version.

## Attribution

This library is based in the [Ocean Protocol](https://oceanprotocol.com) [Squid Java](https://github.com/oceanprotocol/squid-java) library.
It keeps the same Apache v2 License and adds some improvements. See [NOTICE file](NOTICE).

## License

```
Copyright 2020 Keyko GmbH
This product includes software developed at
BigchainDB GmbH and Ocean Protocol (https://www.oceanprotocol.com/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
