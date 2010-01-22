package org.confgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Conf {
	String key();
	
	String def() default "";
	String local() default "";
	String dev() default "";
	String qa() default "";
	String prod() default "";
}
