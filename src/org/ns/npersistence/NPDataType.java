package org.ns.npersistence;

import javassist.CtField;
import javassist.NotFoundException;

import java.util.*;

/**
 * Klasser die Mapper zur Verfügung stellt, die Java-Datentypen auf SQLite-Datentypen abbildet.
 */
class NPDataType {
	private static HashMap<String, String> mMapping = new HashMap<>() {
		{
			put(String.class.getName(), "TEXT");
			put(Integer.class.getName(), "INTEGER");
			put(Long.class.getName(), "INTEGER");

			put(Byte.class.getName(), "TINYINT");
			put(Short.class.getName(), "SMALLINT");
			
			put(Float.class.getName(), "REAL");
			put(Double.class.getName(), "DOUBLE");
			
			put(int.class.getName(), "INTEGER");
			put(byte.class.getName(), "TINYINT");
			put(short.class.getName(), "SMALLINT");
			put(long.class.getName(), "INTEGER");

			put(float.class.getName(), "REAL");
			put(double.class.getName(), "DOUBLE");

			put(Boolean.class.getName(), "BOOLEAN");
			put(boolean.class.getName(), "BOOLEAN");

			put("java.sql.Date", "DATE");
			put("java.sql.Time", "DATETIME");
		}
	};

	
	private static HashMap<String, Object> mDefaults = new HashMap<>() {
		{
			put("java.lang.String", "\"\"");
			put("int", 0);
			put("long", 0);
			put("byte", 0);
			put("short", 0);

			put("float", 0.0f);
			put("double", 0.0);

			put("java.sql.Date", "\"" + new Date() + "\"");
			// put("java.sql.Time", "\"" + new Time(0) + "\"");
		}
	};


	public static String getSQLiteType(String fieldName) {
		return mMapping.get(fieldName);
	}
	
	public static Object getDefaults(String fieldName) {
		return mDefaults.get(fieldName);
	}
	

	/*
	 * SOURCE:
	 * https://stackoverflow.com/questions/709961/determining-if-an-object-is-of-
	 * primitive-type
	 */
	private static final Set<String> WRAPPER_TYPES = new HashSet<String>(Arrays.asList(Boolean.class.getName(), Character.class.getName(),
			Byte.class.getName(), Short.class.getName(), Integer.class.getName(), Long.class.getName(), Float.class.getName(), Double.class.getName(), Void.class.getName(), String.class.getName()));

	public static boolean isWrapperType(String name) {
		return WRAPPER_TYPES.contains(name);
	}

}
