package com.spldeolin.allison1875.handlertransformer.processor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.io.Files;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class EnsureNoRepeatProc {

    /**
     * 确保参数firstLineDto中的handlerName与controller中所有的controller均不重名
     */
    public void inController(ClassOrInterfaceDeclaration controller, FirstLineDto firstLineDto) {
        String handlerName = firstLineDto.getHandlerName();
        if (controller.getMethodsByName(handlerName).size() > 0) {
            String newHandlerName = handlerName + "Ex";
            firstLineDto.setHandlerName(newHandlerName);
            firstLineDto.setHandlerUrl(firstLineDto.getHandlerUrl() + "Ex");
            log.warn("Handler name [{}] is conflicted in Controller [{}], rename to [{}].", handlerName,
                    controller.getNameAsString(), newHandlerName);
            inController(controller, firstLineDto);
        }
    }

    /**
     * 确保参数methodName与service中所有的方法均不重名
     */
    public String inService(ClassOrInterfaceDeclaration service, String methodName) {
        if (service.getMethodsByName(methodName).size() > 0) {
            String newMethodName = methodName + "Ex";
            log.warn("Method name [{}] is conflicted in Service [{}], rename to [{}].", methodName,
                    service.getNameAsString(), newMethodName);
            return inService(service, newMethodName);
        }
        return methodName;
    }

    /**
     * 确保参数coidName与AST森林中所有的java文件名均不重名
     */
    public String inAstForest(AstForest astForest, String coidName) {
        boolean conflicting = astForest.getJavasInForest().stream()
                .anyMatch(java -> Files.getNameWithoutExtension(java.toFile().getName()).equals(coidName));
        if (conflicting) {
            return inAstForest(astForest, concatEx(coidName));
        } else {
            return coidName;
        }
    }

    private String concatEx(String coidName) {
        String newName;
        if (coidName.endsWith("ReqDto")) {
            newName = MoreStringUtils.replaceLast(coidName, "ReqDto", "ExReqDto");
        } else if (coidName.endsWith("RespDto")) {
            newName = MoreStringUtils.replaceLast(coidName, "RespDto", "ExRespDto");
        } else if (coidName.endsWith("Dto")) {
            newName = MoreStringUtils.replaceLast(coidName, "Dto", "ExDto");
        } else if (coidName.endsWith("Service")) {
            newName = MoreStringUtils.replaceLast(coidName, "Service", "ExService");
        } else if (coidName.endsWith("ServiceImpl")) {
            newName = MoreStringUtils.replaceLast(coidName, "ServiceImpl", "ExServiceImpl");
        } else {
            newName = coidName + "Ex";
        }
        return newName;
    }

}