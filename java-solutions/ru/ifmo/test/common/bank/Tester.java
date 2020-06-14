package ru.ifmo.test.common.bank;

import info.kgeorgiy.java.advanced.base.info.kgeorgiy.java.advanced.base.BaseTester;

/**
 * Runs tests for the bank.
 *
 * @author Eugene Geny200
 */
public class Tester extends BaseTester {
    /**
     * Starts testing.
     * Use to start:
     * <ul>
     *         <li> {@code java <class_name> client <canonical_class_name>}
     *         calls {@link BankClientTest}
     *         </li>
     *         <li> {@code java <class_name> bank <canonical_class_name>}
     *         calls {@link BankServerTest}
     *         </li>
     * </ul>
     *
     * @param args array of input parameters ({@link String}).
     * @see BankServerTest
     * @see BankClientTest
     * @see BankServer
     * @see BankClient
     */
    public static void main(final String... args) {
        new Tester().add("bank", BankServerTest.class).add("client", BankClientTest.class).add("client", BankClientTest.class).run(args);
    }
}
