# Ocean Keeper Contracts

Ocean Protocol doesn't provide an updated package with the Keeper Contract stubs in Java format.
This script uses the ABI's to generate the stubs.

To generate the stubs you need to have the Ocean Protocol Keeper Contracts locally and run:

```bash
keeper/generate-keeper-artifact.sh
cd keeper
mvn clean install
```

The artifact generated can be used as a Java dependency in your project.

```xml
        <!-- Ocean Keeper Contracts: Web3j stubs & ABI's -->
        <dependency>
            <groupId>io.keyko.ocean.keeper</groupId>
            <artifactId>contracts</artifactId>
            <version>${ocean.keeper.version}</version>
        </dependency>
```
