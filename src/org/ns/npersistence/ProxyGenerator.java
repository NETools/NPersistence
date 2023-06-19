package org.ns.npersistence;

import javassist.*;
import org.ns.npersistence.annotations.*;

import java.lang.annotation.Annotation;


/**
 * Klasse zur Erstellung von Proxy-Klassen.
 * Proxy-Klassen sind Klassen, die attached-gekennzeichnete Objekte "wrappen" und Veränderungen auf diese Objekte synchronisieren.
 */
class ProxyGenerator {

    private static int PROXY_CLASS_INDEX;
    private ClassPool classPool;

    public ClassPool getClassPool() {
        return classPool;
    }

    public ProxyGenerator() {
        this.classPool = ClassPool.getDefault();
    }

    /**
     * Generiert eine neue Proxy-Klasse
     *
     * @param ofClass            Die Klasse die gewrappt werden soll
     * @param proxyClassProperty
     * @return
     * @throws Exception
     */
    public Class<?> getProxyClass(Class<?> ofClass, ProxyClassProperty proxyClassProperty) throws Exception {
        //String packageName = ofClass.getPackage().getName();

        CtClass proxyClass = this.classPool.makeClass("_proxyClass" + (PROXY_CLASS_INDEX++));
        CtClass superClass = this.classPool.get(ofClass.getName());


        proxyClass.setSuperclass(superClass);
        proxyClass.setModifiers(Modifier.PUBLIC);

        if (proxyClassProperty != null) {
            GeneralToolKit.addFieldToCtClass(this.getClassPool(), proxyClass, "sqlQualifiedName",
                    String.class.getName());
        }

        GeneralToolKit.addFieldToCtClass(this.getClassPool(), proxyClass, "sqlPrimaryKeyName",
                String.class.getName());
        GeneralToolKit.addFieldToCtClass(this.getClassPool(), proxyClass, "sqlPrimaryKeyValue",
                Object.class.getName());
        GeneralToolKit.addFieldToCtClass(this.getClassPool(), proxyClass, "sqlCurrentSessionName",
                String.class.getName());

        CtField[] fields = superClass.getDeclaredFields();

        for (CtField field : fields) {
            Object[] annotations = field.getAnnotations();

            for (Object o : annotations) {
                Annotation annotation = (Annotation) o;

                if (annotation.annotationType().equals(PrimaryKey.class) || annotation.annotationType().equals(AtomicAttribute.class) ||
                        annotation.annotationType().equals(CompositeAttribute.class) || annotation.annotationType().equals(MediaAttribute.class)) {
                    String getterName = (String) AnnotationToolkit.getAnnotationValue(annotation,
                            "getterName");
                    String setterName = (String) AnnotationToolkit.getAnnotationValue(annotation,
                            "setterName");

                    String fieldNameUpperCase =
                            field.getName().toUpperCase().charAt(0) + field.getName().substring(1);

                    getterName = getterName.replace("$x0", "get" + fieldNameUpperCase);
                    setterName = setterName.replace("$x0", "set" + fieldNameUpperCase);

                    if (!GeneralToolKit.containsMethodByName(superClass, setterName)) {
                        NDebugOutputHandler.getDefault().handle(81,
                                "FAILED TO LOAD " + ConsoleConstants.YELLOW + setterName +
                                        ConsoleConstants.RED_BRIGHT + " FOR FIELD " + ConsoleConstants.YELLOW + field.getName() + ConsoleConstants.RED_BRIGHT +
                                        " IN CLASS " + ConsoleConstants.YELLOW + superClass.getName());

                        continue;
                    }

                    if (!GeneralToolKit.containsMethodByName(superClass, getterName)) {
                        NDebugOutputHandler.getDefault().handle(81,
                                "FAILED TO LOAD " + ConsoleConstants.YELLOW + setterName +
                                        ConsoleConstants.RED_BRIGHT + " FOR FIELD " + ConsoleConstants.YELLOW + field.getName() + ConsoleConstants.RED_BRIGHT +
                                        " IN CLASS " + ConsoleConstants.YELLOW + superClass.getName());
                        continue;
                    }

                    CtMethod generateGetter = generateGetter(proxyClass, field.getName(),
                            field.getType().getName(),
                            getterName);
                    CtMethod generateSetter = generateSetter(proxyClass, field.getName(),
                            field.getType().getName(),
                            setterName);

                    proxyClass.addMethod(generateGetter);
                    proxyClass.addMethod(generateSetter);
                } else if (annotation.annotationType().equals(IterableAttribute.class)) {

                    String fieldName
                            = field.getName();
                    String fieldSignature =
                            field.getGenericSignature().substring(field.getGenericSignature().indexOf("<L")).replace(
                                    "/", ".").replace(";", "").replace("<L", "").replace("<", "").replace(
                                    ">", "");

                    CtField stackField =
                            generateRecycleStack(proxyClass, "__recycle__" + fieldName, fieldSignature);
                    proxyClass.addField(stackField);
                }
                
                /* else if (annotation.annotationType().equals(IterableAttribute.class)) {
                    String addName = (String) AnnotationToolkit.getAnnotationValue(annotation,
                            "addMethodName");
                    String removeName = (String) AnnotationToolkit.getAnnotationValue(annotation,
                            "removeMethodName");


                    String fieldSignature =
                            field.getGenericSignature().substring(field.getGenericSignature().indexOf("<L")).replace(
                                    "/", ".").replace(";", "").replace("<L", "").replace("<", "").replace(
                                    ">", "");


                    CtMethod generateAdder = generateSetter(proxyClass, field.getName(), fieldSignature,
                            addName);
                    CtMethod generateRemover = generateSetter(proxyClass, field.getName(), fieldSignature,
                            removeName);

                    proxyClass.addMethod(generateAdder);
                    proxyClass.addMethod(generateRemover);
                }
                 */


            }
        }

        return proxyClass.toClass();
    }

    /**
     * Gibt eine Instanz einer neu erstellten Proxy-Klasse zurück.
     *
     * @param ofClass
     * @param proxyClassProperty
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T getNewInstance(Class<?> ofClass, ProxyClassProperty proxyClassProperty) throws Exception {
        T instance =
                (T) this.getProxyClass(ofClass, proxyClassProperty).getDeclaredConstructor().newInstance();
        if (proxyClassProperty != null)
            GeneralToolKit.setValue(instance, "sqlQualifiedName", proxyClassProperty.getSqlName());
        return instance;
    }


    /**
     * Generiert einen neuen getter() und injiziert Synchronisationscode.
     *
     * @param forClass
     * @param fieldName
     * @param fieldType
     * @param getterName
     * @return
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    protected CtMethod generateGetter(CtClass forClass, String fieldName, String fieldType,
                                      String getterName) throws CannotCompileException, NotFoundException {
        StringBuilder sourceBuilder = new StringBuilder();

        sourceBuilder =
                sourceBuilder
                        .append("public")
                        .append(" ")
                        .append(fieldType)
                        .append(" ")
                        .append(getterName)
                        .append("(")
                        .append("")
                        .append(")")
                        .append("{\n")
                        .append("org.ns.npersistence.NEntityUpdateHandler.getDefault()" +
                                ".checkRetrieveConsistency(this, \"" + fieldName + "\");\n")
                        .append("return super." + getterName + "();" + "\n")
                        .append("}");


        return CtMethod.make(sourceBuilder.toString(), forClass);
    }

    /**
     * Generiert einen neuen setter() und injiziert Synchronisationscode.
     *
     * @param forClass
     * @param fieldName
     * @param fieldType
     * @param setterName
     * @return
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    protected CtMethod generateSetter(CtClass forClass, String fieldName, String fieldType,
                                      String setterName) throws CannotCompileException, NotFoundException {
        StringBuilder sourceBuilder = new StringBuilder();


        sourceBuilder =
                sourceBuilder
                        .append("public")
                        .append(" ")
                        .append("void")
                        .append(" ")
                        .append(setterName)
                        .append("(")
                        .append(fieldType + " data")
                        .append(")")
                        .append("{\n")
                        .append("org.ns.npersistence.NEntityUpdateHandler.getDefault()" +
                                ".beginUpdate(this, \"" + fieldName + "\");\n")
                        .append("super." + setterName + "(data);" + "\n")
                        .append("org.ns.npersistence.NEntityUpdateHandler.getDefault().endUpdate" +
                                "(this, \"" + fieldName + "\");\n")
                        .append("}");
        return CtMethod.make(sourceBuilder.toString(), forClass);
    }

    protected CtField generateRecycleStack(CtClass forClass, String stackName, String fieldType) throws CannotCompileException,
            NotFoundException {

        CtClass stackClass = ClassPool.getDefault().get("java.util.Stack");
        CtField stackField = new CtField(stackClass, stackName, forClass);
        stackField.setGenericSignature(fieldType);
        stackField.setModifiers(Modifier.PUBLIC);


        return stackField;
    }


}





