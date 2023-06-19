package org.ns.npersistence;

class TableProcessorsInsert {
    public static AtomicAttributeProcessorInsert getAtomicAttributeProcessor() {
        return new AtomicAttributeProcessorInsert();
    }

    public static PrimaryKeyAttributeProcessorInsert getPrimaryKeyAttributeProcessor() {
        return new PrimaryKeyAttributeProcessorInsert();
    }

    public static CompositionAttributeProcessorInsert getCompositionAttributeProcessor() {
        return new CompositionAttributeProcessorInsert();
    }

    public static IterableAttributeProcessorInsert getIterableAttributeProcessor() {
        return new IterableAttributeProcessorInsert();
    }

    public static MediaTypeAttributeProcessorInsert getMediaTypeAttributeProcessorInsert() {
        return new MediaTypeAttributeProcessorInsert();
    }
}
