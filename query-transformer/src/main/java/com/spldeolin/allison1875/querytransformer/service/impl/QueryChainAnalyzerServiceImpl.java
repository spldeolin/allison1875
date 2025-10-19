package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.KeywordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.enums.PageParamStyleEnum;
import com.spldeolin.allison1875.querytransformer.dto.AssignmentDTO;
import com.spldeolin.allison1875.querytransformer.dto.Binary;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.JoinClauseDTO;
import com.spldeolin.allison1875.querytransformer.dto.JoinConditionDTO;
import com.spldeolin.allison1875.querytransformer.dto.JoinedPropertyDTO;
import com.spldeolin.allison1875.querytransformer.dto.SearchConditionDTO;
import com.spldeolin.allison1875.querytransformer.dto.SortPropertyDTO;
import com.spldeolin.allison1875.querytransformer.dto.VariableProperty;
import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;
import com.spldeolin.allison1875.querytransformer.enums.JoinTypeEnum;
import com.spldeolin.allison1875.querytransformer.enums.OrderSequenceEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnStyleEnum;
import com.spldeolin.allison1875.querytransformer.service.DesignService;
import com.spldeolin.allison1875.querytransformer.service.QueryChainAnalyzerService;
import com.spldeolin.allison1875.support.OnChainComparison;
import com.spldeolin.allison1875.support.OrderByChainSequence;
import com.spldeolin.allison1875.support.PropertyName;
import com.spldeolin.allison1875.support.WhereChainComparison;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Slf4j
public class QueryChainAnalyzerServiceImpl implements QueryChainAnalyzerService {

    private static final Pattern matchJoinPropertyNames = Pattern.compile(
            "(left|right|inner|outer)Join\\(\\)\\.(.*?)\\.on\\(\\)");

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private DesignService designService;

    @Inject
    private CommonConfig commonConfig;

    @Override
    public ChainAnalysisDTO analyzeDesignChain(MethodCallExpr designChain, DesignMetaDTO designMeta) {
        String chainCode = designChain.toString();
        String betweenCode = chainCode.substring(chainCode.indexOf(".") + 1, chainCode.lastIndexOf("."));
        String designQualifier = designMeta.getDesignQualifier();

        KeywordConstant.ChainInitialMethod initialMethod;
        if (betweenCode.startsWith("select(")) {
            initialMethod = KeywordConstant.ChainInitialMethod.SELECT;
        } else if (betweenCode.startsWith("update(")) {
            initialMethod = KeywordConstant.ChainInitialMethod.UPDATE;
        } else if (betweenCode.startsWith("delete(")) {
            initialMethod = KeywordConstant.ChainInitialMethod.DELETE;
        } else {
            throw new Allison1875Exception("initialMethod is none of select, update nor delete");
        }

        String methodName = this.analyzeSpecifiedMethodName(initialMethod, designChain, designMeta);
        String countMethodNameForPage = analyzeCountMethodNameForPage(methodName);

        ReturnStyleEnum returnStyle;
        PropertyDTO mapOrGroupKeyProperty = null;
        Expression offsetExpr = null;
        Expression limitExpr = null;
        if (designChain.getNameAsString().equals("one")) {
            returnStyle = ReturnStyleEnum.ONE;
        } else if (designChain.getNameAsString().equals("list")) {
            returnStyle = ReturnStyleEnum.LIST;
        } else if (designChain.getNameAsString().equals("count")) {
            returnStyle = ReturnStyleEnum.COUNT;
        } else if (designChain.getNameAsString().equals("page")) {
            returnStyle = ReturnStyleEnum.PAGE;
            if (designMeta.getPageParamStyle() == PageParamStyleEnum.OFFSET_LIMIT) {
                offsetExpr = designChain.getArgument(0);
            } else {
                offsetExpr = new BinaryExpr(new EnclosedExpr(
                        new BinaryExpr(designChain.getArgument(0), new IntegerLiteralExpr("1"), Operator.MINUS)),
                        designChain.getArgument(1), Operator.MULTIPLY);
            }
            limitExpr = designChain.getArgument(1);
        } else if (designChain.getNameAsString().startsWith("mapBy")) {
            returnStyle = ReturnStyleEnum.MAP;
            String keyName = StringUtils.uncapitalize(StringUtils.removeStart(designChain.getNameAsString(), "mapBy"));
            mapOrGroupKeyProperty = designMeta.getProperties().get(keyName);
            if (mapOrGroupKeyProperty == null) {
                throw new Allison1875Exception("mapKeyProperty not found, keyName=" + keyName);
            }
        } else if (designChain.getNameAsString().startsWith("groupBy")) {
            returnStyle = ReturnStyleEnum.GROUP;
            String keyName = StringUtils.uncapitalize(
                    StringUtils.removeStart(designChain.getNameAsString(), "groupBy"));
            mapOrGroupKeyProperty = designMeta.getProperties().get(keyName);
            if (mapOrGroupKeyProperty == null) {
                throw new Allison1875Exception("mapKeyProperty not found, keyName=" + keyName);
            }
        } else {
            returnStyle = null;
        }
        log.info("initialMethod={} returnStyle={} offset={} limit={}", initialMethod, returnStyle, offsetExpr,
                limitExpr);

        Set<PropertyDTO> selectProperties = Sets.newLinkedHashSet();
        Set<SearchConditionDTO> searchConditions = Sets.newLinkedHashSet();
        Set<SortPropertyDTO> sortProperties = Sets.newLinkedHashSet();
        Set<JoinClauseDTO> joinClauses = Sets.newLinkedHashSet();
        Set<AssignmentDTO> assignments = Sets.newLinkedHashSet();
        Map<String/*joinedEntityDesignQualifier*/, Set<JoinConditionDTO>> joinConditions = Maps.newHashMap();

        // 防Cond中的字段名重复（分析where和update中使用）
        List<String> propertyVarNamesInParam = Lists.newArrayList();
        // 防Record中的字段名重复（分析select col和joined col中使用）
        List<String> propertyVarNamesInRecord = Lists.newArrayList();

        ClassOrInterfaceDeclaration joinChain = designService.findCoidWithChecksum(
                commonConfig.getDesignPackage() + ".JoinChain");
        List<String> propertyNamesFromJoinChain = joinChain.getMembers().stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration).flatMap(coid -> coid.getFields().stream())
                .map(fd -> fd.getVariable(0).getNameAsString()).distinct().collect(Collectors.toList());

        for (FieldAccessExpr fae : designChain.findAll(FieldAccessExpr.class, TreeTraversal.POSTORDER)) {
            if (!designMeta.getProperties().containsKey(fae.getNameAsString()) && !propertyNamesFromJoinChain.contains(
                    fae.getNameAsString())) {
                // 例如：XxxxDesign.query("xx").by().privilegeCode.in(Lists.newArrayList(OneTypeEnum.FIRST.getCode()))
                // .list();，其中的OneTypeEnum.FIRST应当被跳过
                continue;
            }

            String describe;
            try {
                describe = fae.calculateResolvedType().describe();
            } catch (Exception e) {
                // 如果fae的scope中出现了调用内部类对象的方法，会进入这个分支，暂时需要规避这种用法
                log.warn("fail to resolve, fae={}", fae);
                continue;
            }

            // 对应SELECT子句中的col_name
            if (describe.startsWith(designQualifier + ".QueryChain")) {
                selectProperties.add(designMeta.getProperties().get(fae.getNameAsString()));
                propertyVarNamesInRecord.add(fae.getNameAsString());
            }

            // 对应WHERE子句中的binary
            if (describe.startsWith(WhereChainComparison.class.getName()) && fae.getParentNode().isPresent()) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                ComparisonOperatorEnum predicate = ComparisonOperatorEnum.of(parent.getNameAsString());
                SearchConditionDTO searchCond = new SearchConditionDTO();
                searchCond.setProperty(designMeta.getProperties().get(fae.getNameAsString()));
                searchCond.setVarName(fae.getNameAsString());
                searchCond.setComparisonOperator(predicate);
                if (CollectionUtils.isNotEmpty(parent.getArguments())) {
                    String varName = antiDuplicationService.getNewElementIfExist(fae.getNameAsString(),
                            propertyVarNamesInParam);
                    propertyVarNamesInParam.add(varName);
                    searchCond.setVarName(varName);
                    searchCond.setArgument(parent.getArgument(0));
                }
                searchConditions.add(searchCond);
            }

            // 对应ORDER BY子句中的col_name和ASC / DESC
            if (describe.startsWith(OrderByChainSequence.class.getName()) && fae.getParentNode().isPresent()) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                OrderSequenceEnum predicate = OrderSequenceEnum.of(parent.getNameAsString());
                SortPropertyDTO phrase = new SortPropertyDTO();
                phrase.setPropertyName(fae.getNameAsString());
                phrase.setOrderSequence(predicate);
                sortProperties.add(phrase);
            }

            // 对应每个JOIN子句的ON子句的每个binary
            if (describe.startsWith(OnChainComparison.class.getName()) && fae.getParentNode().isPresent()) {
                MethodCallExpr parent = (MethodCallExpr) fae.getParentNode().get();
                ComparisonOperatorEnum predicate = ComparisonOperatorEnum.of(parent.getNameAsString());
                JoinConditionDTO joinCond = new JoinConditionDTO();
                // 这里以joinedDesignQualifier作为联系，便不再获取JoinedDesignMeta了，没有后者也就无法获取到PropertyDTO，所以只能先行setPropertyName
                joinCond.setProperty(new PropertyDTO().setPropertyName(fae.getNameAsString()));
                joinCond.setVarName(fae.getNameAsString());
                joinCond.setComparisonOperator(predicate);
                if (CollectionUtils.isNotEmpty(parent.getArguments())) {
                    if (!parent.getArgument(0).calculateResolvedType().describe()
                            .startsWith(PropertyName.class.getName() + "<")) {
                        String varName = antiDuplicationService.getNewElementIfExist(fae.getNameAsString(),
                                propertyVarNamesInParam);
                        propertyVarNamesInParam.add(varName);
                        joinCond.setVarName(varName);
                        joinCond.setArgument(parent.getArgument(0));
                    } else {
                        // 说明是MyEntityDesign.myProperty，无需anti-dupl，但需要在onPhrase记录propertyName4Comparing，应该不能用PhraseDTO了
                        joinCond.setComparedProperty(new PropertyDTO().setPropertyName(
                                parent.getArgument(0).asFieldAccessExpr().getNameAsString()));
                    }
                }
                String designMarkerQualifier = fae.resolve().asField().declaringType().asClass().getAllInterfaces()
                        .get(0).describe();
                String joinedDesignQualifier = MoreStringUtils.splitAndGetLastPart(designMarkerQualifier, ".")
                        .replace('_', '.');
                joinConditions.computeIfAbsent(joinedDesignQualifier, v -> Sets.newLinkedHashSet()).add(joinCond);
            }
        }

        // JOIN部分
        Matcher matcher = matchJoinPropertyNames.matcher(chainCode);
        while (matcher.find()) {
            // 对应JOIN子句的tbl_name
            String joinedEntityWithProperty = matcher.group(2);
            String entityName = joinedEntityWithProperty.split("\\.")[0];
            ClassOrInterfaceDeclaration joinedDesign = designService.findCoidWithChecksum(
                    this.getJoinedDesignQualifier(joinChain, entityName));
            DesignMetaDTO joinedDesignMeta = designService.findDesignMeta(joinedDesign);

            // 对应SELECT子句中的col_name（join表的col）
            Set<JoinedPropertyDTO> joinedProperties = Sets.newLinkedHashSet();
            Stream<String> joinedPropertyNames = this.extractPropertyNames(joinedEntityWithProperty, joinedDesignMeta);
            joinedPropertyNames.forEach(joinedPropertyName -> {
                String varName = StringUtils.uncapitalize(entityName) + StringUtils.capitalize(joinedPropertyName);
                varName = antiDuplicationService.getNewElementIfExist(varName, propertyVarNamesInRecord);
                propertyVarNamesInRecord.add(varName);
                JoinedPropertyDTO joinedProperty = new JoinedPropertyDTO();
                joinedProperty.setProperty(joinedDesignMeta.getProperties().get(joinedPropertyName));
                joinedProperty.setVarName(varName);
                joinedProperties.add(joinedProperty);
            });

            // 对应每个JOIN子句的ON子句的每个binary
            Set<JoinConditionDTO> joinConds = joinConditions.get(joinedDesignMeta.getDesignQualifier());
            for (JoinConditionDTO joinCond : joinConds) {
                joinCond.setProperty(joinedDesignMeta.getProperties().get(joinCond.getProperty().getPropertyName()));
                if (joinCond.getComparedProperty() != null) {
                    joinCond.setComparedProperty(
                            designMeta.getProperties().get(joinCond.getComparedProperty().getPropertyName()));
                }
            }

            JoinClauseDTO joinClause = new JoinClauseDTO();
            joinClause.setJoinType(JoinTypeEnum.of(matcher.group(1)));
            joinClause.setJoinedDesignMeta(joinedDesignMeta);
            joinClause.setJoinedProperties(joinedProperties);
            joinClause.setJoinConditions(joinConds);
            joinClauses.add(joinClause);
        }

        // 如果终结方法是Each或者MultiEach，确保queryPhrases中必须包含each的key
        if (mapOrGroupKeyProperty != null && CollectionUtils.isNotEmpty(selectProperties) && !selectProperties.stream()
                .map(PropertyDTO::getPropertyName).collect(Collectors.toList())
                .contains(mapOrGroupKeyProperty.getPropertyName())) {
            log.warn("Each or MultiEach Key [{}] is not declared in Query Phrases [{}], auto add in",
                    mapOrGroupKeyProperty.getPropertyName(), selectProperties);
            selectProperties.add(mapOrGroupKeyProperty);
        }

        // update set assignment
        for (MethodCallExpr mce : designChain.findAll(MethodCallExpr.class, TreeTraversal.POSTORDER)) {
            String describe;
            try {
                describe = mce.calculateResolvedType().describe();
            } catch (Exception e) {
                log.warn("fail to resolve, mce={}", mce);
                continue;
            }
            if (describe.startsWith(designQualifier + ".NextableUpdateChain")) {
                AssignmentDTO assignment = new AssignmentDTO();
                assignment.setProperty(designMeta.getProperties().get(mce.getNameAsString()));
                String varName = antiDuplicationService.getNewElementIfExist(mce.getNameAsString(),
                        propertyVarNamesInParam);
                propertyVarNamesInParam.add(varName);
                assignment.setVarName(varName);
                assignment.setArgument(mce.getArgument(0));
                assignments.add(assignment);
            }
        }

        // mapper方法的参数字段
        Set<Binary> binaries = Sets.newLinkedHashSet(assignments);
        for (SearchConditionDTO searchCond : searchConditions) {
            if (searchCond.getArgument() != null) {
                binaries.add(searchCond);
            }
        }
        for (JoinClauseDTO joinClause : joinClauses) {
            for (JoinConditionDTO joinCond : joinClause.getJoinConditions()) {
                if (joinCond.getArgument() != null) {
                    binaries.add(joinCond);
                }
            }
        }

        // mapper方法的返回值字段
        Set<VariableProperty> returnVps = Sets.newLinkedHashSet();
        Set<PropertyDTO> returnProperties = selectProperties;
        if (returnProperties.isEmpty()) {
            // 只有join场景需要这么做，确保join中未指定select字段时也能加入到返回字段中；非join场景不需要，返回值直接使用Entity即可
            if (!joinConditions.isEmpty()) {
                returnProperties = Sets.newLinkedHashSet(designMeta.getProperties().values());
            }
        }
        for (PropertyDTO returnProperty : returnProperties) {
            returnVps.add(new VariableProperty() {
                @Override
                public PropertyDTO getProperty() {
                    return returnProperty;
                }

                @Override
                public String getVarName() {
                    return returnProperty.getPropertyName();
                }
            });
        }
        for (JoinClauseDTO joinClause : joinClauses) {
            returnVps.addAll(joinClause.getJoinedProperties());
        }

        log.info("selectProperties={}", JsonUtils.toJsonPrettily(selectProperties));
        log.info("searchConditions={}", JsonUtils.toJsonPrettily(searchConditions));
        log.info("sortProperties={}", JsonUtils.toJsonPrettily(sortProperties));
        log.info("joinClauses={}", JsonUtils.toJsonPrettily(joinClauses));
        log.info("assignments={}", JsonUtils.toJsonPrettily(assignments));
        log.info("binaries={}", JsonUtils.toJsonPrettily(binaries));
        log.info("returnVps={}", JsonUtils.toJsonPrettily(returnVps));

        ChainAnalysisDTO result = new ChainAnalysisDTO();
        result.setEntityQualifier(designMeta.getEntityQualifier());
        result.setMethodName(methodName);
        result.setCountMethodNameForPage(countMethodNameForPage);
        result.setChainInitialMethod(initialMethod);
        result.setReturnStyle(returnStyle);
        result.setSelectProperties(selectProperties);
        result.setSearchConditions(searchConditions);
        result.setOffsetExpr(offsetExpr);
        result.setLimitExpr(limitExpr);
        result.setSortProperties(sortProperties);
        result.setJoinClauses(joinClauses);
        result.setAssignments(assignments);
        result.setBinariesAsArgs(binaries);
        result.setPropertiesAsResult(returnVps);
        result.setMapOrGroupKeyProperty(mapOrGroupKeyProperty);
        result.setChain(designChain);
        result.setIsByForced(chainCode.contains("." + KeywordConstant.WHERE_EVEN_NULL_METHOD_NAME + "()"));
        String hash = StringUtils.upperCase(HashingUtils.hashString(result.toString()));
        result.setLotNo(String.format("QT%s-%s", Allison1875.SHORT_VERSION, hash));
        return result;
    }

    private static String analyzeCountMethodNameForPage(String methodName) {
        String countMethodNameForPage;
        if (methodName.startsWith("query")) {
            countMethodNameForPage = methodName.replaceFirst("query", "count");
        } else {
            countMethodNameForPage = "count" + StringUtils.capitalize(methodName);
        }
        return countMethodNameForPage;
    }

    private Stream<String> extractPropertyNames(String joinedEntityWithProperty, DesignMetaDTO joinedEntityMeta) {
        String joinedPropertiesText = StringUtils.substringAfter(joinedEntityWithProperty, ".");
        Stream<String> joinedPropertyNames;
        if (!joinedPropertiesText.isEmpty()) {
            joinedPropertyNames = Arrays.stream(joinedPropertiesText.split("\\.")).distinct();
        } else {
            joinedPropertyNames = Lists.newArrayList(joinedEntityMeta.getProperties().keySet()).stream();
        }
        return joinedPropertyNames;
    }

    private String getJoinedDesignQualifier(ClassOrInterfaceDeclaration joinChain, String entityName) {
        return joinChain.getFieldByName(entityName)
                .orElseThrow(() -> new Allison1875Exception("Entity Field is absent in JoinChain")).getVariable(0)
                .getInitializer().orElseThrow(() -> new Allison1875Exception("Initializer is absent in Entity Field"))
                .asFieldAccessExpr().getScope().asNameExpr().getNameAsString().replace('_', '.');
    }

    private String analyzeSpecifiedMethodName(KeywordConstant.ChainInitialMethod initialMethod, MethodCallExpr chain,
            DesignMetaDTO designMeta) {
        MethodCallExpr queryMce = chain.findAll(MethodCallExpr.class,
                mce -> StringUtils.equalsAny(mce.getNameAsString(), "select", "update", "delete")).get(0);
        NodeList<Expression> arguments = queryMce.getArguments();
        if (CollectionUtils.isEmpty(arguments)) {
            String defaultMethodName =
                    initialMethod.getMethodName() + StringUtils.removeEnd(designMeta.getEntityName(), "Entity");
            log.info("Method name not specified in Query Chain, hence default '{}' is used", defaultMethodName);
            return defaultMethodName;
        }
        String methodName = arguments.get(0).asStringLiteralExpr().getValue().trim();
        return methodName;
    }

}