package org.ns.npersistence;

import java.lang.reflect.Field;

class NCompositionTableSelect extends NTableSelect {
    private NTableSelect tableSelect;

    private Field field;

    public NTableSelect getTableSelect() {
        return this.tableSelect;
    }

    public Field getField(){
        return this.field;
    }

    public NCompositionTableSelect(NSchemeSelect schemeSelect, Class<?> currentClassDefinition, NTableSelect tableSelect, Field field) throws Exception {
        super(schemeSelect, currentClassDefinition);
        this.tableSelect = tableSelect;
        this.setTableSqlName(this.getTableSelect().getTableSqlName() + "_COMPOSITION_" + field.getName());
    }
}
