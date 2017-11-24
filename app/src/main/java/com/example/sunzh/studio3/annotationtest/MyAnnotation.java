package com.example.sunzh.studio3.annotationtest;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sunzh on 2017/11/16.
 *
 * @author sunzh
 */
@Documented
@Retention(RetentionPolicy.CLASS)

public @interface MyAnnotation {
    String value() default "110";
}
