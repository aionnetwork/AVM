package org.aion.avm.core.dappreading;

import org.aion.avm.core.FileUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.aion.avm.core.FileUtils.putToTempDir;

/**
 * @author Roman Katerinenko
 */
// todo fix platform-specific delimiters
// todo check symlinks
public final class DAppReaderWriter {
    private static final String CLASS_SUFFIX = ".class";

    private Path root;

    // todo close files system?
    // todo unify both read methods
    public Map<String, byte[]> readClassesFromJar(String pathToJar) throws IOException {
        Objects.requireNonNull(pathToJar);
        final var normalizedPath = Paths.get(pathToJar).toAbsolutePath().normalize();
        root = FileUtils.getFSRootDirFor(normalizedPath);
        return walkJarTreeAndFillResult();
    }

    public Map<String, byte[]> readClassesFromJar(byte[] jar) throws IOException {
        Objects.requireNonNull(jar);
        // todo fix making temp directory
        final var normalizedPath = putToTempDir(jar, "aiontemp", "aion-temp-dapp.jar");
        root = FileUtils.getFSRootDirFor(normalizedPath);
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

    private static boolean isClassFile(Path filePath) {
        final var fileName = filePath.getFileName().toString();
        return fileName.endsWith(CLASS_SUFFIX)
                && !fileName.equals("package-info.class")
                && !fileName.equals("module-info.class");
    }

    private String getQualifiedClassNameFrom(String packagePath, String fileName) {
        return packagePath
                .replaceAll("/", ".")
                + "."
                + fileName
                .replaceAll(CLASS_SUFFIX, "");
    }
}