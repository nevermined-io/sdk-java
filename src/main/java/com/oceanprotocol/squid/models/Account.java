/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

public class Account {


    public String address;

    public Balance balance;

    public String password;

    private Account() {
    }

    public Account(String address, String password) {

        this(address, password, new Balance());
    }

    public Account(String address) {

        this(address, null, new Balance());
    }

    public Account(String address, String password, Balance balance) {
        this.address = address;
        this.password = password;
        this.balance = balance;
    }

    public String getId() {
        return this.address;
    }

    @Override
    public String toString() {
        return "Account{" +
                "address='" + address + '\'' +
                ", balance=" + balance +
                '}';
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Balance getBalance() {
        return balance;
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}