package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.service.DeleteApiService;
import com.spldeolin.allison1875.formgenerator.service.GetDetailApiService;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2025-08-14
 */
@Slf4j
@Singleton
public class FormGeneratorServiceLayerExpansionServiceImpl implements ServiceLayerExpansionService {

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private DeleteApiService deleteApiService;

    @Inject
    private GetDetailApiService getDetailApiService;

    @Inject
    private ListApiService listApiService;

    @Inject
    private SaveApiService saveApiService;

    @Override
    public BlockStmt buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis, String reqBodyDTOType,
            List<VariableDeclarator> reqParams, String respBodyDTOType) {
        FormDef form = JsonUtils.toObject(StringEscapeUtils.unescapeJava(initDecAnalysis.getExpansion().get("form")),
                FormDef.class);
        log.info("formDef={}", form);

        // 生成业务实现代码
        BlockStmt body;
        switch (ApiType.of(initDecAnalysis.getExpansion().get("type"))) {
            case SAVE:
                body = saveApiService.generateMethodBody(form);
                break;
            case LIST:
                body = listApiService.generateMethodBody(form);
                break;
            case GET_DETAIL:
                body = getDetailApiService.generateMethodBody(form);
                break;
            case DELETE:
                body = deleteApiService.generateMethodBody(form);
                break;
            default:
                throw new RuntimeException("Unknown api type");
        }
        return body;
    }

    @Override
    public Optional<FieldDeclaration> buildFieldForServiceImpl(ClassOrInterfaceDeclaration serviceImpl,
            InitDecAnalysisDTO initDecAnalysis) {
        FormDef form = JsonUtils.toObject(StringEscapeUtils.unescapeJava(initDecAnalysis.getExpansion().get("form")),
                FormDef.class);
        log.info("form={}", form);

        String mapperType =
                commonConfig.getMapperPackage() + "." + MoreStringUtils.toUpperCamel(form.getName()) + "Mapper";
        String mapperName = MoreStringUtils.toLowerCamel(form.getName()) + "Mapper";
        FieldDeclaration field = StaticJavaParser.parseBodyDeclaration(
                String.format("private %s %s;", mapperType, mapperName)).asFieldDeclaration();
        field.addAnnotation(annotationExprService.springAutowired());

        return Optional.of(field);
    }

}
