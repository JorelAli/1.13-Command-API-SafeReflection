package io.github.jorelali.commandapi.safereflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(SafeReflections.class)
public @interface SafeReflection {

	/**
	 * An array of versions to check
	 */
	String[] versions();   //e.g. {"1.13", "1.13.1"}
	
	/**
	 * The target class where a method/field is declared
	 */
	Class<?> target();     //e.g. MinecraftServer.class
	
	/**
	 * The type to check, either ReflectionType.FIELD or ReflectionType.METHOD
	 */
	ReflectionType type(); //e.g. FIELD
	
	/**
	 * The name of the field/method 
	 */
	String name();         //e.g. "a"
	
	/**
	 * The return type for a method, or the type of the field
	 */
	Class<?> returnType(); //e.g. String.class
	
	/**
	 * Arguments for methods
	 */
	Class<?>[] methodArgs() default {};
	
}
