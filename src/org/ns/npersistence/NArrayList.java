package org.ns.npersistence;


import org.ns.npersistence.annotations.PrimaryKey;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

class NArrayList<T> extends ArrayList<T> {

    private Object currentInstance;
    private String fieldName;


    private ArrayList<Object> cachedPrimaryKeys = new ArrayList<>();
    private NJoinTableSelect instanceJoinTable;

    private boolean activated;
    private boolean loaded;

    void setActivated(boolean flag) {
        this.activated = flag;
    }

    void setCachedPrimaryKeys(ArrayList<Object> cachedPrimaryKeys) {
        this.cachedPrimaryKeys = cachedPrimaryKeys;
    }

    void setInstanceJoinTable(NJoinTableSelect instanceJoinTable) {
        this.instanceJoinTable = instanceJoinTable;
    }

    public NArrayList(Object currentInstance, String fieldName) {
        super();

        this.currentInstance = currentInstance;
        this.fieldName = fieldName;
    }


    @Override
    public boolean add(T t) {
        if (!activated)
            return super.add(t);

        try {

            NEntityUpdateHandler.getDefault().beginUpdate(currentInstance, fieldName);

            if (t.getClass().getName().substring(0
                    , 11).equals("_proxyClass")) {

                NDebugOutputHandler.getDefault().handle(65);
                return false;
            }


            NSchemeInsert schemeInsert = new NSchemeInsert();
            schemeInsert.insertLater(NEntityUpdateHandler.getDefault().getEntityUpdateHandlerProperty().getInstanceSqlName(), null,
                    t,
                    false);


            NJoinTableInsert joinTableInsert = new NJoinTableInsert(schemeInsert.getTable(),
                    NEntityUpdateHandler.getDefault().getEntityUpdateHandlerProperty().getModifiedField(),
                    NEntityUpdateHandler.getDefault().getEntityUpdateHandlerProperty().getFieldAnnotation());


            joinTableInsert.setPrimaryKeySearch(GeneralToolKit.isAnnotationPresent(currentInstance.getClass().getSuperclass(),
                    PrimaryKey.class));
            joinTableInsert.setCurrentObjectInstance(currentInstance);

            joinTableInsert.insert(t,
                    GeneralToolKit.getDefaultRandomGenerator().nextInt(100000000));


            schemeInsert.write();

            this.cachedPrimaryKeys.add(NPrimaryKeyGenerator.getDefault().getLastGeneratedPrimaryKey());

            return true;


        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (!activated)
            return super.remove(o);

        try {

            NEntityUpdateHandler.getDefault().beginUpdate(currentInstance, fieldName);
            NRecyclement.getDefault().add(currentInstance, o, fieldName);

            NIterableElementDelete.getInstance(NEntityUpdateHandler.getDefault().getEntityUpdateHandlerProperty()).
                    delete(o);

            var primaryKeyField =
                    GeneralToolKit.isAnnotationPresent(o.getClass().getSuperclass(),
                            PrimaryKey.class);


            primaryKeyField.getField().setAccessible(true);

            this.cachedPrimaryKeys.remove(primaryKeyField.getField().get(o));

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public T remove(int index) {

        if (!activated)
            return super.remove(index);

        var deletedObject = this.get(index);
        this.remove(deletedObject);

        return deletedObject;

    }

    @Override
    public int size() {
        if (!activated)
            return super.size();

        return cachedPrimaryKeys.size();
    }


    @Override
    public Iterator<T> iterator() {
        return new NArrayListIterator();
    }

    @Override
    public void clear() {
        if (!activated)
            super.clear();

        int size = this.size();
        for (int i = size - 1; i >= 0; i--)
            remove(i);
    }

    @Override
    public boolean contains(Object o) {
        if (!activated)
            return super.contains(o);

        try {
            return this.cachedPrimaryKeys.contains(GeneralToolKit.getFieldValue(o, "sqlPrimaryKeyValue"));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public T get(int index) {
        if (!activated)
            return super.get(index);


        /* Prevents duplicates in NSQLResultSet */
        NPersistenceSelectPolicy.setRequired(false);

        T data = null;
        try {
            data = (T) instanceJoinTable.select(this.cachedPrimaryKeys.get(index));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void add(int index, T element) {
        if (!activated)
            super.add(index, element);

        throw new IllegalArgumentException("[ERROR] INSERT AT INDEX NOT SUPPORTED FOR REMOTE OBJECTS!");
    }

    class NArrayListIterator implements Iterator<T> {
        private int index;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public T next() {
            return get(index++);
        }
    }
}