package org.aion.avm.core;

import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IInstrumentationFactory;


/**
 * Since issue-303 required the creation of IInstrumentationFactory, this helper class exists to cover the common case of our tests:  just using CommonInstrumentation.
 */
public class CommonAvmFactory {
    public static AvmImpl buildAvmInstance() {
        // We use the common instrumentation for this case.
        IInstrumentationFactory factory = new CommonInstrumentationFactory();
        AvmConfiguration configuration = new AvmConfiguration();
        return NodeEnvironment.singleton.buildAvmInstance(factory, configuration);
    }

    public static AvmImpl buildAvmInstanceInDebugMode() {
        IInstrumentationFactory factory = new CommonInstrumentationFactory();
        AvmConfiguration configuration = new AvmConfiguration();
        configuration.enableVerboseContractErrors = true;
        configuration.preserveDebuggability = true;
        return NodeEnvironment.singleton.buildAvmInstance(factory, configuration);
    }

    public static AvmImpl buildAvmInstanceForConfiguration(AvmConfiguration configuration) {
        IInstrumentationFactory factory = new CommonInstrumentationFactory();
        return NodeEnvironment.singleton.buildAvmInstance(factory, configuration);
    }


    private static class CommonInstrumentationFactory implements IInstrumentationFactory {
        @Override
        public IInstrumentation createInstrumentation() {
            return new CommonInstrumentation();
        }
        @Override
        public void destroyInstrumentation(IInstrumentation instance) {
            // Implementation requires no cleanup.
        }
    }
}
