package org.ns.npersistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Bezeichnet ein atomares Attribut
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtomicAttribute {
	public String getterName() default "$x0";
	public String setterName() default "$x0";
}
