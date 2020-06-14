package ru.ifmo.test.common.bank;

import java.rmi.RemoteException;

/**
 * Client for Bank
 *
 * @author Eugene Geny200
 * @see AutoCloseable
 * @see Bank
 */
public interface BankClient extends AutoCloseable {
    /**
     * Creates an instance of the interface {@link Bank} and opens RMI on port.
     *
     * @param port - port for RMI.
     * @see Bank
     */
    void start(int port);

    /**
     * Change the amount of the account and returns a new amount.
     *
     * @param name         {@link String} - first name of a person.
     * @param surname      {@link String} - last name of a person.
     * @param passport     {@link String} - passport of a person.
     * @param accountName  {@link String} - person account id.
     * @param modification {@link String} - change in invoice amount.
     * @return - returns the amount of money in a person’s account.
     * @see Person
     * @see Account
     */
    int change(String name, String surname, String passport, String accountName, String modification) throws RemoteException;

    /**
     * Stops server and deallocate all resources.
     */
    @Override
    void close();
}
