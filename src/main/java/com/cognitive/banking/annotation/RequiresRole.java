// src/main/java/com/cognitive/banking/annotation/RequiresRole.java
package com.cognitive.banking.annotation;

import com.cognitive.banking.domain.enums.UserRole;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {
    UserRole[] value();
}