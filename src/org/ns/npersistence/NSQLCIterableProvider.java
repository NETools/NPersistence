package org.ns.npersistence;

import org.ns.npersistence.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;

/**
 * NSQL-Compiler-spezifische iterable-Prozessor.
 */
class NSQLCIterableProvider {

    private static int NSQLCI_IDENTFIER = 0;
    private static HashSet<String> USED_JOIN_TABLES = new HashSet<>();

    public static void reset() {
        NSQLCI_IDENTFIER = 0;
        USED_JOIN_TABLES.clear();
    }

    private NSQLCProvider nsqlcProvider;
    private IterableProperty iterableProperty;

    private Class<?> currentClass;
    private Class<?> secondClass;


    private Annotation annotation;

    private Field field;

    public Class<?> getCurrentClass() {
        return currentClass;
    }

    public Class<?> getSecondClass() {
        return secondClass;
    }

    public Field getField() {
        return field;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public IterableProperty getProperty() {
        if (iterableProperty != null)
            return iterableProperty;

        var iterableType =
                (IterableConstants) AnnotationToolkit.getAnnotationValue(this.getAnnotation(),
                        "mappingType");
        var addMethodName =
                (String) AnnotationToolkit.getAnnotationValue(this.getAnnotation(),
                        "addMethodName");
        var removeMethodName =
                (String) AnnotationToolkit.getAnnotationValue(this.getAnnotation(),
                        "removeMethodName");

        var fieldSignature = this.getField()
                .getGenericType()
                .getTypeName()
                .substring(this.getField().getGenericType().getTypeName().indexOf("<") + 1)
                .replace(">", "");

        iterableProperty = new IterableProperty();
        iterableProperty.setFieldName(this.getField().getName());
        iterableProperty.setIterableType(iterableType);
        iterableProperty.setAddMethod(addMethodName);
        iterableProperty.setRemoveMethod(removeMethodName);
        iterableProperty.setPrimitive(NPDataType.isWrapperType(fieldSignature));
        iterableProperty.setType(fieldSignature);

        return iterableProperty;
    }

    public NSQLCIterableProvider(NSQLCProvider nsqlcProvider, Class<?> currentClass, Field field,
                                 Annotation annotation) throws Exception {

        this.nsqlcProvider = nsqlcProvider;
        this.currentClass = currentClass;

        this.annotation = annotation;
        this.field = field;

        this.secondClass = this
                .getProperty().getClassDefinition();
    }


    public void getResult(StringBuilder currentSqlTableName) throws Exception {

        Field currentPkField =
                GeneralToolKit.isAnnotationPresent(this.currentClass, PrimaryKey.class).getField();
        String pk1_sql_name =
                "pk_" + this.currentClass.getName().replace(".", "_") + "_" + currentPkField.getName();

        String joinTableName = "";
        String pk2_sql_name = "";

        if (this.getProperty().isPrimitive()) {
            pk2_sql_name = "primitivedata_" + this.getField().getName();
            joinTableName =
                    "JOIN_TABLE_OF_" + currentSqlTableName + "_X_PRIMITIVE_" + this.getField().getName();

        } else {
            Field secondPkField =
                    GeneralToolKit.isAnnotationPresent(this.secondClass, PrimaryKey.class).getField();
            pk2_sql_name =
                    "pk_" + this.secondClass.getName().replace(".", "_") + "_" + secondPkField.getName();
            joinTableName =
                    "JOIN_TABLE_OF_" + currentSqlTableName + "_X_NONPRIMITIVE_" + this.getField().getName();
        }

        joinTableName = GeneralToolKit.sha1(joinTableName);

        if (!USED_JOIN_TABLES.contains(joinTableName)) {
            USED_JOIN_TABLES.add(joinTableName);
            NSQLCI_IDENTFIER++;
        }

        this.nsqlcProvider.putClassResult(
                joinTableName,
                "_j_" + NSQLCI_IDENTFIER,
                pk1_sql_name,
                pk2_sql_name);
    }
}
