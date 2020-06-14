package ru.ifmo.rain.naumov.bank;

import ru.ifmo.test.common.bank.Account;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteAccount extends UnicastRemoteObject implements Account {
    private final String id;
    private int amount;

    public RemoteAccount(String id, int amount) throws RemoteException {
        super();
        this.id = id;
        this.amount = amount;
    }

    public RemoteAccount(String id) throws RemoteException {
        super();
        this.id = id;
        this.amount = 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    synchronized public int getAmount() throws RemoteException {
        return amount;
    }

    @Override
    synchronized public void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }
}
