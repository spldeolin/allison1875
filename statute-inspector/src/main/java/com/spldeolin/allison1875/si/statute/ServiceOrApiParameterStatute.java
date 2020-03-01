package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.JsonSchemas;
import com.spldeolin.allison1875.base.util.Strings;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.ast.ResolvedTypes;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-26
 */
@Log4j2
public class ServiceOrApiParameterStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(ClassOrInterfaceDeclaration.class, this::isServiceOrApi).forEach(service -> {
            service.findAll(MethodDeclaration.class).forEach(method -> {
                NodeList<Parameter> parameters = method.getParameters();
                if (parameters.size() == 0) {
                    return;
                }

                // 只能有1个参数
                if (parameters.size() > 1) {
                    result.add(new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                            .setMessage("service/api方法最多只能有1个参数，当前" + parameters.size() + "个"));
                    return;
                }
                Parameter parameter = Iterables.getOnlyElement(parameters);

                // 类型检查
                Type type = parameter.getType();
                if (type.isPrimitiveType()) {
                    return;
                }
                ResolvedReferenceType rrt;
                try {
                    rrt = type.resolve().asReferenceType();

                    // 可以是Collection或是Collection的派生类
                    if (ResolvedTypes.isOrLike(rrt, QualifierConstants.COLLECTION)) {
                        rrt = rrt.getTypeParametersMap().get(0).b.asReferenceType();
                    }

                    // 可以是Map、Multimap或是两者的派生类
                    if (ResolvedTypes.isOrLike(rrt, QualifierConstants.MAP, QualifierConstants.MULTIPART_FILE)) {
                        ResolvedReferenceType key = rrt.getTypeParametersMap().get(0).b.asReferenceType();
                        if (!JsonSchemas.generateSchema(key.getId()).isValueTypeSchema()) {
                            result.add(new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                                    .setMessage("映射的Key类型必须是value-like"));
                            return;
                        }
                        rrt = rrt.getTypeParametersMap().get(1).b.asReferenceType();
                    }

                    // 可以是一个value-like的类型
                    if (JsonSchemas.generateSchema(rrt.getId()).isValueTypeSchema()) {
                        return;
                    }

                    // 命名前缀和后缀
                    boolean illegalNaming = false;
                    String pojoName = Iterables.getLast(Lists.newArrayList(rrt.getId().split("\\.")));
                    if (!pojoName.endsWith("Ao")) {
                        result.add(new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                                .setMessage("参数[" + parameter + "]的POJO命名必须以Ao结尾"));
                        illegalNaming = true;
                    }
                    if (!Strings.capture(pojoName).substring(0, pojoName.length() - "Ao".length() - 1)
                            .equals(method.getNameAsString())) {
                        result.add(new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                                .setMessage("参数[" + parameter + "]的POJO命名的Ao以外部分必须与方法名一致"));
                        illegalNaming = true;
                    }
                    if (illegalNaming) {
                        return;
                    }

                    // 禁止作为任何field类型和任何方法的返回类型
                    cus.forEach(cux -> {
                        cux.findAll(FieldDeclaration.class).forEach(field -> field.findAll(Type.class).forEach(t -> {
                            if (t.toString().contains(pojoName)) {
                                result.add(new LawlessDto(field).setMessage("[" + pojoName + "]禁止作为Field的类型"));
                            }
                        }));
                        cux.findAll(MethodDeclaration.class).forEach(m -> {
                            if (m.getType().toString().contains(pojoName)) {
                                result.add(new LawlessDto(m, MethodQualifiers.getTypeQualifierWithMethodName(m))
                                        .setMessage("[" + pojoName + "]禁止作为方法的返回类型"));
                            }
                        });
                    });
                } catch (Exception e) {
                    log.warn("The paramater[{}] of [{}] caused.", parameter,
                            MethodQualifiers.getTypeQualifierWithMethodName(method));
                    result.add(new LawlessDto(method, MethodQualifiers.getTypeQualifierWithMethodName(method))
                            .setMessage("参数[" + parameter + "]的类型不合规，具体联系review者"));
                }

            });
        }));

        return result;
    }

    private boolean isServiceOrApi(ClassOrInterfaceDeclaration coid) {
        String name = coid.getNameAsString();
        return coid.isInterface() && (name.endsWith("Service") || name.endsWith("Api"));
    }

}
