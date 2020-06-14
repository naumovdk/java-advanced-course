package ru.ifmo.test.common.bank;

import info.kgeorgiy.java.advanced.base.info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Tests for the server.
 *
 * @author Eugene Geny200
 * @author Egor
 * @see BaseTest
 */
public class BankClientTest extends BaseTest {
    private final String passport = "998244353";
    private final String name = "Smite";
    private final String surname = "Cowley";
    private final String accountName = "accountName";

    private static int port = 28888;

    @FunctionalInterface
    public interface ConsumerWithRemoteException<T, U> {
        void apply(T t, U u) throws RemoteException, InterruptedException;
    }

    @Test
    public void testWrongPerson() {
        test(((bank, bankClient) -> {
            String amount = "123";
            Assert.assertEquals(Integer.parseInt(amount), bankClient.change(name, surname, passport, accountName, amount));

            bankClient.change(name + 1, surname, passport, accountName, amount + 14);
            Assert.assertEquals(Integer.parseInt(amount), bankClient.change(name, surname, passport, accountName, "0"));

            bankClient.change(name, surname + 1, passport, accountName, amount + 13);
            Assert.assertEquals(Integer.parseInt(amount), bankClient.change(name, surname, passport, accountName, "0"));

            bankClient.change(name, surname, passport, accountName + 1, amount + 12);
            Assert.assertEquals(Integer.parseInt(amount), bankClient.change(name, surname, passport, accountName, "0"));

            bankClient.change(name, surname, passport + 1, accountName, amount + 1);
            Assert.assertEquals(Integer.parseInt(amount), bankClient.change(name, surname, passport, accountName, "0"));

            Person remotePerson = bank.getRemotePerson(passport);
            Account account = remotePerson.getAccount(accountName);
            Assert.assertEquals(passport + ':' + accountName, account.getId());
            Assert.assertEquals(Integer.parseInt(amount), account.getAmount());

            Person secondPerson = bank.getRemotePerson(passport + 1);
            Account secondAccount = secondPerson.getAccount(accountName);
            Assert.assertEquals(passport + 1 + ':' + accountName, secondAccount.getId());
            Assert.assertEquals(Integer.parseInt(amount + 1), secondAccount.getAmount());
        }));
    }

    @Test
    public void testClient() {
        test(((bank, bankClient) -> {
            String amount = "100";
            Assert.assertEquals(Integer.parseInt(amount), bankClient.change(name, surname, passport, accountName, amount));
            Person remotePerson = bank.getRemotePerson(passport);
            Account account = remotePerson.getAccount(accountName);
            Assert.assertEquals(passport + ':' + accountName, account.getId());
            Assert.assertEquals(Integer.parseInt(amount), account.getAmount());
            bankClient.change(name, surname, passport, accountName, amount);
            Assert.assertEquals(2 * Integer.parseInt(amount), account.getAmount());
        }));
    }

    private static void test(ConsumerWithRemoteException<Bank, BankClient> bankConsumer) {
        final int port = BankClientTest.port++;

        try (BankClient client = createCUT()) {
            client.start(port);
            Bank bank;
            try {
                Registry registry = LocateRegistry.getRegistry(null, port);
                bank = (Bank) registry.lookup("//localhost/bank");
                bankConsumer.apply(bank, client);
            } catch (NotBoundException e) {
                Assert.fail("Server wasn't started on port: " + port);
            } catch (RemoteException e) {
                Assert.fail("RemoteException: " + e.getMessage());
            } catch (InterruptedException e) {
                Assert.fail("InterruptedException: " + e.getMessage());
            }
        }
    }
}
