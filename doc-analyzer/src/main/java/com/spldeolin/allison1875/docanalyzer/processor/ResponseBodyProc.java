package com.spldeolin.allison1875.docanalyzer.processor;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 解析ResponseBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Log4j2
public class ResponseBodyProc {

    @Inject
    private EnumSchemaProc enumSchemaProc;

    @Inject
    private ReferenceSchemaProc referenceSchemaProc;

    @Inject
    private GetBodyResolvedTypeProc getBodyResolvedTypeProc;


    public JsonSchema analyze(JsonSchemaGenerator jsg, ClassOrInterfaceDeclaration controller,
            MethodDeclaration handler) {
        String responseBodyDescribe = null;
        try {
            ResolvedType responseBody = getBodyResolvedTypeProc.getResponseBody(controller, handler);
            if (responseBody != null) {
                responseBodyDescribe = responseBody.describe();
                JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(responseBodyDescribe, jsg);
                referenceSchemaProc.process(jsonSchema);
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

}
