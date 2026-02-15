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
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2025-08-14
 */
@Slf4j
public class FormGeneratorServiceLayerExpansionServiceImpl implements ServiceLayerExpansionService {

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public BlockStmt buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis, String reqBodyDTOType,
            List<VariableDeclarator> reqParams, String respBodyDTOType) {
        BlockStmt body = new BlockStmt();

        FormDef form = JsonUtils.toObject(StringEscapeUtils.unescapeJava(initDecAnalysis.getExpansion().get("form")),
                FormDef.class);
        log.info("formDef={}", form);

        if (respBodyDTOType != null) {
            body.addStatement("return null;");
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
