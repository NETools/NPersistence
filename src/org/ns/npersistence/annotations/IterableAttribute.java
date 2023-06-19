package org.ns.npersistence.annotations;

import org.ns.npersistence.IterableConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bezeichnet ein iterables - also ein Listenattribut.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IterableAttribute {
	public IterableConstants mappingType();
}
