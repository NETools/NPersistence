package org.ns.npersistence;

class TableProcessorsSelect {
    public static PrimaryKeySelectProcessor getPrimaryKeySelectProcessor() {
        return new PrimaryKeySelectProcessor();
    }

    public static AtomicSelectProcessor getAtomicSelectProcessor() {
        return new AtomicSelectProcessor();
    }

    public static CompositionSelectProcessor getCompositionSelectProcessor() {
        return new CompositionSelectProcessor();
    }

    public static IterableSelectProcessor getIterableSelectProcessor() {
        return new IterableSelectProcessor();
    }

    public static MediaTypeSelectProcessor getMediaTypeSelectProcessor() {
        return new MediaTypeSelectProcessor();
    }


}
