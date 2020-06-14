package ru.ifmo.rain.naumov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static ru.ifmo.rain.naumov.hello.Utils.*;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService threadPool;
    private ExecutorService handler;

    @Override
    public void start(int port, int threads) {
        try {
            threadPool = newFixedThreadPool(threads);
            socket = new DatagramSocket(port);
            socket.setSoTimeout(500);
            handler = newSingleThreadExecutor();
            handler.submit(this::handle);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void handle() {
        while (!socket.isClosed()) {
            try {
                DatagramPacket toBeReceived = newDatagramPacket(new byte[socket.getReceiveBufferSize()]);
                socket.receive(toBeReceived);
                threadPool.submit(() -> respond(toBeReceived));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void respond(DatagramPacket received) {
        try {
            received.setData(message(getData(received)));
            socket.send(received);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    byte[] message(String content) {
        return ("Hello, " + content).getBytes(CHARSET);
    }

    @Override
    public void close() {
        socket.close();
        handler.shutdownNow();
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(100, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
