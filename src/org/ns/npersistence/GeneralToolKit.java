package org.ns.npersistence;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import org.ns.npersistence.annotations.CompositeAttribute;
import org.ns.npersistence.annotations.PrimaryKey;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.Random;

/**
 * Satz von Methoden für allgemeine Operationen
 */
class GeneralToolKit {

    private static Random random_generator;

    public static Random getDefaultRandomGenerator() {
        if (random_generator == null)
            random_generator = new Random();

        return random_generator;
    }

    /**
     * Prüft ob eine Klasse eine Methode mit dem Namen "methodenName" enthält
     *
     * @param ctClass
     * @param methodName
     * @return
     */
    static boolean containsMethodByName(CtClass ctClass, String methodName) {

        try {
            ctClass.getDeclaredMethod(methodName);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Holt den Wert des Feldes einer angegebenen Instanz
     *
     * @param currentInstance
     * @param fieldName
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    public static Object getFieldValue(Object currentInstance, String fieldName) throws IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException {

        Field currentField = currentInstance.getClass().getDeclaredField(fieldName);
        currentField.setAccessible(true);

        Object object = currentField.get(currentInstance);

        return object;

    }


    /**
     * Prüft ob in einer Klasse eine Annotation enthalten ist, falls ja, so wird das Feld zurückgegeben.
     *
     * @param ctClass
     * @param annotationClass
     * @return
     * @throws Exception
     */
    static SearchResult<Field> isAnnotationPresent(Class ctClass,
                                                   Class<? extends Annotation> annotationClass) throws Exception {

        SearchResult<Field> result = new SearchResult<>();

        if (ctClass != null && ctClass.getName().equals(Object.class.getName())) return result;

        Field[] ctFields = ctClass.getDeclaredFields();
        for (Field field : ctFields) {
            field.setAccessible(true);
            Object[] fieldAnnotations = field.getDeclaredAnnotations();
            for (Object ann : fieldAnnotations) {
                Annotation annotation = (Annotation) ann;
                if (annotation.annotationType().getName().equals(annotationClass.getName())) {
                    result.setField(field);
                    result.setPresent(true);
                    return result;
                }
            }
        }

        return isAnnotationPresent(ctClass.getSuperclass(), annotationClass);
    }

    /**
     * Fügt ein neues Feld zu einer Klasse zur Laufzeit hinzu.
     *
     * @param classPool
     * @param toClass
     * @param fieldName
     * @param fieldType
     * @throws Exception
     */
    static void addFieldToCtClass(ClassPool classPool, CtClass toClass, String fieldName, String fieldType) throws Exception {
        CtClass dataType = classPool.get(fieldType);
        CtField ctField = new CtField(dataType, fieldName, toClass);
        ctField.setModifiers(Modifier.PRIVATE);
        toClass.addField(ctField);
    }

    /**
     * Setzt den Wert des Feldes zur Laufzeit neu.
     *
     * @param currentInstance
     * @param fieldName
     * @param value
     * @throws Exception
     */
    static void setValue(Object currentInstance, String fieldName, Object value) throws Exception {
        Field field = currentInstance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(currentInstance, value);

    }


    // SOURCE: https://stackoverflow.com/questions/4895523/java-string-to-sha1/9071224#9071224
    static String sha1(String password) {
        String sha1 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return (password.contains("table_")) ? password : "table_" + sha1;
    }

    static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }


}
