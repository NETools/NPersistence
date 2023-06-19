package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Abstrakte Klasse zur Erstellung von Tabellenspalten
 */
abstract class NAttributeCreate {
	private NTableCreate mTable;

	private Class<?> mClass;
	private Field mField;

	private Annotation mAnnotation;

	public NAttributeCreate(NTableCreate table, Class<?> ctClass, Field field, Annotation annotation) {
		this.mTable = table;
		this.mClass = ctClass;
		this.mField = field;
		this.mAnnotation = annotation;

	}

	/**
	 * Definiert eine neue Spalte.
	 * @param current
	 * @return
	 * @throws Exception
	 */
	public abstract NPSFluentTabling getTableDescription(NPSFluentTabling current) throws Exception;

	/**
	 * Gibt die aktuelle Tabellenrepräsentation wieder
	 * @return
	 */
	public NTableCreate getTable() {
		return mTable;
	}

	/**
	 * Gibt die aktuelle Klassendefinition wieder
	 * @return
	 */
	public Class<?> getCurrentClass() {
		return mClass;
	}

	public Field getField() {
		return mField;
	}

	public Annotation getAnnotation() {
		return mAnnotation;
	}
}
