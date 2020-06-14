package ru.ifmo.rain.naumov.hello;

import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    static String getData(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), CHARSET);
    }

    public static DatagramPacket newDatagramPacket(byte[] data) {
        return new DatagramPacket(data, data.length);
    }
}
