package org.ns.npersistence;

import java.lang.reflect.Field;

class NCompositionTableCreate extends NTableCreate {
	private NTableCreate superTable;

	public NTableCreate getSuperTable() {
		return this.superTable;
	}

	private Field field;

	public Field getCurrentField() {
		return this.field;
	}

	public NCompositionTableCreate(
			NSchemeCreate currentScheme,
			String className,
			NTableCreate superTable,
			Field field) throws ClassNotFoundException {
		super(currentScheme, className);

		this.superTable = superTable;
		this.field = field;

		/* org_ns_test_A_COMPOSITION_p */
		this.setSqlName(this.getSuperTable().getSqlName() + "_COMPOSITION_" + this.getCurrentField().getName());
	}
	 
	
}
