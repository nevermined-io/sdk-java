package io.keyko.nevermind.api.impl;

import io.keyko.nevermind.api.AccountsAPI;
import io.keyko.nevermind.exceptions.EthereumException;
import io.keyko.nevermind.manager.AccountsManager;
import io.keyko.nevermind.models.Account;
import io.keyko.nevermind.models.Balance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

/**
 * Implementation of AccountsAPI
 */
public class AccountsImpl implements AccountsAPI {

    private AccountsManager accountsManager;


    /**
     * Constructor
     *
     * @param accountsManager the accountsManager
     */
    public AccountsImpl(AccountsManager accountsManager) {

        this.accountsManager = accountsManager;
    }

    @Override
    public List<Account> list() throws EthereumException {

        return accountsManager.getAccounts();
    }

    @Override
    public Balance balance(Account account) throws EthereumException {

        return accountsManager.getAccountBalance(account.address);
    }

    @Override
    public TransactionReceipt requestTokens(BigInteger amount) throws EthereumException {

        return accountsManager.requestTokens(amount);
    }
}
