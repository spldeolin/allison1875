package com.spldeolin.allison1875.si.statute;

import static com.spldeolin.allison1875.si.StatuteInspectorConfig.CONFIG;

import java.util.Collection;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.JsonSchemas;
import com.spldeolin.allison1875.base.util.Strings;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.ast.ResolvedTypes;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * h33j
 *
 * @author Deolin 2020-02-26
 */
@Log4j2
public class HandlerReturnStatute implements Statute {

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
                            if (rt.isPrimitive()) {
                                return;
                            }

                            ResolvedReferenceType rrt = rt.asReferenceType();

                            // 可以是Collection<Pojo>或是Collection的派生类
                            if (ResolvedTypes.isOrLike(rrt, QualifierConstants.COLLECTION,
                                    CONFIG.getCommonPageTypeQualifier())) {
                                rrt = rrt.getTypeParametersMap().get(0).b.asReferenceType();
                            }

                            // 可以是一个value-like的类型
                            if (JsonSchemas.generateSchema(rrt.getId()).isValueTypeSchema()) {
                                return;
                            }

                            // 命名前缀和后缀
                            boolean illegalNaming = false;
                            String pojoName = Iterables.getLast(Lists.newArrayList(rrt.getId().split("\\.")));
                            if (!pojoName.endsWith("Req")) {
                                result.add(dto.setMessage("ResponseInfo.data[" + oce + "]的POJO命名必须以Resp结尾"));
                                illegalNaming = true;
                            }
                            if (!Strings.capture(pojoName).substring(0, pojoName.length() - "Req".length() - 1)
                                    .equals(handler.getNameAsString())) {
                                result.add(dto.setMessage("ResponseInfo.data[" + oce + "]的POJO命名的Resp以外部分必须与方法名一致"));
                                illegalNaming = true;
                            }
                            if (illegalNaming) {
                                return;
                            }

                            // 禁止作为任何field类型和任何方法的参数类型
                            cus.forEach(cux -> {
                                cux.findAll(FieldDeclaration.class)
                                        .forEach(field -> field.findAll(Type.class).forEach(type -> {
                                            if (type.toString().contains(pojoName)) {
                                                result.add(new LawlessDto(field)
                                                        .setMessage("[" + pojoName + "]禁止作为Field的类型"));
                                            }
                                        }));
                                cux.findAll(MethodDeclaration.class).forEach(method -> {
                                    method.getParameters().forEach(param -> {
                                        if (param.getType().toString().contains(pojoName)) {
                                            result.add(new LawlessDto(method,
                                                    MethodQualifiers.getTypeQualifierWithMethodName(method))
                                                    .setMessage("[" + pojoName + "]禁止作为方法的参数类型"));
                                        }
                                    });
                                });
                            });

                        } catch (Exception e) {
                            log.warn("The return data type[{}] of handler [{}] caused.", rt, dto.getQualifier());
                            result.add(dto.setMessage("返回[" + oce + "]的类型不合规，具体联系review者"));
                        }

                    });
        }));

        return result;
    }

}
