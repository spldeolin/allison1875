package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.dto.AliasMergedPathVariableDTO;
import com.spldeolin.allison1875.docanalyzer.dto.AliasMergedReqParamDTO;
import com.spldeolin.allison1875.docanalyzer.dto.MvcHandlerDTO;
import com.spldeolin.allison1875.docanalyzer.dto.PathParamDTO;
import com.spldeolin.allison1875.docanalyzer.dto.QueryParamDTO;
import com.spldeolin.allison1875.docanalyzer.enums.ValueTypeEnum;
import com.spldeolin.allison1875.docanalyzer.service.UrlParamService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2025-01-24
 */
@Singleton
@Slf4j
public class UrlParamServiceImpl implements UrlParamService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public List<QueryParamDTO> analyzeQueryParams(MvcHandlerDTO mvcHandler) {
        List<QueryParamDTO> retval = Lists.newArrayList();
        for (Parameter parameter : mvcHandler.getMd().getParameters()) {
            // 只可能0个或1个RequestParam，多个RequestParam会编译错误'Duplicate annotation.'
            annotationExprService.getAnnotation(RequestParam.class.getName(), parameter).ifPresent(reqParam -> {
                AliasMergedReqParamDTO aliasMergedReqParam = mergeReqParam(reqParam);
                ValueTypeEnum valueType;
                try {
                    valueType = induceToValueType(parameter.getType());
                } catch (Exception e) {
                    log.error("fail to induceToValueType, param={}", parameter);
                    throw e;
                }

                QueryParamDTO dto = new QueryParamDTO();
                dto.setName(MoreObjects.firstNonNull(aliasMergedReqParam.getName(), parameter.getNameAsString()));
                dto.setType(valueType);
                dto.setRequired(aliasMergedReqParam.isRequired());
                dto.setDefaultValue(aliasMergedReqParam.getDefaultValue());
                dto.setDescriptionLines(JavadocUtils.getEveryLineByTag(mvcHandler.getMd(), JavadocBlockTag.Type.PARAM,
                        parameter.getNameAsString()));
                retval.add(dto);
            });
        }
        return retval;
    }

    @Override
    public List<PathParamDTO> analyzePathParams(MvcHandlerDTO mvcHandler) {
        List<PathParamDTO> retval = Lists.newArrayList();
        for (Parameter parameter : mvcHandler.getMd().getParameters()) {
            // 只可能0个或1个PathVariable，多个RequestParam会编译错误'Duplicate annotation.'
            annotationExprService.getAnnotation(PathVariable.class.getName(), parameter).ifPresent(pathVariable -> {
                AliasMergedPathVariableDTO aliasMergedPathVariable = mergePathVariable(pathVariable);
                ValueTypeEnum valueType;
                try {
                    valueType = induceToValueType(parameter.getType());
                } catch (Exception e) {
                    log.error("fail to induceToValueType, param={}", parameter);
                    throw e;
                }

                PathParamDTO dto = new PathParamDTO();
                dto.setName(MoreObjects.firstNonNull(aliasMergedPathVariable.getName(), parameter.getNameAsString()));
                dto.setType(valueType);
                dto.setDescriptionLines(JavadocUtils.getEveryLineByTag(mvcHandler.getMd(), JavadocBlockTag.Type.PARAM,
                        parameter.getNameAsString()));
                retval.add(dto);
            });
        }
        return retval;
    }

    protected AliasMergedReqParamDTO mergeReqParam(AnnotationExpr reqParam) {
        if (reqParam.isMarkerAnnotationExpr()) {
            return new AliasMergedReqParamDTO();
        }
        if (reqParam.isSingleMemberAnnotationExpr()) {
            Expression memberValue = reqParam.asSingleMemberAnnotationExpr().getMemberValue();
            String name = memberValue.asStringLiteralExpr().getValue();
            return new AliasMergedReqParamDTO().setName(name);
        }
        if (reqParam.isNormalAnnotationExpr()) {
            AliasMergedReqParamDTO retval = new AliasMergedReqParamDTO();
            for (MemberValuePair pair : reqParam.asNormalAnnotationExpr().getPairs()) {
                if ("name".equals(pair.getName().asString())) {
                    retval.setName(pair.getValue().asStringLiteralExpr().getValue());
                }
                if ("value".equals(pair.getName().asString())) {
                    if (retval.getName() == null) {
                        retval.setName(pair.getValue().asStringLiteralExpr().getValue());
                    } else {
                        // see org.springframework.core.annotation.AnnotationTypeMapping.MirrorSets.MirrorSet.resolve
                        throw new Allison1875Exception("attribute 'name' and its alias 'value' are declared.");
                    }
                }
                if ("required".equalsIgnoreCase(pair.getName().asString())) {
                    retval.setRequired(pair.getValue().asBooleanLiteralExpr().getValue());
                }
                if ("defaultValue".equalsIgnoreCase(pair.getName().asString())) {
                    retval.setDefaultValue(pair.getValue().asStringLiteralExpr().getValue());
                }
            }
            return retval;
        }
        throw new Allison1875Exception("impossible unless bug.");
    }

    protected AliasMergedPathVariableDTO mergePathVariable(AnnotationExpr pathVariable) {
        if (pathVariable.isMarkerAnnotationExpr()) {
            return new AliasMergedPathVariableDTO();
        }
        if (pathVariable.isSingleMemberAnnotationExpr()) {
            Expression memberValue = pathVariable.asSingleMemberAnnotationExpr().getMemberValue();
            String name = memberValue.asStringLiteralExpr().getValue();
            return new AliasMergedPathVariableDTO().setName(name);
        }
        if (pathVariable.isNormalAnnotationExpr()) {
            AliasMergedPathVariableDTO retval = new AliasMergedPathVariableDTO();
            for (MemberValuePair pair : pathVariable.asNormalAnnotationExpr().getPairs()) {
                if ("name".equals(pair.getName().asString())) {
                    retval.setName(pair.getValue().asStringLiteralExpr().getValue());
                }
                if ("value".equals(pair.getName().asString())) {
                    if (retval.getName() == null) {
                        retval.setName(pair.getValue().asStringLiteralExpr().getValue());
                    } else {
                        // see org.springframework.core.annotation.AnnotationTypeMapping.MirrorSets.MirrorSet.resolve
                        throw new Allison1875Exception("attribute 'name' and its alias 'value' are declared.");
                    }
                }
                if ("required".equalsIgnoreCase(pair.getName().asString())) {
                    retval.setRequired(pair.getValue().asBooleanLiteralExpr().getValue());
                }
            }
            return retval;
        }
        throw new Allison1875Exception("impossible unless bug.");
    }

    protected ValueTypeEnum induceToValueType(Type type) {
        if (type.isPrimitiveType()) {
            if (Lists.newArrayList(Primitive.BYTE, Primitive.SHORT, Primitive.INT, Primitive.LONG)
                    .contains(type.asPrimitiveType().getType())) {
                return ValueTypeEnum.INTEGER;
            }
            if (Lists.newArrayList(Primitive.FLOAT, Primitive.DOUBLE).contains(type.asPrimitiveType().getType())) {
                return ValueTypeEnum.DECIMAL;
            }
            if (Primitive.BOOLEAN == type.asPrimitiveType().getType()) {
                return ValueTypeEnum.BOOLEAN;
            }
        }
        if (type.isReferenceType()) {
            if (type.asReferenceType().resolve().asReferenceType().getAllInterfacesAncestors().stream()
                    .anyMatch(ancestor -> ancestor.describe().equalsIgnoreCase("java.lang.CharSequence"))) {
                return ValueTypeEnum.STRING;
            }
        }
        return ValueTypeEnum.UNKNOWN;
    }

}
