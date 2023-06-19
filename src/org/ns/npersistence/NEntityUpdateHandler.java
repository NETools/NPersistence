/* بسم الله الرحمن الرحيم */
package org.ns.npersistence;

import org.ns.npersistence.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;


/**
 * Klasse zur Verwaltung und Echtzeitsynchronisation persistierter Objekte
 */
public class NEntityUpdateHandler {

    private static int row_index;

    private static NEntityUpdateHandler updateHandler;

    public static NEntityUpdateHandler getDefault() {
        if (updateHandler == null) {
            updateHandler = new NEntityUpdateHandler();
            row_index = GeneralToolKit.getDefaultRandomGenerator().nextInt(100000000);
        }
        return updateHandler;
    }

    /**
     * Verhindert die manuelle Instanziierung
     */
    NEntityUpdateHandler() {
    }


    private NPersistenceSession currentSession;
    private NEntityUpdateHandlerProperty entityUpdateHandlerProperty;

    /**
     * Gibt die aktuelle Session zurück.
     *
     * @return
     */
    public NPersistenceSession getCurrentSession() {
        return currentSession;
    }

    public NEntityUpdateHandlerProperty getEntityUpdateHandlerProperty() {
        return entityUpdateHandlerProperty;
    }

    void setCurrentSession(NPersistenceSession currentSession) {
        this.currentSession = currentSession;
    }

    /**
     * Wird aus jeder Proxy-Klasse aufgerufen, wenn der getter() bemüht wird.
     *
     * @param currentInstance
     * @param fieldName
     * @throws Exception
     */
    public void checkRetrieveConsistency(Object currentInstance, String fieldName) throws Exception {
        Field field = currentInstance.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);

        if (field.get(currentInstance) == null)
            return;


        if (!this.getCurrentSession().isSessionOpened()) {
            NDebugOutputHandler.getDefault().handle(61);
            entityUpdateHandlerProperty.setInvalid(true);
            return;
        }

        if (!currentInstance.getClass().getName().substring(0, 11).equals("_proxyClass") || (field.isAnnotationPresent(CompositeAttribute.class)
                && !field.get(currentInstance).getClass().getName().substring(0, 11).equals("_proxyClass")))
            if (!NEntityUpdateHandlerPolicy.isAutoSyncActivated()) {
                NDebugOutputHandler.getDefault().handle(60);
            } else {
                Object fieldValue
                        = field.get(currentInstance);


                var sqlQualifiedName = (String) GeneralToolKit.getFieldValue(currentInstance,
                        "sqlQualifiedName");

                String callingClass = "";

                if (sqlQualifiedName.indexOf("_COMPOSITION_") == -1) {
                    callingClass = sqlQualifiedName.replace("_", ".");
                } else
                    callingClass = sqlQualifiedName
                            .substring(0, sqlQualifiedName.indexOf("_COMPOSITION_"))
                            .replace("_", ".");

                String callHierachy = sqlQualifiedName
                        .replace("_COMPOSITION_", ".")
                        .replace("_", ".")
                        .replace(callingClass + ".", "")
                        .replace(callingClass, "");


                var pkField =
                        GeneralToolKit
                                .isAnnotationPresent(fieldValue.getClass(), PrimaryKey.class).getField();


                String nsqlQuery =
                        "SELECT %new"
                                + (callHierachy.length() == 0 ? "" : ".")
                                + callHierachy
                                + "."
                                + fieldName
                                + " FROM $"
                                + callingClass
                                + " :a WHERE !a"
                                + (callHierachy.length() == 0 ? "" : ".")
                                + callHierachy + "."
                                + fieldName + "."
                                + pkField.getName()
                                + " = '"
                                + pkField.get(fieldValue)
                                + "';";

                NDebugOutputHandler
                        .getDefault()
                        .handle(80, "[NSQL] " + nsqlQuery);

                field.set(currentInstance, NEntityUpdateHandler
                        .getDefault()
                        .getCurrentSession()
                        .getByNSQL(nsqlQuery, NPersistenceSession.RetrieveMode.Attached).get(fieldValue.getClass(), 0));

                NDebugOutputHandler
                        .getDefault()
                        .handle(80, "[AUTO_SYNC] " + "COMPLETED!");
            }


    }


    /**
     * Wird aus jeder Proxy-Klasse ausgerufen, wenn der setter() bemüht wird, bevor eine Veränderung
     * erfolgt ist.
     *
     * @param currentInstance
     * @param fieldName
     * @throws Exception
     */
    public void beginUpdate(Object currentInstance, String fieldName) throws Exception {

        entityUpdateHandlerProperty = new NEntityUpdateHandlerProperty();

        if (!this.getCurrentSession().isSessionOpened()) {
            NDebugOutputHandler.getDefault().handle(61);
            entityUpdateHandlerProperty.setInvalid(true);
            return;
        }

        if (!currentInstance.getClass().getName().substring(0, 11).equals("_proxyClass")) {
            NDebugOutputHandler.getDefault().handle(60);
            entityUpdateHandlerProperty.setInvalid(true);
            return;
        }


        String currentInstanceSessionName =
                GeneralToolKit.getFieldValue(currentInstance, "sqlCurrentSessionName") + "";


        if (!currentInstanceSessionName.equals(this.getCurrentSession().getDatabaseName())) {
            NDebugOutputHandler.getDefault().handle(62);
            entityUpdateHandlerProperty.setInvalid(true);
            return;
        }

        entityUpdateHandlerProperty.setInstanceSqlName(GeneralToolKit.getFieldValue(currentInstance,
                "sqlQualifiedName") + "");
        entityUpdateHandlerProperty.setInstancePrimaryKeyName(GeneralToolKit.getFieldValue(currentInstance,
                "sqlPrimaryKeyName") + "");
        entityUpdateHandlerProperty.setInstancePrimaryKeyValue(GeneralToolKit.getFieldValue(currentInstance,
                "sqlPrimaryKeyValue"));

        entityUpdateHandlerProperty.setCurrentInstance(currentInstance);

        entityUpdateHandlerProperty.setModifiedField(currentInstance.getClass().getSuperclass().getDeclaredField(fieldName));
        entityUpdateHandlerProperty.setModifiedFieldName(fieldName);
        entityUpdateHandlerProperty.setSqlObjectClassName(currentInstance.getClass().getSuperclass().getName().replace(".", "_"));

        if (this.entityUpdateHandlerProperty.getModifiedField().isAnnotationPresent(PrimaryKey.class)) {
            NDebugOutputHandler.getDefault().handle(70);
            entityUpdateHandlerProperty.setInvalid(true);
            return;
        }

        entityUpdateHandlerProperty.setLastModifiedValue(this.entityUpdateHandlerProperty.getModifiedField().get(currentInstance));
        Annotation[] annotations =
                this.entityUpdateHandlerProperty.getModifiedField().getDeclaredAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(AtomicAttribute.class) || annotation.annotationType().equals(CompositeAttribute.class)
                    || annotation.annotationType().equals(MediaAttribute.class)) {
                this.entityUpdateHandlerProperty.setFieldAnnotation(annotation);
            } else if (annotation.annotationType().equals(IterableAttribute.class)) {
                this.entityUpdateHandlerProperty.setFieldAnnotation(annotation);
                this.entityUpdateHandlerProperty.setLastModifiedValue((new ArrayList<>((ArrayList<?>) this.entityUpdateHandlerProperty.getLastModifiedValue())));
            }
        }

    }

    /**
     * Wird aus jeder Proxy-Klasse ausgerufen, wenn der setter() bemüht wird, nachdem eine Veränderung
     * erfolgt ist.
     *
     * @param currentInstance
     * @param fieldName
     * @throws Exception
     */
    public void endUpdate(Object currentInstance, String fieldName) throws Exception {
        if (entityUpdateHandlerProperty.isInvalid()) return;

        Object currentValue = this.entityUpdateHandlerProperty.getModifiedField().get(currentInstance);
        String currentInstanceSessionName = "";

        this.entityUpdateHandlerProperty.setCurrentModifiedValue(currentValue);

        if (entityUpdateHandlerProperty.getFieldAnnotation().annotationType().equals(AtomicAttribute.class)) {
            NUpdateAtomic.getInstance(this.entityUpdateHandlerProperty).buildQuery(fieldName).write(currentValue);
        } else if (entityUpdateHandlerProperty.getFieldAnnotation().annotationType().equals(MediaAttribute.class)) {
            NUpdateMedia.getInstance(this.entityUpdateHandlerProperty).buildQuery(fieldName).write(currentValue);
        } else if (entityUpdateHandlerProperty.getFieldAnnotation().annotationType().equals(CompositeAttribute.class)) {
            boolean isProxy = false;
            /* CHECK SESSION */


            if (this.entityUpdateHandlerProperty.getCurrentModifiedValue() != null && (isProxy =
                    this.entityUpdateHandlerProperty.getCurrentModifiedValue().getClass().getName().substring(0
                            , 11).equals("_proxyClass"))) {
                currentInstanceSessionName =
                        GeneralToolKit.getFieldValue(this.entityUpdateHandlerProperty.getCurrentModifiedValue(), "sqlCurrentSessionName") + "";

                if (!currentInstanceSessionName.equals(this.getCurrentSession().getDatabaseName())) {
                    NDebugOutputHandler.getDefault().handle(62);
                    return;
                }

            }


            NTableDelete tableDelete = null;
            if (this.entityUpdateHandlerProperty.getLastModifiedValue() != null &&
                    this.entityUpdateHandlerProperty.getLastModifiedValue().getClass().getSuperclass().isAnnotationPresent(Table.class)) {
                tableDelete = new NTableDelete(this.entityUpdateHandlerProperty.getLastModifiedValue());
                tableDelete.loadPrimaryKey();
                if (NEntityUpdateHandlerPolicy.doForceDeleteTable())
                    tableDelete.delete();

            } else if (this.entityUpdateHandlerProperty.getLastModifiedValue() != null &&
                    this.entityUpdateHandlerProperty.getLastModifiedValue().getClass().getSuperclass().isAnnotationPresent(CompositionTable.class)) {
                tableDelete =
                        new NCompositionTableDelete(this.entityUpdateHandlerProperty.getLastModifiedValue());
                tableDelete.loadPrimaryKey();
                tableDelete.delete();


            }

            NUpdateAtomic foreignKeyAttribute =
                    NUpdateAtomic.getInstance(this.entityUpdateHandlerProperty).buildQuery("fk_" + fieldName + "_" +
                            tableDelete.getCurrentInstancePrimaryKey().getField().getName());


            if (this.entityUpdateHandlerProperty.getCurrentModifiedValue() != null &&
                    !isProxy) {
                NSchemeInsert schemeInsert = new NSchemeInsert();
                schemeInsert.insertLater(this.entityUpdateHandlerProperty.getInstanceSqlName(),
                        this.entityUpdateHandlerProperty.getModifiedField(),
                        this.entityUpdateHandlerProperty.getCurrentModifiedValue(), true);


                schemeInsert.write();

                Object foreignKeyValue =
                        GeneralToolKit.isAnnotationPresent(this.entityUpdateHandlerProperty.
                                getCurrentModifiedValue().getClass(), PrimaryKey.class).
                                getField().get(this.entityUpdateHandlerProperty.getCurrentModifiedValue());

                foreignKeyAttribute.write(foreignKeyValue);

            } else if (isProxy) {
                Object foreignKeyValue =
                        GeneralToolKit.isAnnotationPresent(this.entityUpdateHandlerProperty.
                                getCurrentModifiedValue().getClass().getSuperclass(), PrimaryKey.class).
                                getField().get(this.entityUpdateHandlerProperty.getCurrentModifiedValue());
                foreignKeyAttribute.write(foreignKeyValue);
            } else if (this.entityUpdateHandlerProperty.getCurrentModifiedValue() == null) {
                foreignKeyAttribute.write(null);
            }

        } else if (entityUpdateHandlerProperty.getFieldAnnotation().annotationType().equals(IterableAttribute.class)) { // OBSOLETE

            ArrayList<?> lastList = (ArrayList<?>) this.entityUpdateHandlerProperty.getLastModifiedValue();
            ArrayList<?> currentList =
                    (ArrayList<?>) new ArrayList<>((ArrayList<?>) this.entityUpdateHandlerProperty.getCurrentModifiedValue());

            /* DELETION HAPPENED */
            if (lastList.size() > currentList.size()) {
                Object deletedObject = null;

                for (int i = 0; i < currentList.size(); i++)
                    lastList.remove(currentList.get(i));

                deletedObject = lastList.get(0);

                /* -- 1 -- */
                /* -- RECYCLE -- */

                /*
                 * Vorgehensweise:
                 * Erstelle in der Proxy-Klasse soviele Recycle-Arraylisten wie es Arraylisten mit
                 * kompositionellen
                 * Elementen gibt, benenne sie ___recycle[NAME_DER_ARRAYLIST]
                 *
                 * Bei jeder Löschung zu löschendes Objekt in den RAM laden, und in
                 * ___recycle[NAME_DER_ARRAYLISTE] hinzufügen.
                 * Klasseninstanz der HashMap in NRecycleManaging übergeben.
                 *
                 * Bei Rollback: .rollBack(classInstance);....
                 *
                 * */

                NRecyclement.getDefault().add(currentInstance, deletedObject, fieldName);

                NIterableElementDelete.getInstance(this.entityUpdateHandlerProperty).delete(deletedObject);

            } /* ADDITION HAPPENED */ else if (currentList.size() > lastList.size()) {
                Object addedObject = null;

                for (int i = 0; i < lastList.size(); i++)
                    currentList.remove(lastList.get(i));

                addedObject = currentList.get(0);

                if (addedObject.getClass().getName().substring(0
                        , 11).equals("_proxyClass")) {

                    if (!currentInstanceSessionName.equals(this.getCurrentSession().getDatabaseName())) {
                        NDebugOutputHandler.getDefault().handle(65);
                        return;
                    }

                }


                NSchemeInsert schemeInsert = new NSchemeInsert();
                schemeInsert.insertLater(this.entityUpdateHandlerProperty.getInstanceSqlName(), null,
                        addedObject,
                        false);


                NJoinTableInsert joinTableInsert = new NJoinTableInsert(schemeInsert.getTable(),
                        this.entityUpdateHandlerProperty.getModifiedField(),
                        this.entityUpdateHandlerProperty.getFieldAnnotation());


                joinTableInsert.setPrimaryKeySearch(GeneralToolKit.isAnnotationPresent(currentInstance.getClass().getSuperclass(),
                        PrimaryKey.class));
                joinTableInsert.setCurrentObjectInstance(currentInstance);

                joinTableInsert.insert(addedObject,
                        GeneralToolKit.getDefaultRandomGenerator().nextInt(100000000));


                schemeInsert.write();


            }
        }

        NSchemeSelect remotedObject = new NSchemeSelect();
        this.getCurrentSession().setCurrentSyncingObject(
                remotedObject.getLater(currentInstance.getClass().getSuperclass(),
                        this.entityUpdateHandlerProperty.getInstanceSqlName(),
                        this.entityUpdateHandlerProperty.getInstancePrimaryKeyValue()));
    }


}
