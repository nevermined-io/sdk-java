package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.AccountsAPI;
import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.external.FaucetService;
import io.keyko.nevermined.manager.AccountsManager;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.Balance;
import io.keyko.nevermined.models.faucet.FaucetResponse;
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
    public FaucetResponse requestEthFromFaucet(String address) throws ServiceException {
        return FaucetService.requestEthFromFaucet(this.accountsManager.getFaucetUrl(), address);
    }


    @Override
    public TransactionReceipt requestTokens(BigInteger amount) throws EthereumException {

        return accountsManager.requestTokens(amount);
    }
}
