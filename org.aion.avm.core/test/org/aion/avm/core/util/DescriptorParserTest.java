package org.aion.avm.core.util;

import org.junit.Assert;
import org.junit.Test;


/**
 * Very basic tests of DescriptorParser, effectively acting as a demonstration of how to use it.
 */
public class DescriptorParserTest {
    @Test
    public void testPrimitiveType() throws Exception {
        String input = "I";
        CallbackCatcher catcher = new CallbackCatcher();
        int result = DescriptorParser.parse(input, catcher, 0);
        Assert.assertEquals(1, result);
        Assert.assertEquals("0Integer ", catcher.external.toString());
        Assert.assertEquals("", catcher.args.toString());
    }
    @Test
    public void testPrimitiveTypeArray() throws Exception {
        String input = "[[B";
        CallbackCatcher catcher = new CallbackCatcher();
        int result = DescriptorParser.parse(input, catcher, 0);
        Assert.assertEquals(1, result);
        Assert.assertEquals("2Byte ", catcher.external.toString());
        Assert.assertEquals("", catcher.args.toString());
    }
    @Test
    public void testObject() throws Exception {
        String input = "Ljava/lang/String;";
        CallbackCatcher catcher = new CallbackCatcher();
        int result = DescriptorParser.parse(input, catcher, 0);
        Assert.assertEquals(1, result);
        Assert.assertEquals("0java/lang/String ", catcher.external.toString());
        Assert.assertEquals("", catcher.args.toString());
    }
    @Test
    public void testObjectArray() throws Exception {
        String input = "[[Ljava/lang/String;";
        CallbackCatcher catcher = new CallbackCatcher();
        int result = DescriptorParser.parse(input, catcher, 0);
        Assert.assertEquals(1, result);
        Assert.assertEquals("2java/lang/String ", catcher.external.toString());
        Assert.assertEquals("", catcher.args.toString());
    }
    @Test
    public void testMethodPrimitives() throws Exception {
        String input = "(I[Z)S";
        CallbackCatcher catcher = new CallbackCatcher();
        int result = DescriptorParser.parse(input, catcher, 0);
        Assert.assertEquals(5, result);
        Assert.assertEquals("0Short ", catcher.external.toString());
        Assert.assertEquals("0Integer 1Boolean ", catcher.args.toString());
    }
    @Test
    public void testMethodMixed() throws Exception {
        String input = "(FLjava/lang/String;)[Ljava/lang/Object;";
        CallbackCatcher catcher = new CallbackCatcher();
        int result = DescriptorParser.parse(input, catcher, 0);
        Assert.assertEquals(5, result);
        Assert.assertEquals("1java/lang/Object ", catcher.external.toString());
        Assert.assertEquals("0Float 0java/lang/String ", catcher.args.toString());
    }
    @Test
    public void testMethodMixedVoidReturn() throws Exception {
        String input = "(DLjava/lang/String;C)V";
        CallbackCatcher catcher = new CallbackCatcher();
        int result = DescriptorParser.parse(input, catcher, 0);
        Assert.assertEquals(6, result);
        Assert.assertEquals("Void ", catcher.external.toString());
        Assert.assertEquals("0Double 0java/lang/String 0Char ", catcher.args.toString());
    }


    private static class CallbackCatcher implements DescriptorParser.Callbacks<Integer> {
        public final StringBuilder external = new StringBuilder();
        public final StringBuilder args = new StringBuilder();
        private boolean inArgs;

        @Override
        public Integer argumentStart(Integer userData) {
            this.inArgs = true;
            return userData + 1;
        }
        @Override
        public Integer argumentEnd(Integer userData) {
            this.inArgs = false;
            return userData + 1;
        }
        @Override
        public Integer readObject(int arrayDimensions, String type, Integer userData) {
            append(arrayDimensions + type);
            return userData + 1;
        }
        @Override
        public Integer readVoid(Integer userData) {
            append("Void");
            return userData + 1;
        }
        @Override
        public Integer readBoolean(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Boolean");
            return userData + 1;
        }
        @Override
        public Integer readShort(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Short");
            return userData + 1;
        }
        @Override
        public Integer readLong(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Long");
            return userData + 1;
        }
        @Override
        public Integer readInteger(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Integer");
            return userData + 1;
        }
        @Override
        public Integer readFloat(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Float");
            return userData + 1;
        }
        @Override
        public Integer readDouble(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Double");
            return userData + 1;
        }
        @Override
        public Integer readChar(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Char");
            return userData + 1;
        }
        @Override
        public Integer readByte(int arrayDimensions, Integer userData) {
            append(arrayDimensions + "Byte");
            return userData + 1;
        }
        private void append(String string) {
            if (inArgs) {
                this.args.append(string + " ");
            } else {
                this.external.append(string + " ");
            }
        }
    }
}
