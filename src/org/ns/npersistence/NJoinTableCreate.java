package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;
import org.ns.npersistence.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class NJoinTableCreate extends NAttributeCreate {

	private IterableProperty iterableProperty;

	public NJoinTableCreate(NTableCreate table, Class<?> ctClass, Field field, Annotation annotation) {
		super(table, ctClass, field, annotation);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NPSFluentTabling getTableDescription(NPSFluentTabling current) {
		return current;
	}

	public IterableProperty getProperty() {
		if (iterableProperty == null) {

			IterableConstants iterableType = (IterableConstants) AnnotationToolkit
					.getAnnotationValue(this.getAnnotation(), "mappingType");
			String addMethodName = (String) AnnotationToolkit.getAnnotationValue(this.getAnnotation(), "addMethodName");
			String removeMethodName = (String) AnnotationToolkit.getAnnotationValue(this.getAnnotation(),
					"removeMethodName");

			String fieldSignature =
					this.getField().getGenericType().getTypeName().substring(this.getField().getGenericType().getTypeName().indexOf("<") + 1).replace(">", "");

			iterableProperty = new IterableProperty();

			iterableProperty.setFieldName(this.getField().getName());
			iterableProperty.setIterableType(iterableType);
			iterableProperty.setAddMethod(addMethodName);
			iterableProperty.setRemoveMethod(removeMethodName);
			iterableProperty.setPrimitive(NPDataType.isWrapperType(fieldSignature));
			iterableProperty.setType(fieldSignature);

		}

		return iterableProperty;
	}

	public void createJoinTable() throws Exception {

		SearchResult<Field> primaryKeySearch = GeneralToolKit.isAnnotationPresent(this.getCurrentClass(),
				PrimaryKey.class);

		if (primaryKeySearch.getField() == null) {
			NDebugOutputHandler.getDefault().handle(50);
			NPSPolicy.setErrorOccured(true);
			return;
		}

		String primaryKeySignature = primaryKeySearch.getField().getType().getName();
		String sqlColumnTypeA = NPDataType.getSQLiteType(primaryKeySignature);

		NPSFluentTabling joinTable = NPSFluentSQLing.createNewTable();

		joinTable = joinTable.begin().ifNotExists();

		String sqlColumnTypeB = "";

		if (this.getProperty().isPrimitive()) {
			sqlColumnTypeB = NPDataType.getSQLiteType(iterableProperty.getType());

			joinTable = joinTable
					.callIt("JOIN_TABLE_OF_" + this.getTable().getSqlName().replace(".", "_") + "_X_PRIMITIVE_"
							+ iterableProperty.getFieldName().replace(".", "_"))
					.addColumn("pk_" + this.getTable().getClassDefinition().getName().replace(".", "_") + "_"
							+ primaryKeySearch.getField().getName(), sqlColumnTypeA, "")
					.addColumn("primitivedata_" + getField().getName().replace(".", "_"), sqlColumnTypeB, "")
					.addColumn("list_index", "INTEGER", "");

		} else {

			/* TODO: CHECK IF MANY TO MANY EXISTS */

			Class<?> peerClass = this.iterableProperty.getClassDefinition();
			SearchResult<Field> primaryKeyPeerSearch = GeneralToolKit.isAnnotationPresent(peerClass,
					PrimaryKey.class);

			if (primaryKeyPeerSearch.getField() == null) {
				NDebugOutputHandler.getDefault().handle(50);
				NPSPolicy.setErrorOccured(true);
				return;
			}

			sqlColumnTypeB = NPDataType.getSQLiteType(primaryKeyPeerSearch.getField().getType().getName());

			joinTable = joinTable
					.callIt("JOIN_TABLE_OF_" + this.getTable().getSqlName().replace(".", "_") + "_X_NONPRIMITIVE_"
							+ iterableProperty.getFieldName().replace(".", "_"))
					.addColumn("pk_" + this.getTable().getClassDefinition().getName().replace(".", "_") + "_"
							+ primaryKeySearch.getField().getName(), sqlColumnTypeA, "")

					.addColumn("pk_" + peerClass.getName().replace(".", "_") + "_"
							+ primaryKeyPeerSearch.getField().getName(), sqlColumnTypeB, "")
					.addColumn("list_index", "INTEGER", "");

			NTableCreate nextTable = null;

			if (peerClass.isAnnotationPresent(CompositionTable.class))
				nextTable = new NCompositionTableCreate(this.getTable().getScheme(), peerClass.getName(), this.getTable(), this.getField());
			else nextTable = new NTableCreate(this.getTable().getScheme(), peerClass.getName());

			nextTable.create();

		}

		if(NPSPolicy.hasErrorOccured()) {
			NDebugOutputHandler.getDefault().handle(52);
			return;
		}
		
		
		this.getTable().getScheme().addSqlQuery(joinTable.end());
	}

}
