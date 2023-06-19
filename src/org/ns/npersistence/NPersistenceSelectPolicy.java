package org.ns.npersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Klasse um bestimmte Verhaltensmuster beim Selektieren festzulegen und NSQL-spezifische Daten abzufangen.
 */
class NPersistenceSelectPolicy {

    private static HashMap<Class<?>, ArrayList<Object>> currentlyRequestedClassInstances;
    private static HashMap<Class<?>, HashSet<Object>> currentlyRequestedInstancePrimaryKeys;
    private static HashMap<Integer, Class<?>> currentlyRequestedClassIndices;
    private static int INDICES;

    private static boolean detached;
    private static boolean required;


    public static boolean isDetached() {
        return detached;
    }

    public static void setDetached(boolean detached) {
        NPersistenceSelectPolicy.detached = detached;
    }

    public static boolean isRequired() {
        return required;
    }

    public static void setRequired(boolean required) {
        NPersistenceSelectPolicy.required = required;
    }

    public static void initialize() {
        if (currentlyRequestedClassInstances != null)
            currentlyRequestedClassInstances.clear();
        if (currentlyRequestedInstancePrimaryKeys != null)
            currentlyRequestedInstancePrimaryKeys.clear();
        if (currentlyRequestedClassIndices != null)
            currentlyRequestedClassIndices.clear();

        currentlyRequestedClassInstances = new HashMap<>();
        currentlyRequestedInstancePrimaryKeys = new HashMap<>();
        currentlyRequestedClassIndices = new HashMap<>();

        NPersistenceSelectPolicy.INDICES = 0;
        NSQLCToken.IDENTIFIER_INDEX = 0;
        NSQLCIterableProvider.reset();
    }

    public static void addClass(Class<?> classDefinition) {
        if (!currentlyRequestedClassInstances.containsKey(classDefinition)) {
            currentlyRequestedClassInstances
                    .put(classDefinition, new ArrayList<>());
            currentlyRequestedInstancePrimaryKeys
                    .put(classDefinition, new HashSet<>());
            currentlyRequestedClassIndices.put(
                    ++INDICES, classDefinition);

        }
    }

    public static void addPrimaryKey(Class<?> currentClass, Object primaryKey) {
        if (currentlyRequestedInstancePrimaryKeys.containsKey(currentClass) &&
                !currentlyRequestedInstancePrimaryKeys.get(currentClass).contains(primaryKey)) {
            currentlyRequestedInstancePrimaryKeys.get(currentClass).add(primaryKey);
        }
    }

    public static void addObject(Class<?> currentClass, Object instance, Object primaryKey) {

        if (currentlyRequestedClassInstances == null) return;

        if (currentlyRequestedClassInstances.containsKey(currentClass) &&
                currentlyRequestedInstancePrimaryKeys.get(currentClass).contains(primaryKey) &&
                !currentlyRequestedClassInstances.get(currentClass).contains(instance)) {
            currentlyRequestedClassInstances.get(currentClass).add(instance);
        }
    }

    public static ArrayList<Object> get(Class<?> classDefinition) {
        return currentlyRequestedClassInstances.get(classDefinition);
    }


    public static HashMap<Class<?>, ArrayList<Object>> getResultSet() {
        return currentlyRequestedClassInstances;
    }

    public static HashMap<Integer, Class<?>> getIndices() {
        return currentlyRequestedClassIndices;
    }

    public static HashSet<Object> getPrimaryKeys(Class<?> classDefinition) {
        return currentlyRequestedInstancePrimaryKeys.get(classDefinition);
    }


}
