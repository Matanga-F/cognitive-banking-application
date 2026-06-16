// src/main/java/com/cognitive/banking/annotation/RequiresPermission.java
package com.cognitive.banking.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    String value();
    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR
    }
}