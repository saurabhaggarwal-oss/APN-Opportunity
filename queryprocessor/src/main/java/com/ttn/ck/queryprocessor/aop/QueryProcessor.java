package com.ttn.ck.queryprocessor.aop;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface QueryProcessor {
    boolean ignore() default false;

}