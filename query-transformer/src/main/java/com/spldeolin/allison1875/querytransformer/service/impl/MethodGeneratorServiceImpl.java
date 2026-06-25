package com.spldeolin.allison1875.querytransformer.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseAnnotation;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseType;

import java.util.List;
import java.util.Set;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.dto.FieldArg;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.KeywordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.JavaTypeDTO;
import com.spldeolin.allison1875.querytransformer.dto.Binary;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.CompareableBinary;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.ExpandedFieldDTO;
import com.spldeolin.allison1875.querytransformer.dto.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.dto.VariableProperty;
import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnStyleEnum;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import com.spldeolin.allison1875.querytransformer.service.MethodGeneratorService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Slf4j
public class MethodGeneratorServiceImpl implements MethodGeneratorService {

    @Inject
    private Config config;

    @Inject
    private DataModelService dataModelGeneratorService;

    @Inject
    private MapperLayerExpansionService mapperLayerExpansionService;

    @Override
    public GenerateParamRetval generateParam(ChainAnalysisDTO chainAnalysis) {
        List<Parameter> params = Lists.newArrayList();
        boolean isParamDTO = false;

        Set<Binary> binaries = chainAnalysis.getBinariesAsArgs();
        ExpandParamRetval expandParamRetval = mapperLayerExpansionService.expandParam(chainAnalysis);
        List<ExpandedFieldDTO> expandedFields = expandParamRetval.getExpandedFields();
        int totalFieldCount = binaries.size() + expandedFields.size();
        if (totalFieldCount > 3 || (totalFieldCount > 1 && chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE)) {
            DataModelArg dataModelArg = new DataModelArg();
            dataModelArg.setSourceRoot(DomainContext.get().getPersistenceSourceRoot());
            dataModelArg.setPackageName(DomainContext.get().getParamDTOPackage());
            dataModelArg.setClassName(MoreStringUtils.toUpperCamel(chainAnalysis.getMethodName()) + "Param");
            dataModelArg.setAuthor(config.getAuthor());
            for (Binary binary : binaries) {
                String varName = binary.getVarName();
                JavaTypeDTO javaType = binary.getProperty().getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(binary.getProperty().getDescription());
                if (binary instanceof CompareableBinary && Lists.newArrayList(ComparisonOperatorEnum.IN,
                        ComparisonOperatorEnum.NOT_IN).contains(((CompareableBinary) binary).getComparisonOperator())) {
                    fieldArg.setTypeQualifier("java.util.List<" + javaType.getQualifier() + ">");
                } else {
                    fieldArg.setTypeQualifier(javaType.getQualifier());
                }
                fieldArg.setFieldName(varName);
                dataModelArg.getFieldArgs().add(fieldArg);
            }
            if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                dataModelArg.getFieldArgs()
                        .add(new FieldArg().setTypeQualifier("java.lang.Integer").setFieldName("offset"));
                dataModelArg.getFieldArgs()
                        .add(new FieldArg().setTypeQualifier("java.lang.Integer").setFieldName("limit"));
            }
            for (ExpandedFieldDTO expandedField : expandedFields) {
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(expandedField.getDescription());
                fieldArg.setTypeQualifier(expandedField.getTypeQualifier());
                fieldArg.setFieldName(expandedField.getFieldName());
                dataModelArg.getFieldArgs().add(fieldArg);
            }
            dataModelArg.setDataModelExistenceResolution(FileExistenceResolutionEnum.RENAME);
            DataModelGeneration paramDTOGeneration = dataModelGeneratorService.generateDataModel(dataModelArg);
            Parameter param = new Parameter();
            param.setType(paramDTOGeneration.getDtoQualifier());
            param.setName(MoreStringUtils.toLowerCamel(paramDTOGeneration.getDtoName()));
            params.add(param);
            isParamDTO = true;
        } else if (CollectionUtils.isNotEmpty(binaries)) {
            for (Binary binary : binaries) {
                String varName = binary.getVarName();
                JavaTypeDTO javaType = binary.getProperty().getJavaType();
                Parameter param = new Parameter();
                param.addAnnotation(parseAnnotation(
                        String.format("@org.apache.ibatis.annotations.Param(\"%s\")", varName)));

                if (binary instanceof CompareableBinary && Lists.newArrayList(ComparisonOperatorEnum.IN,
                        ComparisonOperatorEnum.NOT_IN).contains(((CompareableBinary) binary).getComparisonOperator())) {
                    param.setType("java.util.List<" + javaType.getQualifier() + ">");
                } else {
                    param.setType(javaType.getQualifier());
                }
                param.setName(varName);
                params.add(param);
            }
            if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                params.add(new Parameter().setType("Integer").setName("offset"));
                params.add(new Parameter().setType("Integer").setName("limit"));
            }
            for (ExpandedFieldDTO expandedField : expandedFields) {
                Parameter param = new Parameter();
                param.addAnnotation(parseAnnotation(
                        String.format("@org.apache.ibatis.annotations.Param(\"%s\")", expandedField.getFieldName())));
                param.setType(expandedField.getTypeQualifier());
                param.setName(expandedField.getFieldName());
                params.add(param);
            }
        } else {
            GenerateParamRetval retval = new GenerateParamRetval();
            retval.setIsParamDTO(false);
            retval.setExpandParamRetval(expandParamRetval);
            if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                retval.getParameters().add(new Parameter().setType("Integer").setName("offset"));
                retval.getParameters().add(new Parameter().setType("Integer").setName("limit"));
            }
            for (ExpandedFieldDTO expandedField : expandedFields) {
                Parameter param = new Parameter();
                param.addAnnotation(parseAnnotation(
                        String.format("@org.apache.ibatis.annotations.Param(\"%s\")", expandedField.getFieldName())));
                param.setType(expandedField.getTypeQualifier());
                param.setName(expandedField.getFieldName());
                retval.getParameters().add(param);
            }
            return retval;
        }

        GenerateParamRetval result = new GenerateParamRetval();
        result.getParameters().addAll(params);
        result.setIsParamDTO(isParamDTO);
        result.setExpandParamRetval(expandParamRetval);
        return result;
    }

    @Override
    public GenerateReturnTypeRetval generateReturnType(ChainAnalysisDTO chainAnalysis, DesignMetaDTO designMeta) {
        boolean isAssigned = isAssigned(chainAnalysis);
        GenerateReturnTypeRetval result = new GenerateReturnTypeRetval();

        if (Lists.newArrayList(KeywordConstant.ChainInitialMethod.UPDATE, KeywordConstant.ChainInitialMethod.DELETE)
                .contains(chainAnalysis.getChainInitialMethod())) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.COUNT) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (isAssigned) {
            result.setElementTypeQualifier(chainAnalysis.getEntityQualifier());
            if (Lists.newArrayList(ReturnStyleEnum.LIST, ReturnStyleEnum.GROUP, ReturnStyleEnum.PAGE)
                    .contains(chainAnalysis.getReturnStyle())) {
                result.setResultType(
                        parseType("java.util.List<" + chainAnalysis.getEntityQualifier() + ">"));
            } else if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.MAP) {
                String propertyTypeName = chainAnalysis.getMapOrGroupKeyProperty().getJavaType().getQualifier();
                result.setResultType(parseType(
                        "java.util.Map<" + propertyTypeName + ", " + chainAnalysis.getEntityQualifier() + ">"));
            } else {
                result.setResultType(parseType(chainAnalysis.getEntityQualifier()));
            }
            return result;
        }

        Set<VariableProperty> returnProps = chainAnalysis.getPropertiesAsResult();
        if (returnProps.size() > 1) {
            DataModelArg dataModelArg = new DataModelArg();
            dataModelArg.setSourceRoot(DomainContext.get().getPersistenceSourceRoot());
            dataModelArg.setPackageName(DomainContext.get().getRecordDTOPackage());
            dataModelArg.setClassName(MoreStringUtils.toUpperCamel(chainAnalysis.getMethodName()) + "Record");
            dataModelArg.setAuthor(config.getAuthor());
            for (VariableProperty returnProp : returnProps) {
                JavaTypeDTO javaType = returnProp.getProperty().getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(returnProp.getProperty().getDescription());
                fieldArg.setTypeQualifier(javaType.getQualifier());
                fieldArg.setFieldName(returnProp.getVarName());
                dataModelArg.getFieldArgs().add(fieldArg);
            }
            dataModelArg.setDataModelExistenceResolution(FileExistenceResolutionEnum.RENAME);
            DataModelGeneration recordDTOGeneration = dataModelGeneratorService.generateDataModel(dataModelArg);
            result.setElementTypeQualifier(recordDTOGeneration.getDtoQualifier());
            if (Lists.newArrayList(ReturnStyleEnum.LIST, ReturnStyleEnum.GROUP, ReturnStyleEnum.PAGE)
                    .contains(chainAnalysis.getReturnStyle())) {
                result.setResultType(
                        parseType("java.util.List<" + recordDTOGeneration.getDtoQualifier() + ">"));
            } else if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.MAP) {
                String propertyTypeName = chainAnalysis.getMapOrGroupKeyProperty().getJavaType().getQualifier();
                result.setResultType(parseType(
                        "java.util.Map<" + propertyTypeName + " ," + recordDTOGeneration.getDtoQualifier() + ">"));
            } else {
                result.setResultType(parseType(recordDTOGeneration.getDtoQualifier()));
            }
            return result;

        } else if (returnProps.size() == 1) {
            // 指定了1个属性，使用该属性类型作为返回值类型
            VariableProperty returnProp = Iterables.getOnlyElement(returnProps);
            JavaTypeDTO javaType = returnProp.getProperty().getJavaType();
            result.setElementTypeQualifier(javaType.getQualifier());
            if (Lists.newArrayList(ReturnStyleEnum.LIST, ReturnStyleEnum.GROUP, ReturnStyleEnum.PAGE)
                    .contains(chainAnalysis.getReturnStyle())) {
                result.setResultType(parseType("java.util.List<" + javaType.getQualifier() + ">"));
            } else if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.MAP) {
                String propertyTypeName = chainAnalysis.getMapOrGroupKeyProperty().getJavaType().getQualifier();
                result.setResultType(parseType(
                        "java.util.Map<" + propertyTypeName + " ," + javaType.getQualifier() + ">"));
            } else {
                result.setResultType(parseType(javaType.getQualifier()));
            }
            return result;

        } else {
            // 没有指定属性，使用Entity作为返回值类型
            result.setElementTypeQualifier(chainAnalysis.getEntityQualifier());
            if (Lists.newArrayList(ReturnStyleEnum.LIST, ReturnStyleEnum.GROUP, ReturnStyleEnum.PAGE)
                    .contains(chainAnalysis.getReturnStyle())) {
                result.setResultType(
                        parseType("java.util.List<" + chainAnalysis.getEntityQualifier() + ">"));
            } else if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.MAP) {
                String propertyTypeName = chainAnalysis.getMapOrGroupKeyProperty().getJavaType().getQualifier();
                result.setResultType(parseType(
                        "java.util.Map<" + propertyTypeName + " ," + chainAnalysis.getEntityQualifier() + ">"));
            } else {
                result.setResultType(parseType(chainAnalysis.getEntityQualifier()));
            }
            return result;
        }
    }

    private boolean isAssigned(ChainAnalysisDTO chainAnalysis) {
        if (chainAnalysis.getChain().getParentNode().isPresent()) {
            return chainAnalysis.getChain().getParentNode().get().getParentNode()
                    .filter(parent -> parent instanceof VariableDeclarationExpr).isPresent();
        }
        return false;
    }

}