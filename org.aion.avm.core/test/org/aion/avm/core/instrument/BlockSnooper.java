package org.aion.avm.core.instrument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.instrument.BasicBlock;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;


/**
 * Used by a few of the instrumentation tests to 
 */
public class BlockSnooper implements Function<byte[], byte[]> {
    public Map<String, List<BasicBlock>> resultMap;
    
    @Override
    public byte[] apply(byte[] inputBytes) {
        ClassReader in = new ClassReader(inputBytes);
        Map<String, List<BasicBlock>> result = new HashMap<>();
        
        ClassVisitor reader = new ClassVisitor(Opcodes.ASM6) {
            public MethodVisitor visitMethod(
                    final int access,
                    final String name,
                    final String descriptor,
                    final String signature,
                    final String[] exceptions) {
                // We need a MethodNode to grab the result when the method visitation is finished.
                return new MethodNode(Opcodes.ASM6, access, name, descriptor, signature, exceptions) {
                    @Override
                    public void visitEnd() {
                        // Let the superclass do what it wants to finish this.
                        super.visitEnd();
                        
                        // Create the read-only visitor and use it to extract the block data.
                        BlockBuildingMethodVisitor readingVisitor = new BlockBuildingMethodVisitor();
                        this.accept(readingVisitor);
                        List<BasicBlock> blocks = readingVisitor.getBlockList();
                        result.put(name + descriptor, blocks);
                    }
                };
            }
        };
        in.accept(reader, ClassReader.SKIP_DEBUG);
        
        this.resultMap = result;
        return inputBytes;
    }
}
