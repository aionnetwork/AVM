package org.aion.avm.core;

import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.types.Forest;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IRuntimeSetup;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


public class SimpleAvm {
    private final AvmClassLoader loader;
    private final IRuntimeSetup runtimeSetup;
    private final IInstrumentation instrumentation;
    private final Set<String> transformedClassNames;

    public SimpleAvm(long energyLimit, boolean preserveDebuggability, Class<?>... classes) {
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
                    builder.addClass(e.getKey(), Object.class.getName(), true, e.getValue());
                }
            } catch (ClassNotFoundException ex) {
                throw RuntimeAssertionError.unexpected(ex);
            }
        });
       Forest<String, ClassInfo> classHierarchy = builder.asMutableForest();

        // transform classes
        Map<String, byte[]> transformedClasses = DAppCreator.transformClasses(preTransformedClassBytecode, classHierarchy, preserveDebuggability);
        Map<String, byte[]> finalContractClasses = Helpers.mapIncludingHelperBytecode(transformedClasses, Helpers.loadDefaultHelperBytecode());
        this.loader = NodeEnvironment.singleton.createInvocationClassLoader(finalContractClasses);
        this.transformedClassNames = Collections.unmodifiableSet(transformedClasses.keySet());

        // Create the instrumentation, attach this thread, and push a faked-up frame.
        this.runtimeSetup = Helpers.getSetupForLoader(this.loader);
        this.instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(this.instrumentation);
        InstrumentationHelpers.pushNewStackFrame(this.runtimeSetup, this.loader, energyLimit, 1);
    }

    public void attachBlockchainRuntime(IBlockchainRuntime rt) {
        Helpers.attachBlockchainRuntime(loader, rt);
    }

    public AvmClassLoader getClassLoader() {
        return loader;
    }

    public IRuntimeSetup getRuntimeSetup() {
        return this.runtimeSetup;
    }

    public IInstrumentation getInstrumentation() {
        return this.instrumentation;
    }

    public Set<String> getTransformedClassNames() {
        return this.transformedClassNames;
    }

    public void shutdown() {
        InstrumentationHelpers.popExistingStackFrame(this.runtimeSetup);
        InstrumentationHelpers.detachThread(this.instrumentation);
    }
}
