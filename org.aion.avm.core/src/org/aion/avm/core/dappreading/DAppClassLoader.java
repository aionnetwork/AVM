package org.aion.avm.core.dappreading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.file.*;
import java.util.Optional;

/**
 * @author Roman Katerinenko
 */
class DAppClassLoader extends ClassLoader {
    private final Logger logger = LoggerFactory.getLogger(DAppClassLoader.class);

    private final ModuleFinder moduleFinder;

    DAppClassLoader(ModuleFinder moduleFinder) {
        super("dApp Class loader", ClassLoader.getSystemClassLoader());
        this.moduleFinder = moduleFinder;
    }

    @Override
    protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
        Path filePath = findFileFor(qualifiedClassName);
        if (filePath != null) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                return defineClass(null, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new ClassNotFoundException("Cannot read bytes for " + filePath, e);
            }
        } else {
            throw new ClassNotFoundException(qualifiedClassName + " is not found");
        }
    }

    // todo try contact without modules
    // todo try class with empty package
    // todo it's extremely inefficient
    private Path findFileFor(String qualifiedClassName) {
        final String targetPackageName = getPackageNameOf(qualifiedClassName);
        final String localFilePath = toFilePath(qualifiedClassName);
        for (ModuleReference moduleReference : moduleFinder.findAll()) {
            final Optional<URI> opt = moduleReference.location();
            if (opt.isPresent()) {
                final URI moduleLocation = opt.get();
                try {
                    final FileSystem fs = FileSystems.newFileSystem(Paths.get(moduleLocation.normalize()), null);
                    final Path moduleRoot = fs.getRootDirectories().iterator().next();
                    for (String packageName : moduleReference.descriptor().packages()) {
                        if (packageName.equals(targetPackageName)) {
                            Path filePath = moduleRoot.resolve(localFilePath);
                            if (Files.isReadable(filePath)) {
                                return filePath;
                            } else {
                                if (logger.isErrorEnabled()) {
                                    logger.error(filePath + " exist but not readable");
                                }
                                return null;
                            }
                        }
                    }
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot read module " + moduleLocation);
                    }
                }
            }
        }
        return null;
    }

    private static String getPackageNameOf(String qualifiedClassName) {
        int idx = qualifiedClassName.lastIndexOf('.');
        if (idx == -1) {
            return "";
        } else {
            return qualifiedClassName.substring(0, idx);
        }
    }

    private static String toFilePath(String qualifiedClassName) {
        return qualifiedClassName.replaceAll("\\.", "/") + ".class";
    }
}