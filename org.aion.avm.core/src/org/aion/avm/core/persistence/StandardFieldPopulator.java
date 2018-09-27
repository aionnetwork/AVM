package org.aion.avm.core.persistence;

import java.lang.reflect.Field;

import org.aion.avm.internal.RuntimeAssertionError;


/**
 * An implementation of IFieldPopulator which should be used in the common case.
 * This is the variant which directly applies the field values, so it should be used in all cases where the graph is being fully deserialized.
 */
public class StandardFieldPopulator implements ReflectionStructureCodec.IFieldPopulator {
    @Override
    public org.aion.avm.shadow.java.lang.Object instantiateReference(INode node) {
        // This implementation just returns the instance.
        return (null != node)
                ? node.getObjectInstance()
                : null;
    }

    @Override
    public void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val) {
        try {
            field.setBoolean(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val) {
        try {
            field.setDouble(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val) {
        try {
            field.setLong(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val) {
        try {
            field.setFloat(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val) {
        try {
            field.setInt(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val) {
        try {
            field.setChar(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val) {
        try {
            field.setShort(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val) {
        try {
            field.setByte(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val) {
        try {
            field.set(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
}
