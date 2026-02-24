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
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.FileSnapshotUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.SpecialItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.DdlService;
import com.spldeolin.allison1875.formgenerator.service.DeleteApiService;
import com.spldeolin.allison1875.formgenerator.service.EnumService;
import com.spldeolin.allison1875.formgenerator.service.GetDetailApiService;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformer;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGenerator;
import com.spldeolin.allison1875.persistencegenerator.config.PersistenceGeneratorConfig;
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
    private HandlerTransformer handlerTransformer;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private FormGeneratorServiceLayerExpansionServiceImpl formGeneratorServiceLayerExpansionServiceImpl;

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

        FileSnapshotUtils.FileSystemSnapshot snapshot = FileSnapshotUtils.createSnapshot(astForest.getSourceRoot());
        try {
            // 生成DDL
            String ddl = ddlService.generateDdl(forms);
            Path ddlSql = astForest.getSourceRoot().resolve("../../../../sql/ddl.sql");
            log.info("build ddl.sql, path={}", ddlSql.normalize());
            FileFlush.build(ddlSql.toFile(), ddl).flush();

            // 生成持久层
            persistenceGeneratorConfig.setJdbcUrl(null).setDdl(ddl).setEnableGenerateDesign(true);
            PersistenceGenerator.Retval persistenceGeneratorRetval = persistenceGenerator.process();
            persistenceGeneratorRetval.getFlushes().forEach(FileFlush::flush);
            AstForestContext.set(astForest.cloneWithResetting());

            // 生成枚举
            enumService.generateEnums(forms).forEach(FileFlush::flush);
            AstForestContext.set(astForest.cloneWithResetting());

            // 生成controller和initDec
            for (FormDef form : forms) {
                CompilationUnit cu = CompilationUnitUtils.newBaseCurrentAstForest();
                String controllerName = MoreStringUtils.toUpperCamel(form.getName()) + "Controller";
                Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getSourceRoot(),
                        commonConfig.getControllerPackage(), controllerName + ".java");
                cu.setStorage(absulutePath);
                cu.setPackageDeclaration(commonConfig.getControllerPackage());
                cu.addImport(commonConfig.getDesignPackage() + ".*");
                cu.addImport(commonConfig.getEntityPackage() + ".*");
                cu.addImport(commonConfig.getEnumPackage() + ".*");
                cu.addImport("java.util.stream.*");
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                JavadocUtils.setJavadoc(coid, form.getTitle(), commonConfig.getAuthor());
                coid.addAnnotation(annotationExprService.springRestController());
                coid.setPublic(true).setName(controllerName);
                cu.addType(coid);
                coid.addMember(saveApiService.generateSaveInitDec(form));
                coid.addMember(listApiService.generateListInitDec(form));
                coid.addMember(getDetailApiService.generateGetDetailInitDec(form));
                coid.addMember(deleteApiService.generateDeleteInitDec(form));
                FileFlush.build(cu).flush();
            }
            AstForestContext.set(astForest.cloneWithResetting());

            // 调用handler-transformer转换initDec
            handlerTransformer.process(AstForestContext.get());
            AstForestContext.set(astForest.cloneWithResetting());

            snapshot.cleanup();
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        } catch (Exception e) {
            log.error("Error occurred during processing", e);
            FileSnapshotUtils.rollback(snapshot);
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
            bizId.setSpecialItemType(SpecialItemType.BIZ_ID);
            bizId.setMaxLength(36);
            form.getItems().add(0, bizId);
            TimeItemDef createdAt = new TimeItemDef();
            createdAt.setName("createdAt");
            createdAt.setTitle("创建时间");
            createdAt.setIsNonVoid(true);
            createdAt.setInitPattern(TODO);
            createdAt.setEditPattern(InitOrEditPattern.DO_NOT);
            bizId.setSpecialItemType(SpecialItemType.CREATED_AT);
            form.getItems().add(createdAt);
            TimeItemDef updatedAt = new TimeItemDef();
            updatedAt.setName("updatedAt");
            updatedAt.setTitle("更新时间");
            updatedAt.setIsNonVoid(true);
            updatedAt.setInitPattern(TODO);
            updatedAt.setEditPattern(TODO);
            bizId.setSpecialItemType(SpecialItemType.UPDATED_AT);
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