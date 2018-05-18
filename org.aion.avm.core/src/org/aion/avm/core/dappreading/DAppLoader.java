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
    private final Logger logger = LoggerFactory.getLogger(DAppLoader.class);

    private Class dAppMainClass;

    ClassLoadingResult loadDAppIntoNewLayer(String dAppRuntimePath, String dAppModulesPath, String startModuleName, String fullyQualifiedMainClassName) {
        final ModuleLayer bootLayer = ModuleLayer.boot();
        final ModuleFinder dAppModulesFinder = ModuleFinder.of(Paths.get(dAppModulesPath), Paths.get(dAppRuntimePath));
        final var emptyFinder = ModuleFinder.of();
        final Configuration dAppLayerConfig = bootLayer.configuration().resolve(dAppModulesFinder, emptyFinder, List.of(startModuleName));
        final var avmClassLoader = new DAppClassLoader();
        avmClassLoader.setModuleFinder(dAppModulesFinder);
        final Function<String, ClassLoader> moduleToLoaderMapper = (name) -> avmClassLoader;
        final ModuleLayer dAppLayer = bootLayer.defineModules(dAppLayerConfig, moduleToLoaderMapper);
        try {
            dAppMainClass = dAppLayer.findModule(startModuleName)
                    .orElseThrow(() -> new Exception("Module not found"))
                    .getClassLoader()
                    .loadClass(fullyQualifiedMainClassName);
        } catch (Exception e) {
            final var msg = format("Unable to load dApp. Start module:'%s', main class:'%s', module path:'%s'",
                    startModuleName, fullyQualifiedMainClassName, dAppModulesPath);
            if (logger.isErrorEnabled()) {
                logger.error(msg, e);
            }
            return new ClassLoadingResult().setLoaded(false).setFailDescription(msg);
        }
        return new ClassLoadingResult().setLoaded(true);
    }

    Class getDAppMainClass() {
        return dAppMainClass;
    }
}