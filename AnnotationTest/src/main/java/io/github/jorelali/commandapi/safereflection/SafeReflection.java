package io.github.jorelali.commandapi.safereflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(SafeReflections.class)
public @interface SafeReflection {

	String[] versions();
	Class<?> target();
	String field() default "";
	String method() default "";
	
}
