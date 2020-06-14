package ru.ifmo.test.common.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface Account extends Remote {
    /**
     * @return {@link String} - account identifier
     */
    String getId() throws RemoteException;

    /**
     * @return - money in the account
     */
    int getAmount() throws RemoteException;

    /**
     * @param amount - new amount of money in the account
     */
    void setAmount(int amount)
            throws RemoteException;
}
