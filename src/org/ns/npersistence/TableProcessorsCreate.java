package org.ns.npersistence;

class TableProcessorsCreate {


    public static AtomicAttributeProcessorCreate getAtomicAttributeProcessor() {
        return new AtomicAttributeProcessorCreate();
    }

    public static PrimaryKeyAttributeProcessorCreate getPrimaryKeyAttributeProcessor() {
        return new PrimaryKeyAttributeProcessorCreate();
    }

    public static CompositionAttributeProcessorCreate getCompositionAttributeProcessor() {
        return new CompositionAttributeProcessorCreate();
    }

    public static IterableAttributeProcessorCreate getIterableAttributeProcessor() {
        return new IterableAttributeProcessorCreate();
    }


    public static TableSignatureProcessorCreate getTableSignatureProcessor() {
        return new TableSignatureProcessorCreate();
    }

    public static MediaTypeAttributeProcessorCreate getMediaTypeAttributeProcessorCreate() {
		return new MediaTypeAttributeProcessorCreate();
    }
}
