package ru.ifmo.test.common.bank;

/**
 * @author Eugene Geny200
 * @see AutoCloseable
 */
public interface BankServer extends AutoCloseable {
    /**
     * Creates an instance of the interface {@link Bank} and opens RMI on port.
     *
     * @param port - port for RMI.
     * @see Bank
     */
    void start(int port);

    /**
     * Stops server and deallocate all resources.
     */
    @Override
    void close();
}
