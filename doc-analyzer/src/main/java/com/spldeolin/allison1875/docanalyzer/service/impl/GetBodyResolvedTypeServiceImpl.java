package com.spldeolin.allison1875.docanalyzer.service.impl;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.util.ast.Annotations;
import com.spldeolin.allison1875.common.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.service.GetBodyResolvedTypeService;
import com.spldeolin.allison1875.docanalyzer.service.ObtainConcernedResponseBodyService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-18
 */
@Singleton
@Log4j2
public class GetBodyResolvedTypeServiceImpl implements GetBodyResolvedTypeService {

    @Inject
    private ObtainConcernedResponseBodyService obtainConcernedResponseBodyService;

    /**
     * 1. 遍历出声明了@RequestBody的参数后返回
     * 2. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    @Override
    public ResolvedType getRequestBody(MethodDeclaration handler) {
        ResolvedType result = null;

        String name = MethodQualifiers.getTypeQualifierWithMethodName(handler);
        for (Parameter parameter : handler.getParameters()) {
            try {
                boolean isRequestBody = false;
                for (AnnotationExpr annotation : parameter.getAnnotations()) {
                    if (ImportConstant.SPRING_REQUEST_BODY.getNameAsString()
                            .equals(annotation.resolve().getQualifiedName())) {
                        if (result == null) {
                            result = parameter.getType().resolve();
                            isRequestBody = true;
                            break;
                        } else {
                            log.warn("方法[{}]存在不止一个RequestBody", name);
                        }
                    }
                }

                if (!isRequestBody) {
                    log.warn("方法[{}]存在RequestBody以外的参数[{}]，忽略", name, parameter);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        if (result == null) {
            log.info("方法[{}]没有RequestBody", name);
        }
        return result;
    }

    /**
     * 1. controller上没有声明@RestController且handler上没有声明@ResponseBody时，认为没有ResponseBody
     * 2. 采用ConcernedResponseBodyTypeResolver提供的策略来获取ResponseBody
     * 3. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    @Override
    public ResolvedType getResponseBody(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        try {
            if (Annotations.isAnnotationAbsent(controller, RestController.class) && Annotations.isAnnotationAbsent(
                    handler, ResponseBody.class)) {
                return null;
            }
            return obtainConcernedResponseBodyService.findConcernedResponseBodyType(handler);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

}