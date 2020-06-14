package ru.ifmo.rain.naumov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

// java -cp . -p . -m info.kgeorgiy.java.advanced.implementorjar-interface ru.ifmo.rain.naumov.implementor.JarImplementor
public class JarImplementor extends Implementor implements JarImpler {
    private static final Character ZIP_ENTRY_PATH_DELIMITER = '/';

    private void compile(Class<?> token, Path root) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String path;
        try {
            path = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new ImplerException("failed path conversion");
        }
        String[] args = new String[]{
                "-cp",
                path,
                root.resolve(getPackageNameAsPath(token, ZIP_ENTRY_PATH_DELIMITER)).resolve(getClassName(token) + ".java").toString()
        };
        if (compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("compiler run failed");
        }
    }

    private void buildJar(Path jarFile, Path root, Class<?> token) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            String path = getPackageNameAsPath(token, ZIP_ENTRY_PATH_DELIMITER) + ZIP_ENTRY_PATH_DELIMITER + getClassName(token) + ".class";
            System.out.println(path + '\n' + '\n');
            stream.putNextEntry(new ZipEntry(path));
            Files.copy(root.resolve(path), stream);
        } catch (IOException e) {
            throw new ImplerException("failed to instance output stream");
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            createPath(jarFile.normalize());
        } catch (IOException e) {
            throw new ImplerException("couldn't create path to jar file");
        }
        Path root = jarFile.toAbsolutePath().getParent();
        implement(token, root);
        compile(token, root);
        buildJar(jarFile, root, token);
    }

    public static void main(String[] args) {
        if (args == null || args.length > 3 || args.length < 2) {
            System.err.println("invalid arguments");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println(Arrays.toString(args) + "is null");
            }
        }
        try {
            if (args.length == 2) {
                new JarImplementor().implement(Class.forName(args[0]), Path.of(args[1]));
            } else if (args[0].endsWith("-jar")) {
                new JarImplementor().implementJar(Class.forName(args[0]), Path.of(args[1]));
            } else {
                System.err.println("invalid arguments");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("class not found");
        } catch (ImplerException e) {
            System.err.println("couldn't implement passed class");
        }
    }
}
