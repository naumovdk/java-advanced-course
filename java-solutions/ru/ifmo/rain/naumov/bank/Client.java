package ru.ifmo.rain.naumov.bank;

import ru.ifmo.test.common.bank.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client implements BankClient {
    private Bank bank;
    private BankServer server;

    @Override
    public void start(int port) {
        server = new Server();
        server.start(port);
        try {
            Registry registry = LocateRegistry.getRegistry(null, port);
            bank = (Bank) registry.lookup("//localhost/bank");
        } catch (RemoteException | NotBoundException e) {
            System.out.println("couldn't start server: " + e.getCause());
        }
    }

    @Override
    public int change(String name, String surname, String passport, String accountName, String diff) throws RemoteException {
        Person person = bank.createPerson(name, surname, passport);
        if (person == null) {
            person = bank.createPerson(name, surname, passport);
        }
        if (!(name.equals(person.getName()) && surname.equals(person.getSurname()))) {
            return 0;
        }
        Account account = bank.createPersonAccount(accountName, person).getAccount(accountName);
        account.setAmount(account.getAmount() + Integer.parseInt(diff));
        return account.getAmount();
    }

    @Override
    public void close() {
        server.close();
    }
}
