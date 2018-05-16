package org.aion.avm.core.instrument;


/**
 * This class is used only as a testing resource for BlockBuildingMethodVisitorTest.
 * TODO:  We should check in the compiled class file so we aren't the victim of compiler differences.
 */
public class BlockTestResource {
    public int returnInt() {
        return 5;
    }

    public int throwException() {
        throw new IllegalStateException();
    }

    public int checkBranch(int test) {
        int result = 5;
        if (test > 6) {
            result = 6;
        } else {
            result = 4;
        }
        return result;
    }

    public int checkTableSwitch(int test) {
        int result = 5;
        switch (test) {
        case 0:
            result = 1;
            break;
        case 1:
            result = 2;
            break;
        case 2:
            result = 3;
            break;
        default:
            result = 0;
        }
        return result;
    }

    public int checkLookupSwitch(int test) {
        int result = 5;
        switch (test) {
        case 5:
            result = 1;
            break;
        case 10:
            result = 2;
            break;
        case 22:
            result = 3;
            break;
        default:
            result = 0;
        }
        return result;
    }
}
