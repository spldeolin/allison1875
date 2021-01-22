package com.spldeolin.allison1875.handlertransformer.processor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class EnsureNoRepeationProc {

    public void ensureNoRepeation(ClassOrInterfaceDeclaration controller, FirstLineDto firstLineDto) {
        String handlerName = firstLineDto.getHandlerName();
        if (controller.getMethodsByName(handlerName).size() > 0) {
            String newHandlerName = handlerName + "Ex";
            firstLineDto.setHandlerName(newHandlerName);
            firstLineDto.setHandlerUrl(firstLineDto.getHandlerUrl() + "Ex");
            log.warn("Handler name [{}] is conflicted in Controller [{}], rename to [{}].", handlerName,
                    controller.getNameAsString(), newHandlerName);
            ensureNoRepeation(controller, firstLineDto);
        }
    }

}