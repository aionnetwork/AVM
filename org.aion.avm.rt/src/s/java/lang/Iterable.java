package s.java.lang;

import org.aion.avm.internal.IObject;
import s.java.util.Iterator;

public interface Iterable<T> extends IObject {
    Iterator<T> avm_iterator();

    //Default
}
