package org.ns.npersistence;


/**
 * Klasse zur Erstellung einer sql-INSERT-Anweisung
 */
class NPSFluentInserting {

	private StringBuilder mSqlStringBuilder;
	private int mValuesCount;

	public NPSFluentInserting() {
		this.mSqlStringBuilder = new StringBuilder();
	}

	public NPSFluentInserting begin() {
		mSqlStringBuilder.append("INSERT INTO ");
		return this;
	}

	public NPSFluentInserting toInsertIn(String tableName) {
		tableName = GeneralToolKit.sha1(tableName);
		mSqlStringBuilder.append(tableName + " (");
		return this;
	}

	public NPSFluentInserting values(String columnName) {
		if (mValuesCount == 0)
			mSqlStringBuilder.append(columnName);
		else
			mSqlStringBuilder.append("," + columnName);

		mValuesCount++;

		return this;
	}

	public NPSFluentInserting addMeta(String metaData){
		mSqlStringBuilder.append(metaData);
		return this;
	}

	public String end() {
		
		mSqlStringBuilder.append(") VALUES(");
		mSqlStringBuilder.append("?");
		for (int i = 1; i < mValuesCount; i++)
			mSqlStringBuilder.append(",?");
		mSqlStringBuilder.append(");");
		
		return this.toString();
	}

	@Override
	public String toString() {
		return this.mSqlStringBuilder.toString();
	}

}
