package org.aion.avm.core;

import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.types.Forest;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


public class SimpleAvm {
    private final AvmClassLoader loader;
    private final IHelper helper;
    private final Set<String> transformedClassNames;

    public SimpleAvm(long energyLimit, Class<?>... classes) {
        Map<String, byte[]> preTransformedClassBytecode = new HashMap<>();

        Stream.of(classes).forEach(clazz -> preTransformedClassBytecode.put(clazz.getName(),
                Helpers.loadRequiredResourceAsBytes(clazz.getName().replaceAll("\\.", "/") + ".class")));

        // build class hierarchy
        HierarchyTreeBuilder builder = new HierarchyTreeBuilder();
        preTransformedClassBytecode.entrySet().stream().forEach(e -> {
            try {
                // NOTE: we load the class to figure out the super class instead of by static analysis.
                Class<?> clazz = SimpleAvm.class.getClassLoader().loadClass(e.getKey());
                if (!clazz.isInterface()) {
                    builder.addClass(e.getKey(), clazz.getSuperclass().getName(), false, e.getValue());
                }else{
                    builder.addClass(e.getKey(), Object.class.getName(), false, e.getValue());
                }
            } catch (ClassNotFoundException ex) {
                throw RuntimeAssertionError.unexpected(ex);
            }
        });
       Forest<String, ClassInfo> classHierarchy = builder.asMutableForest();

        // transform classes
        Map<String, byte[]> transformedClasses = DAppCreator.transformClasses(preTransformedClassBytecode, classHierarchy);
        Map<String, byte[]> finalContractClasses = Helpers.mapIncludingHelperBytecode(transformedClasses);
        this.loader = NodeEnvironment.singleton.createInvocationClassLoader(finalContractClasses);
        this.transformedClassNames = Collections.unmodifiableSet(transformedClasses.keySet());

        // set up helper
        helper = Helpers.instantiateHelper(loader, energyLimit, 1);
    }

    public void attachBlockchainRuntime(IBlockchainRuntime rt) {
        Helpers.attachBlockchainRuntime(loader, rt);
    }

    public AvmClassLoader getClassLoader() {
        return loader;
    }

    public IHelper getHelper() {
        return helper;
    }

    public Set<String> getTransformedClassNames() {
        return this.transformedClassNames;
    }

    public void shutdown() {
        RuntimeAssertionError.assertTrue(this.helper == IHelper.currentContractHelper.get());
        IHelper.currentContractHelper.remove();
    }
}
