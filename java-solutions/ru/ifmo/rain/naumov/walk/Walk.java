package ru.ifmo.rain.naumov.walk;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

// java -cp . -p . -m info.kgeorgiy.java.advanced.walk RecursiveWalk  ru.ifmo.rain.naumov.walk.RecursiveWalk
public class Walk {
    public static void main(String[] args) {
        Path input = null, output = null;
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("invalid arguments");
            return;
        }
        try {
            input = Paths.get(args[0]);
            output = Paths.get(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("invalid paths");
            return;
        }

        try {
            Path parentDirectory = output.getParent();
            if (parentDirectory != null && !Files.isDirectory(parentDirectory)) {
                Files.createDirectory(parentDirectory);
            }
        } catch (IOException e) {
            System.err.println("could not create parent directory for output file");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
                String file = reader.readLine();
                while (file != null) {
                    try {
                        Files.walkFileTree(Paths.get(file), new HashVisitor(writer));
                    } catch (InvalidPathException | IOException e) {
                        writer.write("00000000" + " " + file + System.lineSeparator());
                    }
                    file = reader.readLine();
                }
            } catch (IOException e) {
                System.err.println("invalid input file");
            }
        } catch (IOException e) {
            System.err.println("invalid output file");
        }
    }
}
