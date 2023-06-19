package org.ns.npersistence;

import org.ns.npersistence.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

class NTableSelect {

    private NSchemeSelect schemeSelect;

    private Object currentInstanceOfClass;
    private Class<?> currentClassDefinition;

    private Object primaryKey;
    private String tableSqlName;

    private ArrayList<Object> compositionObjects;

    private SearchResult<Field> primaryKeyField;
    private ResultSet resultSet;

    private ProxyGenerator proxyGenerator;

    public NSchemeSelect getSchemeSelect() {
        return this.schemeSelect;
    }

    public Object getCurrentInstanceOfClass() {
        return this.currentInstanceOfClass;
    }

    public Class<?> getCurrentClassDefinition() {
        if (NPersistenceSelectPolicy.isDetached())
            return this.currentClassDefinition;
        return this.currentClassDefinition.getSuperclass();
    }

    public void setPrimaryKey(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Object getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKeyField(SearchResult<Field> primaryKeyField) {
        this.primaryKeyField = primaryKeyField;
    }

    public SearchResult<Field> getPrimaryKeyField() {
        return primaryKeyField;
    }

    public void setTableSqlName(String tableSqlName) {
        this.tableSqlName = tableSqlName;
    }

    public String getTableSqlName() {
        return this.tableSqlName;
    }

    public void addCompositionObject(Object compositionObject) {
        this.compositionObjects.add(compositionObject);
    }

    public ArrayList<Object> getCompositionObjects() {
        return compositionObjects;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public ResultSet getResultSet() {
        return this.resultSet;
    }

    public NTableSelect(NSchemeSelect schemeSelect, Class<?> currentClassDefinition) throws Exception {
        this.schemeSelect = schemeSelect;
        this.proxyGenerator = new ProxyGenerator();

        if (NPersistenceSelectPolicy.isDetached())
            this.currentClassDefinition = currentClassDefinition;
        else
            this.currentClassDefinition = this.proxyGenerator.getProxyClass(currentClassDefinition,
                    new ProxyClassProperty());

        this.compositionObjects = new ArrayList<>();

        this.tableSqlName = this.getCurrentClassDefinition().getName().replace(".", "_");
        this.find();

    }

    public NTableSelect(NSchemeSelect schemeSelect, Class<?> currentClassDefinition, String root) throws Exception {
        this(schemeSelect, currentClassDefinition);
        this.tableSqlName = root;
    }

    public void find() throws Exception {
        this.setPrimaryKeyField(GeneralToolKit.isAnnotationPresent(this.getCurrentClassDefinition(),
                PrimaryKey.class));
    }

    public void select() throws Exception {
        try {
            this.currentInstanceOfClass = this.currentClassDefinition.getDeclaredConstructor().newInstance();

            if (!NPersistenceSelectPolicy.isDetached()) {
                GeneralToolKit.setValue(this.getCurrentInstanceOfClass(), "sqlQualifiedName",
                        this.getTableSqlName());
                GeneralToolKit.setValue(this.getCurrentInstanceOfClass(),
                        "sqlPrimaryKeyName", "pk_" + this.getPrimaryKeyField().getField().getName());
                GeneralToolKit.setValue(this.getCurrentInstanceOfClass(), "sqlPrimaryKeyValue",
                        this.getPrimaryKey());
                GeneralToolKit.setValue(this.getCurrentInstanceOfClass(), "sqlCurrentSessionName",
                        NEntityUpdateHandler.getDefault().getCurrentSession()
                                .getDatabaseName());
            }

        } catch (Exception ex) {
            NDebugOutputHandler.getDefault().handle(71);
            NPSPolicy.setErrorOccured(true);
            return;
        }


        TableProcessorsSelect.getPrimaryKeySelectProcessor().process(this);


        if (this.resultSet == null || (this.getResultSet() != null && this.getResultSet().isClosed())) {
            // this.currentInstanceOfClass = null;
            return;
        }

        switch (NEntityUpdateHandler.getDefault().getCurrentSession().getCurrentLoadedDriver()) {
            case MySQL:
                this.resultSet.next();
                break;
        }

        this.process(this.getCurrentClassDefinition().getDeclaredFields());
    }

    public void process(Field[] fields) throws Exception {
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Annotation[] annotations = field.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(AtomicAttribute.class)) {
                        TableProcessorsSelect.getAtomicSelectProcessor().process(this, field, annotation);
                    } else if (annotation.annotationType().equals(CompositeAttribute.class)) {
                        TableProcessorsSelect.getCompositionSelectProcessor().process(this, field, annotation);
                    } else if (annotation.annotationType().equals(IterableAttribute.class)) {
                        TableProcessorsSelect.getIterableSelectProcessor().process(this, field, annotation);
                    } else if (annotation.annotationType().equals(MediaAttribute.class)) {
                        TableProcessorsSelect.getMediaTypeSelectProcessor().process(this, field, annotation);
                    }
                }
            }
        } catch (SQLException exception) {
            switch (exception.getErrorCode()) {
                case 0:
                    NDebugOutputHandler.getDefault().handle(15);
                    System.out.println("ADDITION: " + exception.getMessage());
                    NPSPolicy.setErrorOccured(true);
                    break;
                default:
                    NDebugOutputHandler.getDefault().handle(82, exception.getMessage());
                    break;
            }
        }
    }
}
