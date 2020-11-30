package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class HandlerIterateProc {

    private final Class<?> controllerClass;

    HandlerIterateProc(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    void iterate(Consumer<Method> eachMethod) {
        Arrays.stream(controllerClass.getDeclaredMethods()).filter(this::isHandler).forEach(relectionMethod -> {
            try {
                eachMethod.accept(relectionMethod);
            } catch (Throwable t) {
                log.error("controller fail [{}]", relectionMethod.getName(), t);
            }
        });
    }

    private boolean isHandler(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) != null;
    }

}
