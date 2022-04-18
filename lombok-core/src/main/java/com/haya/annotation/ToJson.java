package com.haya.annotation;

import java.lang.annotation.*;
import java.lang.annotation.Target;

//@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ToJson {
}
