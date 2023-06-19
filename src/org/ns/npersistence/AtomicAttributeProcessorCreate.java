package org.ns.npersistence;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class AtomicAttributeProcessorCreate {
    void process(NTableCreate table, Field field, Annotation annotation)  {
        NAtomicAttributeCreate attribute = new NAtomicAttributeCreate(table, table.getClassDefinition(), field, annotation);
        table.setCurrentTable(attribute.getTableDescription(table.getCurrentTable()));
    }
}
