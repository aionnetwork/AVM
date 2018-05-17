package org.aion.avm.core.dappreading;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Katerinenko
 */
public final class DAppReaderWriter {
    private static final String CLASS_SUFFIX = ".class";

    // todo module-info.java and package-info.java must be excluded from the result
    // todo fix platform-specific delimiters
    public static Map<String, byte[]> readClassesFrom(String pathToModule) throws IOException {
        final var normalizedPath = Paths.get(pathToModule).normalize();
        final var fileSystem = FileSystems.newFileSystem(normalizedPath, null);
        final var result = new HashMap<String, byte[]>();
        // todo case with multiple root dirs
        Path rootDir = fileSystem.getRootDirectories().iterator().next();
        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isClassFile(file)) {
                    result.put(getQualifiedClassNameFrom(file), Files.readAllBytes(file));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private static boolean isClassFile(Path filePath) {
        return filePath.getFileName().toString().endsWith(CLASS_SUFFIX);
    }

    private static String getQualifiedClassNameFrom(Path classFile) {
        return classFile
                .getParent()
                .toString()
                .substring(1)
                .replaceAll("/", ".")
                + "."
                + classFile
                .getFileName()
                .toString()
                .replaceAll(CLASS_SUFFIX, "");
    }
}