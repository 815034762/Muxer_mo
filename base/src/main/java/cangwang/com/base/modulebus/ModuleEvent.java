package cangwang.com.base.modulebus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zjl on 16/11/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ModuleEvent {
    Class<?> coreClientClass();
    ThreadMode threadMode() default ThreadMode.POSTING;
}
