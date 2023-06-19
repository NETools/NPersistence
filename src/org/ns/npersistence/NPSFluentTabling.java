package org.ns.npersistence;


/**
 * Klasse zur Erstellung einert sql-CREATE_TABLE-Anweisung
 */
class NPSFluentTabling {

	private StringBuilder mSqlStringBuilder;

	public NPSFluentTabling() {
		this.mSqlStringBuilder = new StringBuilder();
	}

	public NPSFluentTabling begin() {
		mSqlStringBuilder.append("CREATE TABLE ");
		return this;
	}

	public NPSFluentTabling ifNotExists() {
		mSqlStringBuilder.append("IF NOT EXISTS ");
		return this;
	}
	
	public NPSFluentTabling callIt(String tableName) {
		tableName = GeneralToolKit.sha1(tableName);
		mSqlStringBuilder.append(tableName + " (\n");
		return this;
	}
	
	public NPSFluentTabling addColumn(String columnName, String datatype, String attribute) {
		mSqlStringBuilder.append(columnName + " " + datatype + " " + attribute + ",\n");
		return this;
	}
	
	
	public String end() {
		mSqlStringBuilder.deleteCharAt(mSqlStringBuilder.length() - 2);
		mSqlStringBuilder.deleteCharAt(mSqlStringBuilder.length() - 1);
		mSqlStringBuilder.append(");");
		return this.toString();
	}
	
	
	@Override
	public String toString() {
		return this.mSqlStringBuilder.toString();
	}
	
	
	
	
	
}
