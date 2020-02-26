package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * h33j
 *
 * @author Deolin 2020-02-26
 */
@Log4j2
public class HandlerReturnStatute implements Statute {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(MethodDeclaration.class,
                method -> method.getAnnotationByName("PostMapping").isPresent()).forEach(handler -> {
            LawlessDto dto = new LawlessDto(handler, MethodQualifiers.getTypeQualifierWithMethodName(handler));
            handler.findAll(ObjectCreationExpr.class, oce -> oce.getTypeAsString().equals("ResponseInfo"))
                    .forEach(oce -> {
                        if (oce.getArguments().size() == 0) {
                            return;
                        }
                        ResolvedType rt = null;
                        try {
                            rt = oce.getArgument(0).calculateResolvedType();

                            ResolvedReferenceType rrt = rt.asReferenceType();

                            // 可以是Collection<Pojo>或是Collection的派生类
                            if (isOrLike(rrt)) {
                                rrt = rrt.getTypeParametersMap().get(0).b.asReferenceType();
                            }

                            // 可以是一个value-like的类型
                            if (generateSchema(rrt.getId()).isValueTypeSchema()) {
                                return;
                            }

                            // 必须以Xxx结尾
                            if (!rrt.getId().endsWith("Resp")) {
                                result.add(dto.setMessage("返回[" + oce + "]的POJO命名必须以Req结尾"));
                            }

                        } catch (Exception e) {
                            log.warn("The return data type[{}] of handler [{}] caused.", rt, dto.getQualifier());
                            result.add(dto.setMessage("返回[" + oce + "]的类型不合规，具体原因需要review者检查"));
                        }

                    });
        }));

        return result;
    }

    private boolean isOrLike(ResolvedReferenceType rrt) {
        if (QualifierConstants.COLLECTION.equals(rrt.getId())) {
            return true;
        }
        return rrt.getAllAncestors().stream()
                .anyMatch(ancestor -> QualifierConstants.COLLECTION.equals(ancestor.getId()));
    }

    private JsonSchema generateSchema(String qualifierForClassLoader) throws JsonMappingException {
        JavaType javaType;
        javaType = new TypeFactory(null) {
            private static final long serialVersionUID = -8151903006798193420L;

            @Override
            public ClassLoader getClassLoader() {
                return WarOrFatJarClassLoaderFactory.getClassLoader();
            }
        }.constructFromCanonical(qualifierForClassLoader);
        return jsg.generateSchema(javaType);
    }

}
