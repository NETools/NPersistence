package org.ns.npersistence;

import org.ns.npersistence.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class NCompositionAttributeCreate extends NAttributeCreate {

	public NCompositionAttributeCreate(NTableCreate table, Class<?> ctClass, Field field, Annotation annotation) {
		super(table, ctClass, field, annotation);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NPSFluentTabling getTableDescription(NPSFluentTabling current) throws Exception {

		Class<?> ctPeerClass = Class.forName(this.getField().getType().getName());

		SearchResult<Field> primaryKeyPeerSearchResult = GeneralToolKit.isAnnotationPresent(ctPeerClass,
				PrimaryKey.class);
		
		if (primaryKeyPeerSearchResult.getField() == null) {
			NDebugOutputHandler.getDefault().handle(50);
			NPSPolicy.setErrorOccured(true);
			return current;
		}

		String primaryKeySqlDtb = NPDataType.getSQLiteType(primaryKeyPeerSearchResult.getField().getType().getName());
		return current.addColumn("fk_" + this.getField().getName() + "_" + primaryKeyPeerSearchResult.getField().getName(), primaryKeySqlDtb, "");

	}

}
