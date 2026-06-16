// src/main/java/com/cognitive/banking/annotation/RateLimit.java
package com.cognitive.banking.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int capacity() default 10;      // max tokens
    int refillTokens() default 5;   // tokens added per refill interval
    int refillMinutes() default 1;  // refill interval in minutes
    String key() default "";        // optional SpEL expression for custom key (e.g., "#username")
}