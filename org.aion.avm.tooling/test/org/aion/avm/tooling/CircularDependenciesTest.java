 package org.aion.avm.tooling;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingKernel;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class CircularDependenciesTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address DEPLOYER;
    private static avm.Address DEPLOYER_API;
    private static final long ENERGY_LIMIT = 100_000_000_000L;
    private static final long ENERGY_PRICE = 1;

    private static TestingKernel kernel;
    Address contract;

    @BeforeClass
    public static void setup() {
        DEPLOYER_API = avmRule.getPreminedAccount();
        DEPLOYER = new Address(DEPLOYER_API.unwrap());
        kernel = avmRule.kernel;
    }

    private TransactionResult callContract(String method, Object... parameters) {
        return callContract(DEPLOYER, method, parameters);
    }

    private TransactionResult callContract(Address sender, String method, Object... parameters) {
        byte[] callData = ABIUtil.encodeMethodArguments(method, parameters);
        avm.Address contractAddress = new avm.Address(contract.toBytes());
        avm.Address senderAddress = new avm.Address(sender.toBytes());
        AvmRule.ResultWrapper result = avmRule.call(senderAddress, contractAddress, BigInteger.ZERO, callData, ENERGY_LIMIT, ENERGY_PRICE);
        assertTrue(result.getReceiptStatus().isSuccess());
        return result.getTransactionResult();

    }

    /**
     * Note: this does not test a circularity in the type relationships themselves, rather circular
     * object referencing, which is OK.
     */
    @Test
    public void testCircularDependency() {
        byte[] jarBytes = avmRule.getDappBytes(CircularDependencyATarget.class, null, CircularDependencyBTarget.class);
        AvmRule.ResultWrapper result = avmRule.deploy(DEPLOYER_API, BigInteger.ZERO, jarBytes, ENERGY_LIMIT, ENERGY_PRICE);
        assertTrue(result.getReceiptStatus().isSuccess());
        contract = new Address(result.getDappAddress().unwrap());

        callContract("getValue");
    }

    /**
     * Tests a circularity in the type relationships themselves: A is child of & parent of B.
     */
    @Test
    public void testCircularTypesInterfacesDependency() throws IOException {
        byte[] interfaceA = Files.readAllBytes(Paths.get("test/org/aion/avm/tooling/CircularInterfaceTypesATarget.class"));
        byte[] interfaceB = Files.readAllBytes(Paths.get("test/org/aion/avm/tooling/CircularInterfaceTypesBTarget.class"));
        Map<String, byte[]> classMap = new HashMap<>();
        classMap.put("CircularClassTypesATarget", interfaceA);
        classMap.put("CircularInterfaceTypesBTarget", interfaceB);
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(
            SelfDestructSmallResource.class, classMap);
        CodeAndArguments codeAndArguments = new CodeAndArguments(jar, null);
        AvmRule.ResultWrapper result = avmRule.deploy(DEPLOYER_API, BigInteger.ZERO, codeAndArguments.encodeToBytes(), ENERGY_LIMIT, ENERGY_PRICE);
        assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, result.getReceiptStatus());
    }

    /**
     * Tests a circularity in the type relationships themselves: A is child of & parent of B.
     */
    @Test
    public void testCircularTypesClassesDependency() throws IOException {
        byte[] classA = Files.readAllBytes(Paths.get("test/org/aion/avm/tooling/CircularClassTypesATarget.class"));
        byte[] classB = Files.readAllBytes(Paths.get("test/org/aion/avm/tooling/CircularClassTypesBTarget.class"));
        Map<String, byte[]> classMap = new HashMap<>();
        classMap.put("CircularClassTypesATarget", classA);
        classMap.put("CircularInterfaceTypesBTarget", classB);
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(
            SelfDestructSmallResource.class, classMap);
        CodeAndArguments codeAndArguments = new CodeAndArguments(jar, null);
        AvmRule.ResultWrapper result = avmRule.deploy(DEPLOYER_API, BigInteger.ZERO, codeAndArguments.encodeToBytes(), ENERGY_LIMIT, ENERGY_PRICE);
        assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, result.getReceiptStatus());
    }
}
