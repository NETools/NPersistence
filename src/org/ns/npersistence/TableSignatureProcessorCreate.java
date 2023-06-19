package org.ns.npersistence;

import org.ns.npersistence.annotations.PrimaryKey;

class TableSignatureProcessorCreate {

	public void signClass(NTableCreate table) throws Exception {
		table.getTableProperty().setKeySearchResults(GeneralToolKit.isAnnotationPresent(table.getClassDefinition(), PrimaryKey.class));
	}
}
