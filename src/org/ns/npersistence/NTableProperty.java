package org.ns.npersistence;

import java.lang.reflect.Field;

class NTableProperty {
    private boolean injected;
    private SearchResult<Field> keySearchResults;

    public boolean isInjected() {
        return injected;
    }

    public void setInjected(boolean status){
        this.injected = status;
    }

    public SearchResult<Field> getKeySearchResults() {
        return keySearchResults;
    }

    public void setKeySearchResults(SearchResult<Field> keySearchResults) {
        this.keySearchResults = keySearchResults;
    }
}
