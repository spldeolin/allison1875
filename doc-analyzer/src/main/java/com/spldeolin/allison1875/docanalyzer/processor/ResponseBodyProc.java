package com.spldeolin.allison1875.docanalyzer.processor;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.handle.ObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析ResponseBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class ResponseBodyProc {

    private final JsonSchemaGenerator jsg;

    private final ObtainConcernedResponseBodyHandle obtainConcernedResponseBodyHandle;

    EnumSchemaProc enumSchemaProc = new EnumSchemaProc();

    ResponseBodyProc(JsonSchemaGenerator jsg, ObtainConcernedResponseBodyHandle obtainConcernedResponseBodyHandle) {
        this.jsg = jsg;
        this.obtainConcernedResponseBodyHandle = obtainConcernedResponseBodyHandle;
    }

    JsonSchema analyze(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        String responseBodyDescribe = null;
        try {
            ResolvedType responseBody = findResponseBody(controller, handler);
            if (responseBody != null) {
                responseBodyDescribe = responseBody.describe();
                JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(responseBodyDescribe, jsg);
                new ReferenceSchemaProc(jsonSchema).process();
                enumSchemaProc.process(jsonSchema);
                return jsonSchema;
            }
        } catch (JsonSchemaException ignore) {
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifiers.getTypeQualifierWithMethodName(handler), responseBodyDescribe, e);
        }
        return null;
    }

    /**
     * 1. controller上没有声明@RestController且handler上没有声明@ResponseBody时，认为没有ResponseBody
     * 2. 采用ConcernedResponseBodyTypeResolver提供的策略来获取ResponseBody
     * 3. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    private ResolvedType findResponseBody(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        try {
            if (Annotations.isAnnotationAbsent(controller, RestController.class) && Annotations
                    .isAnnotationAbsent(handler, ResponseBody.class)) {
                return null;
            }
            return obtainConcernedResponseBodyHandle.findConcernedResponseBodyType(handler);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

}
