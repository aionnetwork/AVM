package org.aion.avm.core.dappreading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public class DAppLoader {
    private static final Logger logger = LoggerFactory.getLogger(DAppLoader.class);

    private final String dAppModulesPath;
    private final ModuleFinder dAppModulesFinder;
    private final DAppClassLoader avmClassLoader;

    public DAppLoader(String dAppRuntimePath, String dAppModulesPath) {
        this.dAppModulesPath = dAppModulesPath;
        this.dAppModulesFinder = ModuleFinder.of(Paths.get(dAppModulesPath), Paths.get(dAppRuntimePath));
        this.avmClassLoader = new DAppClassLoader(this.dAppModulesFinder);
    }

    public ClassLoadingResult loadDAppIntoNewLayer(String startModuleName, String fullyQualifiedMainClassName) {
        final ModuleLayer bootLayer = ModuleLayer.boot();
        final var emptyFinder = ModuleFinder.of();
        final Configuration dAppLayerConfig = bootLayer.configuration().resolve(dAppModulesFinder, emptyFinder, List.of(startModuleName));
        final Function<String, ClassLoader> moduleToLoaderMapper = (name) -> avmClassLoader;
        final ModuleLayer dAppLayer = bootLayer.defineModules(dAppLayerConfig, moduleToLoaderMapper);
        try {
            Class<?> dAppMainClass = dAppLayer.findModule(startModuleName)
                    .orElseThrow(() -> new Exception("Module not found"))
                    .getClassLoader()
                    .loadClass(fullyQualifiedMainClassName);
            return new ClassLoadingResult().setLoaded(true).setLoadedClass(dAppMainClass);
        } catch (Exception e) {
            final var msg = format("Unable to load dApp. Start module:'%s', main class:'%s', module path:'%s'",
                    startModuleName, fullyQualifiedMainClassName, dAppModulesPath);
            if (logger.isErrorEnabled()) {
                logger.error(msg, e);
            }
            return new ClassLoadingResult().setLoaded(false).setFailDescription(msg);
        }
    }
}
