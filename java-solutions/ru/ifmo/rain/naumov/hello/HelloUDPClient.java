package ru.ifmo.rain.naumov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static ru.ifmo.rain.naumov.hello.Utils.*;

// java -cp . -p . -m info.kgeorgiy.java.advanced.hello client ru.ifmo.rain.naumov.hello.HelloUDPClient

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService threadPool = newFixedThreadPool(threads);
        for (int t = 0; t < threads; ++t) {
            try {
                threadPool.submit(task(InetAddress.getByName(host), port, prefix, t, requests));
            } catch (UnknownHostException e) {
                System.out.println("Unknown host");
                e.printStackTrace();
            }
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(threads * requests, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Runnable task(InetAddress host, int port, String prefix, int thread, int requests) {
        return () -> {
            for (int r = 0; r < requests; r++) {
                byte[] message = message(prefix, thread, r);
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(5000);
                    DatagramPacket toBeSent = new DatagramPacket(message, message.length, host, port);
                    DatagramPacket toBeReceived = newDatagramPacket(new byte[socket.getReceiveBufferSize()]);
                    sendAndReceiveAndLog(socket, toBeSent, toBeReceived, message);
                } catch (SocketException ignored) {
                    System.out.println("Unable to establish connection");
                }
            }
        };
    }

    private void sendAndReceiveAndLog(DatagramSocket socket, DatagramPacket toBeSent, DatagramPacket toBeReceived, byte[] expected) {
        try {
            socket.send(toBeSent);
            toBeReceived.setData(new byte[socket.getReceiveBufferSize()]);
            socket.receive(toBeReceived);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            sendAndReceiveAndLog(socket, toBeSent, toBeReceived, expected);
        }
        if (isOkResponse(toBeReceived, expected)) {
            log(toBeReceived);
        } else {
            sendAndReceiveAndLog(socket, toBeSent, toBeReceived, expected);
        }
    }

    private boolean isOkResponse(DatagramPacket response, byte[] expected) {
        return getData(response).contains(new String(expected, CHARSET));
    }

    private void log(DatagramPacket packet) {
        System.out.println("Received: " + getData(packet));
    }

    private byte[] message(String prefix, int thread, int requestNumber) {
        return (prefix + thread + "_" + requestNumber).getBytes(CHARSET);
    }
}
