package org.aion.avm.core.dappreading;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Katerinenko
 */
// todo module-info.java and package-info.java must be excluded from the result
// todo fix platform-specific delimiters
// todo check symlinks
public final class DAppReaderWriter {
    private static final String CLASS_SUFFIX = ".class";

    private Path absolutePathToDirOrJar;
    private String prefix = "";
    private Path root;

    /**
     * For example, if you want to read all classes (e.g, 'com.example.C1.class') located at '/home/dude/build'
     * then pathToDir='/home/dude/build/'
     * packagePrefix=''
     * <p>
     * If you want to load all classes from some subpackage (e.g., com/) then
     * pathToDir='/home/dude/build/com'
     * packagePrefix='com'
     * because you don't start from the root of the package but from 'com'
     */
    public Map<String, byte[]> readClassesFromDir(String pathToDir, String packagePrefix) throws IOException {
        Objects.requireNonNull(pathToDir);
        Objects.requireNonNull(packagePrefix);
        prefix = packagePrefix;
        absolutePathToDirOrJar = root = Paths.get(pathToDir).toAbsolutePath().normalize();
        return walkDirTreeAndFillResult();
    }

    public Map<String, byte[]> readClassesFromJar(String pathToJar) throws IOException {
        Objects.requireNonNull(pathToJar);
        final var normalizedPath = Paths.get(pathToJar).toAbsolutePath().normalize();
        final var fileSystem = FileSystems.newFileSystem(normalizedPath, null);
        root = fileSystem.getRootDirectories().iterator().next();
        absolutePathToDirOrJar = normalizedPath;
        return walkJarTreeAndFillResult();
    }

    private Map<String, byte[]> walkJarTreeAndFillResult() throws IOException {
        final var result = new HashMap<String, byte[]>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path relativeFilePath, BasicFileAttributes attrs) throws IOException {
                if (isClassFile(relativeFilePath)) {
                    final var packagePath = relativeFilePath.getParent().toString().substring(1);
                    final var fileName = relativeFilePath.getFileName().toString();
                    result.put(getQualifiedClassNameFrom(packagePath, fileName), Files.readAllBytes(relativeFilePath));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private Map<String, byte[]> walkDirTreeAndFillResult() throws IOException {
        final var result = new HashMap<String, byte[]>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path absolutePath, BasicFileAttributes attrs) throws IOException {
                if (isClassFile(absolutePath)) {
                    final var relativePath = absolutePathToDirOrJar.relativize(absolutePath);
                    final var parentPath = relativePath.getParent();
                    final var packagePath = parentPath == null ? "" : "." + parentPath.toString();
                    final var fileName = relativePath.getFileName().toString();
                    result.put(getQualifiedClassNameFrom(prefix + packagePath, fileName), Files.readAllBytes(absolutePath));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private static boolean isClassFile(Path filePath) {
        return filePath.getFileName().toString().endsWith(CLASS_SUFFIX);
    }

    private String getQualifiedClassNameFrom(String packagePath, String fileName) {
        return packagePath
                .replaceAll("/", ".")
                + "."
                + fileName
                .replaceAll(CLASS_SUFFIX, "");
    }
}