package ru.ifmo.rain.naumov.bank;

import ru.ifmo.test.common.bank.Account;
import ru.ifmo.test.common.bank.Person;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RemotePerson extends UnicastRemoteObject implements Person {
    private final String name;
    private final String surname;
    private final String passport;
    private final HashMap<String, Account> accounts;

    public RemotePerson(String name, String surname, String passport) throws RemoteException {
        super();
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accounts = new HashMap<>();
    }

    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    @Override
    public String getPassport() throws RemoteException {
        return passport;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    synchronized public Account getAccount(String accountName) throws RemoteException {
        return accounts.get(accountName);
    }

    public Map<String, Account> getAccounts() throws RemoteException {
        return accounts;
    }

    @Override
    public void addAccount(String name, Account account) throws RemoteException {
        accounts.put(name, account);
    }
}
