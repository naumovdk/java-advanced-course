package ru.ifmo.rain.naumov.bank;

import ru.ifmo.test.common.bank.Bank;
import ru.ifmo.test.common.bank.BankServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements BankServer {
    private Bank bank;
    private Registry registry;

    @Override
    public void start(int port) {
        try {
            bank = new RemoteBank();
            Bank remoteBank = (Bank) UnicastRemoteObject.exportObject(bank, 0);
            registry = LocateRegistry.createRegistry(port);
            registry.rebind("//localhost/bank", remoteBank);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            registry.unbind("//localhost/bank");
            UnicastRemoteObject.unexportObject(bank, true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (RemoteException | NotBoundException ignored) {

        }
    }
}
