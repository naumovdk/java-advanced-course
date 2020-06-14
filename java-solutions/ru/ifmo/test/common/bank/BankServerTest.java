package ru.ifmo.test.common.bank;

import info.kgeorgiy.java.advanced.base.info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Tests for the server.
 *
 * @author Egor
 * @author Eugene Geny200
 * @see BaseTest
 */
public class BankServerTest extends BaseTest {
    private final String passport = "998244353";
    private final String name = "Smite";
    private final String surname = "Cowley";
    private final String accountName = "accountName";

    private static int port = 28888;

    @FunctionalInterface
    public interface ConsumerWithRemoteException<T> {
        void apply(T t) throws RemoteException, InterruptedException;
    }

    @Test
    public void createAccountAndChangeAmount() {
        test(bank -> {
            bank.createAccount("h̵̨̭̳͚̭͔̞̯͛͗͑̚:̨̡̲̪̭");
            Account account = bank.getAccount("h̵̨̭̳͚̭͔̞̯͛͗͑̚:̨̡̲̪̭");
            account.setAmount(500);
            Assert.assertEquals(bank.getAccount("h̵̨̭̳͚̭͔̞̯͛͗͑̚:̨̡̲̪̭").getAmount(), 500);
        });
    }

    @Test
    public void createLocalPersonAccountAndChangeAmount() {
        test(bank -> {
            bank.createPerson(name, surname, passport);
            Person localPerson = bank.createPersonAccount(accountName, bank.getLocalPerson(passport));
            Assert.assertNotNull(localPerson.getAccount(accountName));
            Assert.assertNull(bank.getLocalPerson(passport).getAccount(accountName));
            Assert.assertNull(bank.getRemotePerson(passport).getAccount(accountName));
            Assert.assertNull(bank.getAccount(passport + ':' + accountName));
            Account account = localPerson.getAccount(accountName);
            Assert.assertEquals(0, account.getAmount());
            account.setAmount(500);
            Assert.assertEquals(500, account.getAmount());
        });
    }

    @Test
    public void createOneAccountRemotePersonAndChange() {
        test(bank -> {
            Person remotePerson = bank.createPerson(name, surname, passport);
            Person person = bank.createPersonAccount(accountName, remotePerson);

            Assert.assertNotNull(person.getAccount(accountName));
            Assert.assertNotNull(remotePerson.getAccount(accountName));

            Person localPerson = bank.getLocalPerson(passport);
            Account localAccount = localPerson.getAccount(accountName);
            Account account = remotePerson.getAccount(accountName);
            Assert.assertEquals(passport + ':' + accountName, account.getId());
            Assert.assertEquals(passport + ':' + accountName, localAccount.getId());
            Assert.assertEquals(0, account.getAmount());
            account.setAmount(200);
            Assert.assertEquals(200, bank.getAccount(account.getId()).getAmount());
            Assert.assertEquals(0, localAccount.getAmount());
        });
    }

    @Test
    public void createImaginaryManyAccount() {
        test(bank -> {
            final Person remotePerson = bank.createPerson(name, surname, passport);
            ExecutorService service = Executors.newFixedThreadPool(100);
            BlockingQueue<Account> blockingQueue = new LinkedBlockingQueue<>();
            for (int i = 0; i != 100; ++i)
                service.submit(() -> {
                    try {
                        Person person = bank.createPersonAccount(accountName, remotePerson);
                        Assert.assertNotNull(person);
                        blockingQueue.put(person.getAccount(accountName));
                    } catch (RemoteException e) {
                        Assert.fail("RemoteException: " + e.getMessage());
                    } catch (InterruptedException e) {
                        Assert.fail("InterruptedException: " + e.getMessage());
                    }
                });
            service.awaitTermination(1000, TimeUnit.MILLISECONDS);
            Assert.assertEquals(100, blockingQueue.size());
            final Person localPerson = bank.getLocalPerson(passport);
            for (int i = 0; i != 100; ++i) {
                Account account = blockingQueue.take();
                Assert.assertNotNull(account);
                Assert.assertEquals(i, account.getAmount());
                account.setAmount(i + 1);
            }
            for (int i = 0; i != 100; ++i) {
                Account account = localPerson.getAccount(accountName);
                Assert.assertNotNull(account);
                Assert.assertEquals(i, account.getAmount());
                account.setAmount(i + 1);
            }
        });
    }

    @Test
    public void createReallyManyAccount() {
        test(bank -> {
            final Person remotePerson = bank.createPerson(name, surname, passport);
            ExecutorService service = Executors.newFixedThreadPool(100);
            BlockingQueue<Account> blockingQueue = new LinkedBlockingQueue<>();
            for (int i = 0; i != 100; ++i) {
                int finalI = i;
                service.submit(() -> {
                    try {
                        Person person = bank.createPersonAccount(accountName + finalI, remotePerson);
                        //Assert.assertNotNull(person);
                        blockingQueue.put(person.getAccount(accountName + finalI));
                    } catch (RemoteException e) {
                        Assert.fail("RemoteException: " + e.getMessage());
                    } catch (InterruptedException e) {
                        Assert.fail("InterruptedException: " + e.getMessage());
                    }
                });
            }
            service.awaitTermination(1000, TimeUnit.MILLISECONDS);
            Assert.assertEquals(100, blockingQueue.size());
            Person localPerson = bank.getLocalPerson(passport);
            for (int i = 0; i != 100; ++i) {
                Account account = blockingQueue.take();
                Assert.assertNotNull(account);
                Assert.assertEquals(0, account.getAmount());
                account.setAmount(i + 1);
            }

            for (int i = 0; i != 100; ++i) {
                Account account = localPerson.getAccount(accountName + i);
                Assert.assertNotNull(account);
                Assert.assertEquals(0, account.getAmount());
                account.setAmount(i + 1);
            }
        });
    }

    @Test
    public void createPerson() {
        test(bank -> {
            Assert.assertNull(bank.getRemotePerson(passport));
            bank.createPerson(name, surname, passport);
            Assert.assertNotNull(bank.getRemotePerson(passport));
        });
    }

    @Test
    public void createOneRemotePerson() {
        test(bank -> {
            bank.createPerson(name, surname, passport);
            Person remotePerson = bank.getRemotePerson(passport);
            Assert.assertEquals(remotePerson.getPassport(), passport);
            Assert.assertEquals(remotePerson.getName(), name);
            Assert.assertEquals(remotePerson.getSurname(), surname);
        });
    }

    @Test
    public void createOneRemoteLocalPerson() {
        test(bank -> {
            bank.createPerson(name, surname, passport);
            Person localPerson = bank.getLocalPerson(passport);
            Assert.assertEquals(localPerson.getPassport(), passport);
            Assert.assertEquals(localPerson.getName(), name);
            Assert.assertEquals(localPerson.getSurname(), surname);
        });
    }

    @Test
    public void createManyPerson() {
        test(bank -> {
            int count = 1000;
            for (int i = 0; i < count; i++) {
                bank.createPerson(name + i, surname + i, passport + i);
                Person remotePerson = bank.getRemotePerson(passport + i);
                Assert.assertEquals(remotePerson.getPassport(), passport + i);
                Assert.assertEquals(remotePerson.getName(), name + i);
                Assert.assertEquals(remotePerson.getSurname(), surname + i);
                Person localPerson = bank.getLocalPerson(passport + i);
                Assert.assertEquals(localPerson.getPassport(), passport + i);
                Assert.assertEquals(localPerson.getName(), name + i);
                Assert.assertEquals(localPerson.getSurname(), surname + i);
            }
        });
    }

    @Test
    public void createOneAccountRemotePerson() {
        test(bank -> {
            bank.createPerson(name, surname, passport);
            Person remotePerson = bank.getRemotePerson(passport);
            bank.createPersonAccount(accountName, remotePerson);
            Account account = remotePerson.getAccount(accountName);
            Assert.assertEquals(account.getId(), passport + ':' + accountName);
            Assert.assertEquals(account.getAmount(), 0);
        });
    }

    @Test
    public void createManyAccountRemotePerson() {
        test(bank -> {
            bank.createPerson(name, surname, passport);
            Person remotePerson = bank.getRemotePerson(passport);
            Person localPerson = bank.getLocalPerson(passport);
            for (int i = 0; i < 1000; i++) {
                bank.createPersonAccount(accountName + i, remotePerson);
                Account account = remotePerson.getAccount(accountName + i);
                Assert.assertEquals(account.getId(), passport + ':' + accountName + i);
                Assert.assertEquals(account.getAmount(), 0);
                account = localPerson.getAccount(accountName + i);
                Assert.assertNull(account);
            }
        });
    }

    @Test
    public void setAccountAmountRemotePersonAfterLocalPerson() {
        test(bank -> {
            int amount = 500;
            bank.createPerson(name, surname, passport);
            Person remotePerson = bank.getRemotePerson(passport);
            Account account = bank.createPersonAccount(accountName, remotePerson).getAccount(accountName);
            Assert.assertNotNull(account);
            Person localPerson = bank.getLocalPerson(passport);
            account.setAmount(amount);
            Assert.assertEquals(remotePerson.getAccount(accountName).getAmount(), amount);
            Assert.assertEquals(localPerson.getAccount(accountName).getAmount(), 0);
        });
    }

    @Test
    public void setAccountAmountRemotePersonBeforeLocalPerson() {
        test(bank -> {
            int amount = 500;
            bank.createPerson(name, surname, passport);
            Person remotePerson = bank.getRemotePerson(passport);
            Account account = bank.createPersonAccount(accountName, remotePerson).getAccount(accountName);
            Assert.assertNotNull(account);
            account.setAmount(amount);
            Person localPerson = bank.getLocalPerson(passport);
            Assert.assertEquals(remotePerson.getAccount(accountName).getAmount(), amount);
            Assert.assertEquals(localPerson.getAccount(accountName).getAmount(), amount);
        });
    }

    @Test
    public void setAccountAmountLocalPerson() {
        test(bank -> {
            int amount = 500;
            bank.createPerson(name, surname, passport);
            Person remotePerson = bank.getRemotePerson(passport);
            bank.createPersonAccount(accountName, remotePerson);
            Person localPerson = bank.getLocalPerson(passport);
            Account account = localPerson.getAccount(accountName);
            account.setAmount(amount);
            Assert.assertEquals(remotePerson.getAccount(accountName).getAmount(), 0);
            Assert.assertEquals(localPerson.getAccount(accountName).getAmount(), amount);
        });
    }

    @Test
    public void createManyAccountRemotePersonsAndSetAmountMultiThreading() {
        test(bank -> {
            final int countOfPersons = 100;
            final int countOfAccount = 10;
            final int amount = 50;
            ExecutorService streams = Executors.newFixedThreadPool(countOfAccount);
            Phaser phaser = new Phaser(1);
            Set<Account>[] set = new Set[countOfAccount];
            for (int i = 0; i < countOfAccount; i++) {
                set[i] = ConcurrentHashMap.newKeySet();
            }
            for (int i = 0; i < countOfPersons; i++) {
                Person remotePerson = bank.createPerson(name + i, surname + i, passport + i);
                for (int j = 0; j < countOfAccount; j++) {
                    Person person = bank.createPersonAccount(accountName + j, remotePerson);
                    Assert.assertNotNull(person);
                    Assert.assertNotNull(person.getAccount(accountName + j));
                    Assert.assertNotNull(remotePerson.getAccount(accountName + j));
                    set[j].add(remotePerson.getAccount(accountName + j));
                }
            }
            for (int i = 0; i < countOfAccount; i++) {
                phaser.register();
                final int j = i;
                streams.submit(() -> {
                    set[j].forEach(account -> {
                                while (true) {
                                    try {
                                        account.setAmount(account.getAmount() + amount);
                                        break;
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    );
                    phaser.arrive();
                });
            }
            phaser.arriveAndAwaitAdvance();
            for (int i = 0; i < countOfPersons; i++) {
                Person remotePerson = bank.getRemotePerson(passport + i);
                for (int j = 0; j < countOfAccount; j++) {
                    Assert.assertEquals(remotePerson.getAccount(accountName + j).getAmount(), amount);
                }
            }
        });
    }

    private static void test(ConsumerWithRemoteException<Bank> bankConsumer) {
        final int port = BankServerTest.port++;

        try (BankServer server = createCUT()) {
            server.start(port);
            Bank bank;
            try {
                Registry registry = LocateRegistry.getRegistry(null, port);
                bank = (Bank) registry.lookup("//localhost/bank");
                bankConsumer.apply(bank);
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
