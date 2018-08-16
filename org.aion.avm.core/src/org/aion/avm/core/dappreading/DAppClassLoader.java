package org.aion.avm.core.dappreading;

import org.aion.avm.internal.RuntimeAssertionError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Roman Katerinenko
 */
public class DAppClassLoader extends ClassLoader {
    private final Logger logger = LoggerFactory.getLogger(DAppClassLoader.class);

    private final ModuleFinder moduleFinder;
    private final Map<String, byte[]> injectedClasses;

    DAppClassLoader(ModuleFinder moduleFinder) {
        super("dApp Class loader", ClassLoader.getSystemClassLoader());
        this.moduleFinder = moduleFinder;
        this.injectedClasses = new HashMap<>();
    }

    public Class<?> injectAndLoadClass(String className, byte[] bytecode) {
        byte[] previous = this.injectedClasses.put(className, bytecode);
        // If we over-wrote something, this is a serious bug (unless we define how redundant injection is handled, here).
        RuntimeAssertionError.assertTrue(null == previous);
        
        Class<?> clazz = null;
        try {
            clazz = this.loadClass(className);
        } catch (ClassNotFoundException e) {
            // We just defined this, so we better have found it.
            throw RuntimeAssertionError.unexpected(e);
        }
        RuntimeAssertionError.assertTrue(null != clazz);
        return clazz;
    }

    @Override
    protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
        Class<?> clazz = null;
        byte[] injected = this.injectedClasses.get(qualifiedClassName);
        if (null != injected) {
            clazz = defineClass(null, injected, 0, injected.length);
        } else {
            clazz = findClassOnFilesystem(qualifiedClassName); 
        }
        return clazz;
    }

    private Class<?> findClassOnFilesystem(String qualifiedClassName) throws ClassNotFoundException {
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