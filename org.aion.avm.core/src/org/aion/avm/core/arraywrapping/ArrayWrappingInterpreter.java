package org.aion.avm.core.arraywrapping;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public class ArrayWrappingInterpreter extends BasicInterpreter{

    public ArrayWrappingInterpreter() {
      super(Opcodes.ASM6);
    }

    @Override
    // Override this method to get unmasked type from BasicInterpreter
    public BasicValue newValue(final Type type) {
      if (type == null) {
        return BasicValue.UNINITIALIZED_VALUE;
      }
      switch (type.getSort()) {
        case Type.VOID:
          return null;
        case Type.BOOLEAN:
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
        case Type.FLOAT:
        case Type.LONG:
        case Type.DOUBLE:
        case Type.ARRAY:
        case Type.OBJECT:
          return new BasicValue(type);
        default:
          throw new AssertionError();
      }
    }

}
