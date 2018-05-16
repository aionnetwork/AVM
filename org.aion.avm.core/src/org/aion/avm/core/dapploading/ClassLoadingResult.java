package org.aion.avm.core.dapploading;

/**
 * @author Roman Katerinenko
 */
class ClassLoadingResult {
    private boolean loaded;
    private String failDescription;

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