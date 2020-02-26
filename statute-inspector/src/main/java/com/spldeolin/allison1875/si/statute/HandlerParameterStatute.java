package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.MethodQualifiers;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * h33j
 *
 * @author Deolin 2020-02-26
 */
@Log4j2
public class HandlerParameterStatute implements Statute {

    private static final JsonSchemaGenerator jsg = new JsonSchemaGenerator(new ObjectMapper());

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

            try {
                ResolvedType rt = parameter.getType().resolve();
                ResolvedReferenceType rrt = rt.asReferenceType();

                // 可以是Collection<Req>或是Collection的派生类
                if (isOrLike(rrt)) {
                    rrt = rrt.getTypeParametersMap().get(0).b.asReferenceType();
                }

                // 可以是一个value-like的类型
                if (generateSchema(rrt.getId()).isValueTypeSchema()) {
                    log.info("The only parameter[{}] of handler[{}] is value-like, ingore.", parameter,
                            dto.getQualifier());
                    return;
                }

                // 必须以Req结尾
                if (!rrt.getId().endsWith("Req")) {
                    result.add(dto.setMessage("参数[" + parameter + "]的POJO命名必须以Req结尾"));
                }

            } catch (Exception e) {
                log.warn("The paramater[{}] caused.", parameter);
                result.add(dto.setMessage("参数[" + parameter + "]的类型不合规，具体原因需要review者检查"));
            }

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
