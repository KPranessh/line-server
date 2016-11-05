package com.salsify.data;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Target(value={FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp="^[0-9]+$")
@NotNull
public @interface LineId {
		
	String message() default "Invalid Line ID!";
	
	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
