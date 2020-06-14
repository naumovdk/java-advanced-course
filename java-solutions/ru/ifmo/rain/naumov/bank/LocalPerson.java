package ru.ifmo.rain.naumov.bank;

import ru.ifmo.test.common.bank.Account;
import ru.ifmo.test.common.bank.Person;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPerson implements Serializable, Person {
    private final String name;
    private final String surname;
    private final String passport;
    private final Map<String, Account> accounts;

    public LocalPerson(Person person) throws RemoteException {
        surname = person.getSurname();
        passport = person.getPassport();
        name = person.getName();
        accounts = new ConcurrentHashMap<>();
//        person.getAccounts().values().forEach(a -> {
//            try {
//                accounts.put(a.getId(), new LocalAccount(a));
//            } catch (RemoteException ignored) {}
//        });
        for (var remoteAccount : person.getAccounts().entrySet()) {
            accounts.put(remoteAccount.getKey(), new LocalAccount(remoteAccount.getValue()));
        }
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
    public Account getAccount(String accountName) {
        return accounts.get(accountName);
    }

    @Override
    public Map<String, Account> getAccounts() {
        return accounts;
    }

    @Override
    public void addAccount(String name, Account account) {
        accounts.put(name, account);
    }
}
