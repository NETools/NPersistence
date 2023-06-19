package org.ns.npersistence;

import org.ns.npersistence.annotations.PrimaryKey;

class NPrimaryKeyProcessorDelete {
    public void process(NTableDelete tableDelete) throws Exception {
        tableDelete.setCurrentInstancePrimaryKey(GeneralToolKit.isAnnotationPresent(tableDelete.getCurrentInstanceClassDlt(),
                PrimaryKey.class));
    }


    public static NPrimaryKeyProcessorDelete getInstance() {
        return new NPrimaryKeyProcessorDelete();
    }

}
