package org.ns.npersistence;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class PrimaryKeyAttributeProcessorCreate {
	public void process(NTableCreate table, Field field, Annotation annotation)
			throws NotFoundException, CannotCompileException {

		String setterName =
				(String) AnnotationToolkit.getAnnotationValue(annotation, "setterName");
		if (!table.getTableProperty().getKeySearchResults().isPresent())
			setterName = "setGenPrimaryKey";

		NPrimaryKeyCreate primaryKey = new NPrimaryKeyCreate(table, table.getClassDefinition(), field, annotation);
		table.setCurrentTable(primaryKey.getTableDescription(table.getCurrentTable()));
	}
}
