package ru.ifmo.test.common.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * @author Eugene Geny200
 * @author Egor
 */
public interface Person extends Remote {

    /**
     * @return {@link String} - last name of a person.
     */
    String getSurname()
            throws RemoteException;

    /**
     * @return {@link String} - passport of a person.
     */
    String getPassport()
            throws RemoteException;

    /**
     * @return {@link String} - first name of a person.
     */
    String getName()
            throws RemoteException;

    /**
     * @param accountName - account identifier
     * @return {@link Account} - person account.
     */
    Account getAccount(String accountName)
            throws RemoteException;

    Map<String, Account> getAccounts() throws RemoteException;

    void addAccount(String name, Account account) throws RemoteException;
}
