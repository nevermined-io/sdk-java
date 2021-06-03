package io.keyko.nevermined.core.conditions;

import io.keyko.nevermined.contracts.LockPaymentCondition;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LockPaymentConditionPayable extends LockPaymentCondition {

    protected LockPaymentConditionPayable(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(contractAddress, web3j, credentials, contractGasProvider);
    }

    protected LockPaymentConditionPayable(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static LockPaymentConditionPayable load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new LockPaymentConditionPayable(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static LockPaymentConditionPayable load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new LockPaymentConditionPayable(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> fulfill(byte[] _agreementId, byte[] _did, String _rewardAddress, String _tokenAddress, List<BigInteger> _amounts, List<String> _receivers, BigInteger weiValue) {
        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function("fulfill", Arrays.asList(new Bytes32(_agreementId), new Bytes32(_did), new Address(_rewardAddress), new Address(_tokenAddress), new DynamicArray(Uint256.class, Utils.typeMap(_amounts, Uint256.class)), new DynamicArray(Address.class, Utils.typeMap(_receivers, Address.class))), Collections.emptyList());
        return this.executeRemoteCallTransaction(function, weiValue);
    }
}
