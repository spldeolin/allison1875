package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.reflect.Method;
import java.util.Collection;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class HandlerIterateProc {

    Collection<Method> listHandlers(Class<?> controllerClass) {
        Collection<Method> result = Lists.newArrayList();
        for (Method declaredMethod : controllerClass.getDeclaredMethods()) {
            if (this.isHandler(declaredMethod)) {
                result.add(declaredMethod);
            }
        }
        return result;
    }

    private boolean isHandler(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) != null;
    }

}
