package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.Balance;
import io.keyko.nevermined.models.faucet.FaucetResponse;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

/**
 * Exposes the Public API related with the management of Accounts
 */
public interface AccountsAPI {

    /**
     * Returns a list of the accounts registered in Keeper
     *
     * @return a List of all Account registered in Keeper
     * @throws EthereumException EthereumException
     */
    List<Account> list() throws EthereumException;

    /**
     * Returns the Balance of an account
     *
     * @param account the account we want to get the balance
     * @return the Balance of the account
     * @throws EthereumException EthereumException
     */
    Balance balance(Account account) throws EthereumException;

    /**
     * Requests Ether to faucet for paying transactions gas
     *
     * @param address the account address requesting ETH
     * @return FaucetResponse response status and message
     * @throws ServiceException ServiceException
     */
    FaucetResponse requestEthFromFaucet(String address) throws ServiceException;

    /**
     * Requests Nevermined Tokens
     *
     * @param amount the amount of tokens
     * @return a TransactionReceipt from the transaction sent to the smart contract
     * @throws EthereumException EthereumException
     */
    TransactionReceipt requestTokens(BigInteger amount) throws EthereumException;

}
