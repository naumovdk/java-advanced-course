package ru.ifmo.rain.naumov.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

class HashVisitor extends SimpleFileVisitor<Path> {
    private final Writer writer;
    private final int BUFFER_SIZE = 1024;

    HashVisitor(Writer writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            int hash = 0x811c9dc5;
            byte[] buffer = inputStream.readNBytes(BUFFER_SIZE);
            while (buffer.length != 0) {
                for (final byte b : buffer) {
                    hash = (hash * 0x01000193) ^ (b & 0xff);
                }
                buffer = inputStream.readNBytes(BUFFER_SIZE);
            }
            writer.write(String.format("%1$08x", hash) + " " + path.toString() + System.lineSeparator());
        } catch (Exception e) {
            System.err.println("could not access file");
        }
        return super.visitFile(path, attrs);
    }
}
