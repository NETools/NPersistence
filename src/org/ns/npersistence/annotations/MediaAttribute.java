package org.ns.npersistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MediaAttribute {
    public String getterName() default "$x0";

    public String setterName() default "$x0";

    public MediaType mediaType();

    enum MediaType {
        Image,
        File
    }

}


