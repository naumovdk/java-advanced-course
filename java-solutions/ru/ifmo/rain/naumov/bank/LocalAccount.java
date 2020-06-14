package ru.ifmo.rain.naumov.bank;

import ru.ifmo.test.common.bank.Account;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount implements Account, Serializable {
    private int amount;
    private final String id;

    public LocalAccount(String id) {
        this.id = id;
    }

    public LocalAccount(String id, int amount) {
        this.amount = amount;
        this.id = id;
    }

    public LocalAccount(Account remoteAccount) throws RemoteException {
        this.id = remoteAccount.getId();
        this.amount = remoteAccount.getAmount();
    }

    @Override
    public String getId() throws RemoteException {
        return this.id;
    }

    @Override
    public int getAmount() throws RemoteException {
        return this.amount;
    }

    @Override
    public void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }
}
