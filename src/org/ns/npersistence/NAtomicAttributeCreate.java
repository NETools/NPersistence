package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;


class NAtomicAttributeCreate extends NAttributeCreate {

	public NAtomicAttributeCreate(NTableCreate table, Class<?> ctClass, Field field, Annotation annotation) {
		super(table, ctClass, field, annotation);
		// TODO Auto-generated constructor stub
	}

	public NPSFluentTabling getTableDescription(NPSFluentTabling current) {
		String columnName = getField().getName();

		String dataType = NPDataType.getSQLiteType(getField().getType().getName());
		Object defaultValue = NPDataType.getDefaults(getField().getType().getName());

		return current.addColumn(columnName, dataType, "");
	}

 

}
