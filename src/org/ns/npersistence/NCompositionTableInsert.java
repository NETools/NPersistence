package org.ns.npersistence;

import java.lang.reflect.Field;

class NCompositionTableInsert extends NTableInsert {
    private NTableInsert superTable;

    public NTableInsert getSuperTable() {
        return this.superTable;
    }

    private Field field;

    public Field getCurrentField() {
        return this.field;
    }

    public NCompositionTableInsert(NSchemeInsert currentScheme, Object object, NTableInsert superTable, Field field) {
        super(currentScheme, object);

        this.superTable = superTable;
        this.field = field;

        /* org_ns_test_A_COMPOSITION_p */
        this.setSqlName(this.getSuperTable().getSqlName() + "_COMPOSITION_" + this.getCurrentField().getName());
    }
}





