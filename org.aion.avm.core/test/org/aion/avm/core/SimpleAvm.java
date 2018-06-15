package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.rt.BlockchainRuntime;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SimpleAvm {

    private BlockchainRuntime rt;
    private Map<String, byte[]> classes;

    private AvmClassLoader loader;

    public SimpleAvm(BlockchainRuntime rt, Class<?>... classes) {
        this.rt = rt;
        this.classes = new HashMap<>();

        Stream.of(classes).forEach(clazz -> this.classes.put(clazz.getName(),
                Helpers.loadRequiredResourceAsBytes(clazz.getName().replaceAll("\\.", "/") + ".class")));

        setup();
    }

    private void setup() {
        // build shared class loader
        AvmSharedClassLoader sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());

        // build class hierarchy
        HierarchyTreeBuilder builder = new HierarchyTreeBuilder();
        classes.entrySet().stream().forEach(e -> {
            try {
                // NOTE: we load the class to figure out the super class instead of by static analysis.
                Class<?> clazz = SimpleAvm.class.getClassLoader().loadClass(e.getKey());

                builder.addClass(e.getKey(), clazz.getSuperclass().getName(), e.getValue());
            } catch (ClassNotFoundException ex) {
                Assert.unexpected(ex);
            }
        });
        Forest<String, byte[]> classHierarchy = builder.asMutableForest();

        // create a new AVM
        AvmImpl avm = new AvmImpl(sharedClassLoader);

        // compute object sizes
        Map<String, Integer> runtimeObjectSizes = AvmImpl.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = avm.computeObjectSizes(classHierarchy, runtimeObjectSizes);

        // transform classes
        Map<String, byte[]> transformedClasses = avm.transformClasses(classes, classHierarchy, allObjectSizes);
        this.loader = new AvmClassLoader(sharedClassLoader, transformedClasses);

        // set up helper
        Helpers.instantiateHelper(loader, rt);
    }


    public BlockchainRuntime getBlockchainRuntime() {
        return rt;
    }

    public Map<String, byte[]> getClasses() {
        return classes;
    }

    public AvmClassLoader getClassLoader() {
        return loader;
    }
}
