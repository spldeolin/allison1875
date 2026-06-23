package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseFieldDeclaration;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType.CREATE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType.DELETE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType.UPDATE;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ApiType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.CreateApiService;
import com.spldeolin.allison1875.formgenerator.service.DeleteApiService;
import com.spldeolin.allison1875.formgenerator.service.GetDetailApiService;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import com.spldeolin.allison1875.formgenerator.service.UpdateApiService;
import com.spldeolin.allison1875.handlertransformer.dto.BuildServiceImplMethodBodyRetval;
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
    private Config config;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private DeleteApiService deleteApiService;

    @Inject
    private GetDetailApiService getDetailApiService;

    @Inject
    private ListApiService listApiService;

    @Inject
    private CreateApiService createApiService;

    @Inject
    private UpdateApiService updateApiService;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public List<AnnotationExpr> buildAnnotationsFormServiceImplMethod(InitDecAnalysisDTO initDecAnalysis) {
        if (Lists.newArrayList(CREATE, UPDATE, DELETE).contains(ApiType.of(initDecAnalysis.getExpansion().get("type")))) {
            return Lists.newArrayList(annotationExprService.springTransactional());
        }
        return Collections.emptyList();
    }

    @Override
    public BuildServiceImplMethodBodyRetval buildServiceImplMethodBody(InitDecAnalysisDTO initDecAnalysis,
            String reqBodyDTOType, List<VariableDeclarator> reqParams, String respBodyDTOType) {
        FormDef form = JsonUtils.toObject(StringEscapeUtils.unescapeJava(initDecAnalysis.getExpansion().get("form")),
                FormDef.class);
        log.info("formDef={}", form);

        // 生成业务实现代码
        BlockStmt body;
        switch (ApiType.of(initDecAnalysis.getExpansion().get("type"))) {
            case CREATE:
                body = createApiService.generateCreateMethodBody(form);
                break;
            case UPDATE:
                body = updateApiService.generateUpdateMethodBody(form);
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

        // 准备必要的imports
        List<String> neededImports = Lists.newArrayList();
        neededImports.add("java.time.*");
        neededImports.add("java.math.*");
        neededImports.add("java.util.*");

        return new BuildServiceImplMethodBodyRetval().setBody(body).setNeededImports(neededImports);
    }

    @Override
    public List<FieldDeclaration> buildFieldsForServiceImpl(ClassOrInterfaceDeclaration serviceImpl,
            InitDecAnalysisDTO initDecAnalysis) {
        FormDef form = JsonUtils.toObject(StringEscapeUtils.unescapeJava(initDecAnalysis.getExpansion().get("form")),
                FormDef.class);
        log.info("form={}", form);

        // 加入主表单Mapper
        String mapperType = DomainContext.get().getMapperPackage() + "." + form.getName() + "Mapper";
        String mapperName = form.getVarName() + "Mapper";
                FieldDeclaration field = parseFieldDeclaration(
                        "private %s %s;", mapperType, mapperName);
        field.addAnnotation(annotationExprService.javaxResource());
        List<FieldDeclaration> retval = Lists.newArrayList(field);

        // 加入关联表单Mapper
        for (ItemDef item : form.getItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                FormDef associationForm = multiSelectItemService.toAssociationForm(form, (MultiSelectItemDef) item);
                mapperType = DomainContext.get().getMapperPackage() + "." + associationForm.getName() + "Mapper";
                mapperName = associationForm.getVarName() + "Mapper";
                        FieldDeclaration associationFormMapperField = parseFieldDeclaration(
                                "private %s %s;", mapperType, mapperName);
                associationFormMapperField.addAnnotation(annotationExprService.javaxResource());
                retval.add(associationFormMapperField);
            }
        }

        // 加入分页total，避免在调用query-transformer前因total不存在而编译错误
                field = parseFieldDeclaration(
                        "private final Long query%sTotal = 0L;", form.getName());
        retval.add(field);

        return retval;
    }

}
