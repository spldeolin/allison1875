package com.spldeolin.allison1875.docanalyzer.processor;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析RequestBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
public class RequestBodyProc {

    EnumSchemaProc enumSchemaProc = new EnumSchemaProc();

    public JsonSchema analyze(JsonSchemaGenerator jsg, MethodDeclaration handler) {
        String requestBodyDescribe = null;
        try {
            ResolvedType requestBody = findRequestBody(handler);
            if (requestBody != null) {
                requestBodyDescribe = requestBody.describe();
                JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(requestBodyDescribe, jsg);
                new ReferenceSchemaProc(jsonSchema).process();
                enumSchemaProc.process(jsonSchema);
                return jsonSchema;
            }
        } catch (JsonSchemaException ignore) {
        } catch (Exception e) {
            log.error("BodySituation.FAIL method={} describe={}",
                    MethodQualifiers.getTypeQualifierWithMethodName(handler), requestBodyDescribe, e);
        }
        return null;
    }

    /**
     * 1. 遍历出声明了@RequestBody的参数后返回
     * 2. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    private ResolvedType findRequestBody(MethodDeclaration method) {
        ResolvedType result = null;

        String name = MethodQualifiers.getTypeQualifierWithMethodName(method);
        for (Parameter parameter : method.getParameters()) {
            try {
                boolean isRequestBody = false;
                for (AnnotationExpr annotation : parameter.getAnnotations()) {
                    if (QualifierConstants.REQUEST_BODY.equals(annotation.resolve().getQualifiedName())) {
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

}
