package org.aion.avm.core.dappreading;

/**
 * @author Roman Katerinenko
 */
public class ClassLoadingResult {
    private boolean loaded;
    private Class<?> loadedClass;
    private String failDescription;

    public Class<?> getLoadedClass() {
        return loadedClass;
    }

    public ClassLoadingResult setLoadedClass(Class<?> loadedClass) {
        this.loadedClass = loadedClass;
        return this;
    }

    public String getFailDescription() {
        return failDescription;
    }

    public ClassLoadingResult setFailDescription(String failDescription) {
        this.failDescription = failDescription;
        return this;
    }

    public ClassLoadingResult setLoaded(boolean loaded) {
        this.loaded = loaded;
        return this;
    }

    public boolean isLoaded(){
        return loaded;
    }
}
