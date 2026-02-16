package com.spldeolin.allison1875.formgenerator;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern.TODO;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.ast.ProceedingAstForest;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.DdlService;
import com.spldeolin.allison1875.formgenerator.service.DeleteApiService;
import com.spldeolin.allison1875.formgenerator.service.EnumService;
import com.spldeolin.allison1875.formgenerator.service.GetDetailApiService;
import com.spldeolin.allison1875.formgenerator.service.InitDecService;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformer;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformer.Retval;
import com.spldeolin.allison1875.handlertransformer.config.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.service.impl.ServiceLayerExpansionServiceImplManager;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGenerator;
import com.spldeolin.allison1875.persistencegenerator.config.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.querytransformer.QueryTransformer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Slf4j
public class FormGenerator implements Allison1875MainService {

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private FormGeneratorConfig formGeneratorConfig;

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Inject
    private PersistenceGenerator persistenceGenerator;

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private HandlerTransformer handlerTransformer;

    @Inject
    private QueryTransformer queryTransformer;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private InitDecService initDecService;

    @Inject
    private ServiceLayerExpansionServiceImplManager serviceMethodServiceImplManager;

    @Inject
    private DdlService ddlService;

    @Inject
    private EnumService enumService;

    @Inject
    private DeleteApiService deleteApiService;

    @Inject
    private GetDetailApiService getDetailApiService;

    @Inject
    private ListApiService listApiService;

    @Inject
    private SaveApiService saveApiService;

    @Override
    public void process(AstForest astForest) {
        List<FormDef> forms = deserializeDSL();
        if (CollectionUtils.isEmpty(forms)) {
            log.warn("no form definitions detected");
            return;
        }

        // 为每个Form增加业务主键、审计字段等
        addCommonItems(forms);

        List<FileFlush> flushes = Lists.newArrayList(); // 多组件flushes合成为一个

        // 生成DDL
        String ddl = ddlService.generateDdl(forms);
        Path ddlSql = astForest.getSourceRoot().resolve("../../../../sql/ddl.sql");
        log.info("build ddl.sql, path={}", ddlSql.normalize());
        flushes.add(FileFlush.build(ddlSql.toFile(), ddl));

        // 生成持久层
        persistenceGeneratorConfig.setJdbcUrl(null).setDdl(ddl).setEnableGenerateDesign(true);
        PersistenceGenerator.Retval persistenceGeneratorRetval = persistenceGenerator.process();
        flushes.addAll(persistenceGeneratorRetval.getFlushes());

        // persistence-generator执行完毕，获取designCu以供query-transformer使用
        ProceedingAstForest proceedingAstForest = new ProceedingAstForest(astForest);
        AstForestContext.set(proceedingAstForest);
        proceedingAstForest.addUnflushedCus(persistenceGeneratorRetval.getDesignCus());

        // 生成枚举
        flushes.addAll(enumService.generateEnums(forms));

        // 生成controller和initDec
        for (FormDef form : forms) {
            CompilationUnit cu = CompilationUnitUtils.newBaseCurrentAstForest();
            String controllerName = MoreStringUtils.toUpperCamel(form.getName()) + "Controller";
            Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getSourceRoot(),
                    commonConfig.getControllerPackage(), controllerName + ".java");
            cu.setStorage(absulutePath);
            cu.setPackageDeclaration(commonConfig.getControllerPackage());
            cu.addImport(commonConfig.getDesignPackage() + ".*");
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            JavadocUtils.setJavadoc(coid, form.getTitle(), commonConfig.getAuthor());
            coid.addAnnotation(annotationExprService.springRestController());
            coid.setPublic(true).setName(controllerName);
            cu.addType(coid);
            coid.addMember(initDecService.buildSaveHandler(form));
            coid.addMember(initDecService.buildListHandler(form));
            coid.addMember(initDecService.buildGetDetailHandler(form));
            coid.addMember(initDecService.buildDeleteHandler(form));
            proceedingAstForest.addUnflushedCu(cu);
        }

        // 运行时替换form-generator中ServiceMethodService的实现类
        FormGeneratorServiceLayerExpansionServiceImpl expansionService =
                new FormGeneratorServiceLayerExpansionServiceImpl(
                commonConfig, annotationExprService, deleteApiService, getDetailApiService, listApiService,
                saveApiService);
        serviceMethodServiceImplManager.setCurrentImpl(expansionService);

        // 调用handler-transformer转换initDec
        Retval handlerTransformerRetval = handlerTransformer.internalProcess(proceedingAstForest);
        flushes.addAll(handlerTransformerRetval.getFlushes());

        // handler-transformer执行完毕，可获取到ServiceImpl的CU
        proceedingAstForest.addUnflushedCus(handlerTransformerRetval.getServiceImplCus());

        // 调用query-transformer转换业务层的DesignChain
        flushes.addAll(queryTransformer.internalProcess(proceedingAstForest));

        // write all to file
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        }
    }

    private void addCommonItems(List<FormDef> forms) {
        for (FormDef form : forms) {
            TextItemDef bizId = new TextItemDef();
            bizId.setName(StringUtils.uncapitalize(form.getName()) + "Code");
            bizId.setTitle("业务主键");
            bizId.setIsNonVoid(true);
            bizId.setInitPattern(TODO);
            bizId.setEditPattern(InitOrEditPattern.DO_NOT);
            bizId.setMaxLength(36);
            form.getItems().add(0, bizId);
            TimeItemDef createdAt = new TimeItemDef();
            createdAt.setName("createdAt");
            createdAt.setTitle("创建时间");
            createdAt.setIsNonVoid(true);
            createdAt.setInitPattern(TODO);
            createdAt.setEditPattern(InitOrEditPattern.DO_NOT);
            form.getItems().add(createdAt);
            TimeItemDef updatedAt = new TimeItemDef();
            updatedAt.setName("updatedAt");
            updatedAt.setTitle("更新时间");
            updatedAt.setIsNonVoid(true);
            updatedAt.setInitPattern(TODO);
            updatedAt.setEditPattern(TODO);
            form.getItems().add(updatedAt);
            IndexDef index = new IndexDef();
            index.setItemNames(Lists.newArrayList(bizId.getName()));
            index.setIsUnique(true);
            form.getIndices().add(0, index);
        }
    }

    private List<FormDef> deserializeDSL() {
        try {
            return new YAMLMapper().readValue(
                    FileUtils.readFileToString(formGeneratorConfig.getDslPath(), StandardCharsets.UTF_8),
                    new TypeReference<List<FormDef>>() {
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}