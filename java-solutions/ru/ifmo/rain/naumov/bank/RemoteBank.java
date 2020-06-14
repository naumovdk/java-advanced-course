package ru.ifmo.rain.naumov.bank;

import ru.ifmo.test.common.bank.Account;
import ru.ifmo.test.common.bank.Bank;
import ru.ifmo.test.common.bank.Person;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteBank implements Bank {
    private final ConcurrentHashMap<String, RemoteAccount> accounts;
    private final ConcurrentHashMap<String, RemotePerson> persons;

    public RemoteBank() {
        super();
        this.accounts = new ConcurrentHashMap<>();
        this.persons = new ConcurrentHashMap<>();
    }

    public RemoteBank(ConcurrentHashMap<String, RemoteAccount> accounts, ConcurrentHashMap<String, RemotePerson> persons) {
        super();
        this.accounts = accounts;
        this.persons = persons;
    }

    @Override
    public Account createAccount(String id) throws RemoteException {
        if (accounts.containsKey(id))
            return accounts.get(id);
        RemoteAccount account = new RemoteAccount(id);
        accounts.put(id, account);
        return account;
    }

    @Override
    public Account getAccount(String id) throws RemoteException {
        return accounts.get(id);
    }

    @Override
    public Person createPersonAccount(String accountName, Person remotePerson) throws RemoteException {
        if (remotePerson.getAccount(accountName) == null) {
            Account account;

            if (remotePerson instanceof LocalPerson) {
                account = new LocalAccount(remotePerson.getPassport() + ":" + accountName);
            } else {
                account = createAccount(remotePerson.getPassport() + ":" + accountName);
            }
            remotePerson.addAccount(accountName, account);
        }
        return remotePerson;
    }

    @Override
    public Person createPerson(String name, String surname, String passport) throws RemoteException {
        if (persons.containsKey(passport))
            return persons.get(passport);
        RemotePerson person = new RemotePerson(name, surname, passport);
        persons.put(passport, person);
        return person;
    }

    @Override
    public Person getLocalPerson(String passport) throws RemoteException {
        RemotePerson remotePerson = persons.get(passport);
        return remotePerson == null ? null : new LocalPerson(remotePerson);
    }

    @Override
    public Person getRemotePerson(String passport) throws RemoteException {
        return persons.get(passport);
    }
}
