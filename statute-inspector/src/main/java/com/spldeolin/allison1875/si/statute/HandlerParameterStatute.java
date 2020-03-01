package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
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
 * h33j
 *
 * @author Deolin 2020-02-26
 */
@Log4j2
public class HandlerParameterStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessDto> result = Lists.newLinkedList();
        cus.forEach(cu -> cu.findAll(MethodDeclaration.class,
                method -> method.getAnnotationByName("PostMapping").isPresent()).forEach(handler -> {
            NodeList<Parameter> parameters = handler.getParameters();
            if (parameters.size() == 0) {
                return;
            }
            LawlessDto dto = new LawlessDto(handler, MethodQualifiers.getTypeQualifierWithMethodName(handler));

            // 只能有1个参数
            if (parameters.size() > 1) {
                result.add(dto.setMessage("handler最多只能有1个参数，当前" + parameters.size() + "个"));
                return;
            }
            Parameter parameter = Iterables.getOnlyElement(parameters);

            // 必须是@RequestBody
            if (!parameter.getAnnotationByName("RequestBody").isPresent()) {
                result.add(dto.setMessage("唯一的那个参数必须是@RequestBody"));
                return;
            }

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

                // 可以是一个value-like的类型
                if (JsonSchemas.generateSchema(rrt.getId()).isValueTypeSchema()) {
                    return;
                }

                // 命名前缀和后缀
                boolean illegalNaming = false;
                String pojoName = Iterables.getLast(Lists.newArrayList(rrt.getId().split("\\.")));
                if (!pojoName.endsWith("Req")) {
                    result.add(dto.setMessage("参数[" + parameter + "]的POJO命名必须以Req结尾"));
                    illegalNaming = true;
                }
                if (!Strings.capture(pojoName).substring(0, pojoName.length() - "Req".length() - 1)
                        .equals(handler.getNameAsString())) {
                    result.add(dto.setMessage("参数[" + parameter + "]的POJO命名的Req以外部分必须与方法名一致"));
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
                log.warn("The paramater[{}] of handler[{}] caused.", parameter, dto.getQualifier());
                result.add(dto.setMessage("参数[" + parameter + "]的类型不合规，具体联系review者"));
            }

        }));


        return result;
    }

}
