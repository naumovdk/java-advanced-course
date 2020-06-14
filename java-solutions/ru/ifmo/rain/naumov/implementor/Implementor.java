package ru.ifmo.rain.naumov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// java -cp . - p . -m info.kgeorgiy.java.advanced.implementor interface ru.ifmo.rain.naumov.implementor.Implementor

/**
 * {@link info.kgeorgiy.java.advanced.implementor.JarImpler} implementation
 */
public class Implementor implements Impler {
    /**
     * Extension of generated classes
     */
    private static final String EXTENSION = ".java";
    /**
     * Name suffix of generated classes
     */
    private static final String CLASS_NAME_SUFFIX = "Impl";
    /**
     * For text generation
     */
    private static final String EMPTY = "";
    /**
     * For text generation
     */
    private static final String SPACE = " ";
    /**
     * For text generation
     */
    private static final String NEWLINE = System.lineSeparator();
    /**
     * For text generation
     */
    private static final String COLON = ";";
    /**
     * For text generation
     */
    private static final String COMMA = ",";
    /**
     * For text generation
     */
    private static final Character DOT = '.';
    /**
     * Delimiter for path parsing
     */
    protected static final Character PATH_SEPARATOR_CHAR = File.separatorChar;
    /**
     * For text generation
     */
    private static final String OPEN = "(";
    /**
     * For text generation
     */
    private static final String CLOSE = ")";
    /**
     * For text generation
     */
    private static final String CURLY_OPEN = "{";
    /**
     * For text generation
     */
    private static final String CURLY_CLOSE = "}";
    /**
     * For text generation
     */
    private static final String UNDERLINE = "_";

    /**
     * For text generation
     */
    private static final String CLASS = "class";
    /**
     * For text generation
     */
    private static final String IMPLEMENTS = "implements";
    /**
     * For text generation
     */
    private static final String THROWS = "throws";
    /**
     * For text generation
     */
    private static final String PUBLIC = "public";
    /**
     * For text generation
     */
    private static final String RETURN = "return";

    /**
     * Joins several elements into one
     * @param delimiter the delimiter between elements
     * @param elements elements to be joined
     * @return joined elements
     */
    private static String join(String delimiter, String... elements) {
        return String.join(delimiter, elements);
    }

    /**
     * Default constructor
     */
//    public Implementor() {}

    /**
     * Returns default value
     * @param type type to be processed
     * @return default value of passed type
     */
    private String getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return "null";
        } else if (type.equals(void.class)) {
            return EMPTY;
        } else if (type.equals(boolean.class)) {
            return "false";
        } else {
            return "0";
        }
    }

    /**
     * Adds "throws" to passed exception
     * @param exception exception to be processed
     * @return "throws" + exception or empty string if empty string was passed
     */
    private static String addThrows(String exception) {
        return (!exception.isEmpty()) ? join(NEWLINE, THROWS, exception) : EMPTY;
    }

    /**
     * Returns exceptions of passed method
     * @param method method to be processed
     * @return exceptions of passed method
     */
    private String getExceptions(Executable method) {
        return addThrows(Arrays.stream(method.getExceptionTypes())
                .map(Class::getCanonicalName).collect(Collectors.joining(COMMA)));
    }

    /**
     * Returns arguments of passed method
     * @param method method to be processed
     * @return arguments of passed method
     */
    private static String getArguments(Executable method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return IntStream.range(0, parameterTypes.length)
                .mapToObj(i -> parameterTypes[i].getCanonicalName() + SPACE + UNDERLINE + i)
                .collect(Collectors.joining(COMMA, OPEN, CLOSE));
    }

    /**
     * Returns access modifiers of passed method
     * @param method to be processed
     * @return access modifiers of passed method
     */
    private String getModifiers(Executable method) {
        return Modifier.toString(method.getModifiers() &
                ~Modifier.NATIVE & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }

    /**
     * Returns generated package header with {@link #SPACE} as words delimiter
     * @param token class to be processed
     * @return generated package header
     */
    private static String getPackage(Class<?> token) {
        return token.getPackageName() == null ? EMPTY : join(SPACE, token.getPackage() + COLON);
    }

    /**
     * Returns generated class definition line with {@link #PUBLIC} access status
     * @param token class to be processed
     * @return generated class definition
     */
    private String getClassDefinition(Class<?> token) {
        return join(SPACE,
                PUBLIC + SPACE + CLASS, getClassName(token),
                join(SPACE, IMPLEMENTS, token.getCanonicalName())
        );
    }

    /**
     * Returns generated class body
     * @param method method to be processed
     * @return generated class body
     */
    private String getMethodBody(Method method) {
        return join(SPACE, RETURN, getDefaultValue(method.getReturnType())) + COLON;
    }

    /**
     * Returns generated method declaration
     * @param method method to be processed
     * @return generated method declaration
     */
    private String getMethodDeclaration(Method method) {
        return join(SPACE, getModifiers(method),
                method.getReturnType().getCanonicalName(),
                method.getName() + getArguments(method),
                getExceptions(method), join(System.lineSeparator(),
                        CURLY_OPEN,
                        getMethodBody(method) + CURLY_CLOSE));
    }

    /**
     * Returns methods with it's bodies of passed class
     * @param token class to be processed
     * @return methods of passed class
     */
    private String getMethods(Class<?> token) {
        Set<HashableMethod> methods =
                Arrays.stream(token.getMethods()).map(HashableMethod::new).collect(Collectors.toCollection(HashSet::new));

        while (token != null) {
            Arrays.stream(token.getDeclaredMethods()).map(HashableMethod::new).forEach(methods::add);
            token = token.getSuperclass();
        }
        return methods.stream().filter(a -> Modifier.isAbstract(a.get().getModifiers()))
                .map(a -> getMethodDeclaration(a.get())).collect(Collectors.joining(NEWLINE));
    }

    /**
     * Return joined package, class definition, methods with their bodies
     * @param token class to be processed
     * @return fully generated class text
     */
    private String getFullClass(Class<?> token) {
        return join(System.lineSeparator(),
                getPackage(token),
                join(SPACE, getClassDefinition(token), CURLY_OPEN),
                getMethods(token),
                CURLY_CLOSE
        );
    }

    /**
     * Return name of passed class
     * @param token class to be processed
     * @return name of passed class
     */
    String getClassName(Class<?> token) {
        return token.getSimpleName() + CLASS_NAME_SUFFIX;
    }

    /**
     * Creates all directories of passed path
     * @param path path to be created
     * @throws IOException if couldn't create directory at some point
     */
    static void createPath(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Re-encode 16-bit symbols to unicode
     * @param text text to be encoded
     * @return re-encoded text
     */
    private static String encode(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            stringBuilder.append(c < 128 ? String.valueOf(c) : String.format("\\u%04x", (int) c));
        }
        return stringBuilder.toString();
    }

    /**
     * Returns {@link String} version of passed class package resolved as path
     * @param token class to be processed
     * @param newDelimiter char to replace dots at package representation
     * @return {@link String} version of passed class package resolved as path
     */
    String getPackageNameAsPath(Class<?> token, Character newDelimiter) {
        return token.getPackageName().replace(DOT, newDelimiter);
    }

    /**
     * Creates implementation of passed class by passed path
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException thrown if couldn't create path to {@code root} or invalid arguments were passed
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("null arguments");
        }
        if ((!token.isInterface()) || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("invalid class");
        }

        try {
            Path realPath = root.resolve(Path.of(getPackageNameAsPath(token, PATH_SEPARATOR_CHAR),
                    getClassName(token) + EXTENSION));
            createPath(realPath);
            try (BufferedWriter writer = Files.newBufferedWriter(realPath)) {
                writer.write(encode(getFullClass(token)));
            }
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Calls {@link #implement(Class, Path)} on args passed
     * @param args will be translated to {@link #implement(Class, Path)}
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("two arguments expected");
            return;
        }
        try {
            new Implementor().implement(Class.forName(args[0]), Path.of(args[1]));
        } catch (ClassNotFoundException e) {
            System.err.println("class not found");
        } catch (ImplerException e) {
            System.err.println("couldn't implement");
        } catch (InvalidPathException e) {
            System.err.println("invalid path");
        }
    }

    /**
     * Boxer class for {@link Method} to make it storable in {@link HashMap}
     */
    private static class HashableMethod {
        /**
         * {@link Method} to be hashed
         */
        private final Method method;
        HashableMethod(Method method) {
            this.method = method;
        }

        /**
         * Getter for {@link #method}
         * @return {@link #method}
         */
        Method get() {
            return method;
        }


        /**
         * {@inheritDoc}
         *
         * Computes hashcode for {@link #method}
         * @return hashcode for {@link #method}
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(method.getParameterTypes()) +
                    method.getReturnType().hashCode() + method.getName().hashCode();
        }

        /**
         * {@inheritDoc}
         *
         * Basic {@link Object} method
         * @param o other object to be compared
         * @return {@code true} if equal, {@code false} otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof HashableMethod) {
                HashableMethod hm = (HashableMethod) o;
                return method.getName().equals(hm.method.getName()) &&
                        Arrays.equals(method.getParameterTypes(), hm.method.getParameterTypes()) &&
                        method.getReturnType().equals(hm.method.getReturnType());
            }
            return false;
        }
    }
}