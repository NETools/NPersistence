package org.ns.npersistence;

import java.lang.annotation.Annotation;

/**
 * Stellt Methode für das Laden der Annotationswerte bereit
 */
class AnnotationToolkit {

    /**
     * Lädt den Wert der Annotation-Property
     * @param annotation
     * @param parameterName
     * @return
     */
    static Object getAnnotationValue(Annotation annotation, String parameterName) {
        try {
            return annotation.annotationType().getDeclaredMethod(parameterName).invoke(annotation, new Object[0]);
        } catch (Exception ex) {
            return null;
        }
    }
}
