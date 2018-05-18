package org.aion.avm.core.dappreading;

/**
 * @author Roman Katerinenko
 */
class ClassLoadingResult {
    private boolean loaded;
    private Class<?> loadedClass;
    private String failDescription;

    Class<?> getLoadedClass() {
        return loadedClass;
    }

    ClassLoadingResult setLoadedClass(Class<?> loadedClass) {
        this.loadedClass = loadedClass;
        return this;
    }

    String getFailDescription() {
        return failDescription;
    }

    ClassLoadingResult setFailDescription(String failDescription) {
        this.failDescription = failDescription;
        return this;
    }

    ClassLoadingResult setLoaded(boolean loaded) {
        this.loaded = loaded;
        return this;
    }

    boolean isLoaded(){
        return loaded;
    }
}