package com.spldeolin.allison1875.da;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.IdUtils;
import com.spldeolin.allison1875.base.util.JsonSchemaUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.da.builder.EndpointDtoBuilder;
import com.spldeolin.allison1875.da.dto.EndpointDto;
import com.spldeolin.allison1875.da.dto.EnumDto;
import com.spldeolin.allison1875.da.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.da.dto.PropertiesContainerDto;
import com.spldeolin.allison1875.da.dto.PropertyDto;
import com.spldeolin.allison1875.da.dto.PropertyTreeNodeDto;
import com.spldeolin.allison1875.da.enums.BodySituationEnum;
import com.spldeolin.allison1875.da.enums.JsonTypeEnum;
import com.spldeolin.allison1875.da.markdown.MarkdownConverter;
import com.spldeolin.allison1875.da.processor.ControllerIterateProcessor;
import com.spldeolin.allison1875.da.processor.JsonSchemaGeneratorProcessor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-01
 */
@Log4j2
public class DocAnanlyzerBoot {

    public static void main(String[] args) {
        new DocAnanlyzerBoot().process();
    }

    private static final PathMatcher pathMatcher = new AntPathMatcher();

    public void process() {
        AstForest astForest = AstForest.getInstance();

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需
        JsonSchemaGeneratorProcessor jsgProcessor = new JsonSchemaGeneratorProcessor(astForest);
        JsonSchemaGenerator jsg = jsgProcessor.analyzeAstAndBuildJsg();

        // 再次重头遍历astForest，并遍历每个cu下的每个controller（是否是controller由Processor判断）
        ControllerIterateProcessor controllerIterateProcessor = new ControllerIterateProcessor(astForest.reset());
        controllerIterateProcessor.iterate(controller -> {

            // 反射controller，如果失败那么这个controller就没有处理的必要了
            Class<?> controllerClass;
            try {
                controllerClass = tryReflectController(controller, astForest);
            } catch (ClassNotFoundException e) {
                return;
            }

            EndpointDtoBuilder builder = new EndpointDtoBuilder();

            builder.groupNames(findGroupNames(controller));

            RequestMapping controllerRequestMapping = findRequestMappingAnnoOrElseNull(controllerClass);
            String[] cPaths = findValueFromAnno(controllerRequestMapping);
            RequestMethod[] cVerbs = findVerbFromAnno(controllerRequestMapping);

            Map<String, MethodDeclaration> methods = Maps.newHashMap();
            for (MethodDeclaration method : controller.findAll(MethodDeclaration.class)) {
                methods.put(MethodQualifiers.getShortestQualifiedSignature(method), method);
            }

            for (Method reflectionMethod : controllerClass.getDeclaredMethods()) {
                if (isNotHandler(reflectionMethod)) {
                    continue;
                }

                RequestMapping methodRequestMapping = findRequestMappingAnnoOrElseNull(reflectionMethod);
                String[] mPaths = methodRequestMapping.value();
                RequestMethod[] mVerbs = methodRequestMapping.method();

                builder.combinedUrls(combineUrl(cPaths, mPaths));
                builder.combinedVerbs(combineVerb(cVerbs, mVerbs));

                MethodDeclaration handler = methods
                        .get(MethodQualifiers.getShortestQualifiedSignature(reflectionMethod));
                if (handler == null) {
                    // 可能是源码删除了某个handler但未编译，所以reflectionMethod还存在，但MethodDeclaration已经不存在了，忽略即可
                    continue;
                }

                builder.description(StringUtils.limitLength(Javadocs.extractEveryLine(handler, "\n"), 4096));
                builder.version("");
                builder.isDeprecated(isDeprecated(controller, handler));
                builder.author(Authors.getAuthorOrElseEmpty(handler));
                builder.sourceCode(Locations.getRelativePathWithLineNo(handler));

                BodySituationEnum requestBodySituation;
                String requestBodyDescribe = null;
                try {
                    ResolvedType requestBody = findRequestBody(handler);
                    if (requestBody != null) {
                        requestBodyDescribe = requestBody.describe();
                        JsonSchema jsonSchema = JsonSchemaUtils
                                .generateSchema(requestBodyDescribe, astForest.getCurrentClassLoader(), jsg);

                        if (jsonSchema.isObjectSchema()) {
                            requestBodySituation = BodySituationEnum.KEY_VALUE;
                            PropertiesContainerDto propContainer = anaylzeObjectSchema(requestBodyDescribe,
                                    jsonSchema.asObjectSchema());
                            builder.flatRequestProperties(propContainer.getFlatProperties());
                        } else if (fieldsAbsent(requestBody)) {
                            requestBodySituation = BodySituationEnum.NONE;
                        } else {
                            requestBodySituation = BodySituationEnum.CHAOS;
                            builder.requestBodyJsonSchema(JsonUtils.beautify(jsonSchema));
                        }
                    } else {
                        requestBodySituation = BodySituationEnum.NONE;
                    }
                } catch (JsonSchemaException ignore) {
                    requestBodySituation = BodySituationEnum.FAIL;
                } catch (Exception e) {
                    log.error("BodySituation.FAIL method={} describe={}",
                            MethodQualifiers.getTypeQualifierWithMethodName(handler), requestBodyDescribe, e);
                    requestBodySituation = BodySituationEnum.FAIL;
                }
                builder.requestBodySituation(requestBodySituation);

                BodySituationEnum responseBodySituation;
                String responseBodyDescribe = null;
                try {
                    ResolvedType responseBody = findResponseBody(controller, handler);
                    if (responseBody != null) {
                        responseBodyDescribe = responseBody.describe();
                        JsonSchema jsonSchema = JsonSchemaUtils
                                .generateSchema(responseBodyDescribe, astForest.getCurrentClassLoader(), jsg);

                        if (jsonSchema.isArraySchema()) {
                            Items items = jsonSchema.asArraySchema().getItems();
                            if (items != null && items.isSingleItems() && items.asSingleItems().getSchema()
                                    .isObjectSchema()) {
                                responseBodySituation = BodySituationEnum.KEY_VALUE_ARRAY;
                                PropertiesContainerDto propContainer = anaylzeObjectSchema(responseBodyDescribe,
                                        items.asSingleItems().getSchema().asObjectSchema());
                                clearAllValidatorAndNullableFlag(propContainer);
                                builder.flatResponseProperties(propContainer.getFlatProperties());
                            } else {
                                responseBodySituation = BodySituationEnum.CHAOS;
                                builder.responseBodyJsonSchema(JsonUtils.beautify(jsonSchema));
                            }
                        } else if (jsonSchema.isObjectSchema()) {
                            responseBodySituation = BodySituationEnum.KEY_VALUE;
                            PropertiesContainerDto propContainer = anaylzeObjectSchema(responseBodyDescribe,
                                    jsonSchema.asObjectSchema());
                            clearAllValidatorAndNullableFlag(propContainer);
                            builder.flatResponseProperties(propContainer.getFlatProperties());
                        } else if (fieldsAbsent(responseBody)) {
                            responseBodySituation = BodySituationEnum.NONE;
                        } else {
                            responseBodySituation = BodySituationEnum.CHAOS;
                            builder.responseBodyJsonSchema(JsonUtils.beautify(jsonSchema));
                        }
                    } else {
                        responseBodySituation = BodySituationEnum.NONE;
                    }
                } catch (JsonSchemaException ignore) {
                    responseBodySituation = BodySituationEnum.FAIL;
                } catch (Exception e) {
                    log.error("BodySituation.FAIL method={} describe={}",
                            MethodQualifiers.getTypeQualifierWithMethodName(handler), responseBodyDescribe, e);
                    responseBodySituation = BodySituationEnum.FAIL;
                }
                builder.responseBodySituation(responseBodySituation);

                EndpointDto endpoint = builder.build();

                new MarkdownConverter().convert(Lists.newArrayList(endpoint), false);
            }
        });

    }

    private String findGroupNames(ClassOrInterfaceDeclaration controller) {
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        String result = null;
        for (Comment oc : cu.getOrphanComments()) {
            if (oc.isLineComment() && oc.getContent().trim().startsWith("DOC-GROUP")) {
                result = oc.getContent().replaceFirst("DOC-GROUP", "").trim();
                break;
            }
        }
        if (StringUtils.isBlank(result)) {
            result = "未分类";
        }
        return result;
    }

    private boolean fieldsAbsent(ResolvedType requestBody) {
        if (requestBody.isReferenceType()) {
            return requestBody.asReferenceType().getDeclaredFields().size() == 0;
        }
        return false;
    }

    private void clearAllValidatorAndNullableFlag(PropertiesContainerDto propContainer) {
        for (PropertyDto prop : propContainer.getFlatProperties()) {
            prop.setRequired(null);
            prop.setValidators(null);
        }
    }

    private PropertiesContainerDto anaylzeObjectSchema(String requestBodyDescribe, ObjectSchema objectSchema) {
        PropertyTreeNodeDto tempParent = new PropertyTreeNodeDto();
        calcObjectTypeWithRecur(tempParent, objectSchema, false);
        List<PropertyTreeNodeDto> dendriformProperties = getDendriformPropertiesFromTemp(tempParent);
        return new PropertiesContainerDto(requestBodyDescribe, dendriformProperties);
    }

    private List<PropertyTreeNodeDto> getDendriformPropertiesFromTemp(PropertyTreeNodeDto tempParent) {
        return tempParent.getChildren().stream().map(child -> child.setParent(null)).collect(Collectors.toList());
    }

    private JsonTypeEnum calcObjectTypeWithRecur(PropertyTreeNodeDto parent, ObjectSchema parentSchema,
            boolean isInArray) {
        parent.setJsonType(isInArray ? JsonTypeEnum.OBJECT_ARRAY : JsonTypeEnum.OBJECT);

        Collection<PropertyTreeNodeDto> children = Lists.newLinkedList();
        for (Entry<String, JsonSchema> entry : parentSchema.getProperties().entrySet()) {
            String childName = entry.getKey();
            JsonSchema childSchema = entry.getValue();
            PropertyTreeNodeDto child = new PropertyTreeNodeDto();
            child.setId(IdUtils.nextId());
            child.setName(childName);

            JsonPropertyDescriptionValueDto jpdv = null;
            try {
                jpdv = JsonUtils.toObject(childSchema.getDescription(), JsonPropertyDescriptionValueDto.class);
            } catch (Exception ignored) {
            }

            JsonTypeEnum jsonType;
            Boolean isFloat = null;
            Boolean isEnum = null;
            Collection<EnumDto> enums = null;
            JsonSchema forCalcJsonFormat = childSchema;
            if (childSchema.isValueTypeSchema()) {
                ValueTypeSchema valueSchema = childSchema.asValueTypeSchema();
                jsonType = calcValueType(valueSchema, false);
                isFloat = isFloat(valueSchema);
                isEnum = isEnum(valueSchema);
                if (isEnum) {
                    enums = calcEnum(valueSchema);
                }
            } else if (childSchema.isObjectSchema()) {
                jsonType = calcObjectTypeWithRecur(child, childSchema.asObjectSchema(), false);
            } else if (childSchema.isArraySchema()) {
                Items items = childSchema.asArraySchema().getItems();
                if (items == null || items.isArrayItems()) {
                    jsonType = JsonTypeEnum.UNKNOWN;
                } else {
                    JsonSchema eleSchema = items.asSingleItems().getSchema();
                    forCalcJsonFormat = eleSchema;
                    forCalcJsonFormat.setDescription(childSchema.getDescription());
                    if (eleSchema.isValueTypeSchema()) {
                        ValueTypeSchema valueSchema = eleSchema.asValueTypeSchema();
                        jsonType = calcValueType(valueSchema, true);
                        isFloat = isFloat(valueSchema);
                        isEnum = isEnum(valueSchema);
                        if (isEnum) {
                            enums = calcEnum(valueSchema);
                        }
                    } else if (eleSchema.isObjectSchema()) {
                        jsonType = calcObjectTypeWithRecur(child, eleSchema.asObjectSchema(), true);
                    } else if (eleSchema instanceof ReferenceSchema) {
                        jsonType = JsonTypeEnum.RECURSION_ARRAY;
                    } else {
                        jsonType = JsonTypeEnum.UNKNOWN;
                    }
                }
            } else if (childSchema instanceof ReferenceSchema) {
                jsonType = JsonTypeEnum.RECURSION;
            } else {
                jsonType = JsonTypeEnum.UNKNOWN;
            }
            child.setJsonType(jsonType);
            child.setIsFloat(isFloat);
            child.setIsEnum(isEnum);
            child.setEnums(enums);

            if (jpdv != null) {
                child.setDescription(jpdv.getComment());
                child.setValidators(jpdv.getValidators());
                child.setRequired(jpdv.getRequired());
                child.setDatetimePattern(jpdv.getJsonFormatPattern());
            } else {
                child.setRequired(false);
//                log.warn("Cannot found JsonPropertyDescriptionValue. parentSchemaId={} childName={}",
//                        JsonSchemaUtils.getId(parentSchema), childName);
            }

            child.setParent(parent);
            if (child.getChildren() == null) {
                child.setChildren(Lists.newArrayList());
            }
            children.add(child);
        }

        parent.setChildren(children);
        return parent.getJsonType();
    }

    private Boolean isFloat(ValueTypeSchema valueTypeSchema) {
        if (valueTypeSchema.isIntegerSchema()) {
            return false;
        } else if (valueTypeSchema.isNumberSchema()) {
            return true;
        } else {
            return null;
        }
    }

    private Boolean isEnum(ValueTypeSchema valueSchema) {
        return !CollectionUtils.isEmpty(valueSchema.getEnums());
    }

    private Collection<EnumDto> calcEnum(ValueTypeSchema valueSchema) {
        Collection<EnumDto> result = Lists.newArrayList();
        for (String enumJson : valueSchema.getEnums()) {
            EnumDto cad = JsonUtils.toObject(enumJson, EnumDto.class);
            result.add(cad);
        }
        return result;
    }

    private JsonTypeEnum calcValueType(ValueTypeSchema vSchema, boolean isInArray) {
        if (vSchema.isNumberSchema()) {
            return isInArray ? JsonTypeEnum.NUMBER_ARRAY : JsonTypeEnum.NUMBER;
        } else if (vSchema.isStringSchema()) {
            return isInArray ? JsonTypeEnum.STRING_ARRAY : JsonTypeEnum.STRING;
        } else if (vSchema.isBooleanSchema()) {
            return isInArray ? JsonTypeEnum.BOOLEAN_ARRAY : JsonTypeEnum.BOOLEAN;
        } else {
            throw new IllegalArgumentException(vSchema.toString());
        }
    }

    /**
     * 1. 遍历出声明了@RequestBody的参数后返回
     * 2. 发生任何异常时，都会认为没有ResponseBody
     * 异常均会被log.error，除非目标项目源码更新后没有及时编译，否则不应该抛出异常
     */
    private ResolvedType findRequestBody(MethodDeclaration method) {
        for (Parameter parameter : method.getParameters()) {
            Type type = parameter.getType();
            for (AnnotationExpr annotation : parameter.getAnnotations()) {
                try {
                    ResolvedAnnotationDeclaration resolve = annotation.resolve();
                    if (QualifierConstants.REQUEST_BODY.equals(resolve.getQualifiedName())) {
                        try {
                            return type.resolve();
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
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
            if (isRestControllerAnnoAbsent(controller) && isResponseBodyAnnoAbsent(handler)) {
                return null;
            }
            return new ConcernedResponseBodyTypeResolver().findConcernedResponseBodyType(handler);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    private boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return Annotations.isAnnoPresent(handler, Deprecated.class) || Annotations
                .isAnnoPresent(controller, Deprecated.class);
    }

    private boolean isResponseBodyAnnoAbsent(MethodDeclaration handler) {
        return Annotations.isAnnoAbsent(handler, ResponseBody.class);
    }

    private boolean isRestControllerAnnoAbsent(ClassOrInterfaceDeclaration controller) {
        return Annotations.isAnnoAbsent(controller, RestController.class);
    }


    private Collection<RequestMethod> combineVerb(RequestMethod[] cVerbs, RequestMethod[] mVerbs) {
        Collection<RequestMethod> combinedVerbs = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cVerbs)) {
            combinedVerbs.addAll(Arrays.asList(cVerbs));
        }
        if (ArrayUtils.isNotEmpty(mVerbs)) {
            combinedVerbs.addAll(Arrays.asList(mVerbs));
        }
        if (combinedVerbs.size() == 0) {
            combinedVerbs.addAll(Arrays.asList(RequestMethod.values()));
        }
        return combinedVerbs;
    }

    private Collection<String> combineUrl(String[] cPaths, String[] mPaths) {
        Collection<String> combinedUrls = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cPaths) && ArrayUtils.isNotEmpty(mPaths)) {
            for (String cPath : cPaths) {
                for (String mPath : mPaths) {
                    combinedUrls.add(pathMatcher.combine(cPath, mPath));
                }
            }
        } else if (ArrayUtils.isEmpty(cPaths)) {
            combinedUrls.addAll(Arrays.asList(mPaths));
        } else if (ArrayUtils.isEmpty(mPaths)) {
            combinedUrls.addAll(Arrays.asList(cPaths));
        } else {
            combinedUrls.add("/");
        }
        combinedUrls = ensureAllStartWithSlash(combinedUrls);
        return combinedUrls;
    }

    private Collection<String> ensureAllStartWithSlash(Collection<String> urls) {
        Collection<String> result = Lists.newArrayList();
        for (String url : urls) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            result.add(url);
        }
        return result;
    }


    private boolean isNotHandler(Method method) {
        return findRequestMappingAnnoOrElseNull(method) == null;
    }

    private RequestMethod[] findVerbFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new RequestMethod[0] : controllerRequestMapping.method();
    }

    private String[] findValueFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new String[0] : controllerRequestMapping.value();
    }

    private RequestMapping findRequestMappingAnnoOrElseNull(AnnotatedElement controllerClass) {
        return AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);
    }

    private Class<?> tryReflectController(ClassOrInterfaceDeclaration controller, AstForest astForest)
            throws ClassNotFoundException {
        String qualifier = controller.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        try {
            return LoadClassUtils.loadClass(qualifier, astForest.getCurrentClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("类[{}]无法被加载", qualifier);
            throw e;
        }
    }

}
