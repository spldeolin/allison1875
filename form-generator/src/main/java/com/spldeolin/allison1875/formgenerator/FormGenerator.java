package com.spldeolin.allison1875.formgenerator;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern.TODO;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MavenProjectClassLoaderUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzer;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.SpecialItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.DdlService;
import com.spldeolin.allison1875.formgenerator.service.DeleteApiService;
import com.spldeolin.allison1875.formgenerator.service.EnumService;
import com.spldeolin.allison1875.formgenerator.service.GetDetailApiService;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import com.spldeolin.allison1875.formgenerator.service.SaveApiService;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformer;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGenerator;
import com.spldeolin.allison1875.querytransformer.QueryTransformer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Slf4j
public class FormGenerator implements Allison1875MainService {

    @Inject
    private Config config;

    @Inject
    private PersistenceGenerator persistenceGenerator;

    @Inject
    private HandlerTransformer handlerTransformer;

    @Inject
    private DocAnalyzer docAnalyzer;

    @Inject
    private QueryTransformer queryTransformer;

    @Inject
    private AnnotationExprService annotationExprService;

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
    public void process() {
        List<FormDef> forms = deserializeDSL();
        if (CollectionUtils.isEmpty(forms)) {
            log.warn("no form definitions detected");
            return;
        }

        // 为每个Form增加业务主键、审计字段等
        addCommonItems(forms);

        // 生成DDL
        String ddl = ddlService.generateDdl(forms);
        Path ddlSql = Paths.get(DomainContext.get().getPersistenceModule()).resolve("sql/ddl.sql");
        log.info("build ddl.sql, path={}", ddlSql.normalize());
        try {
            Files.createDirectories(ddlSql.getParent());
            Files.writeString(ddlSql, ddl, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // 生成持久层
        config.setJdbcUrl(null);
        config.setDdl(ddl);
        config.setEnableGenerateDesign(true);
        persistenceGenerator.process();

        // 生成枚举
        enumService.generateEnums(forms);

        // 生成controller和initDec
        List<String> controllerQualifiers = Lists.newArrayList();
        for (FormDef form : forms) {
            CompilationUnit cu = new CompilationUnit();
            String controllerName = MoreStringUtils.toUpperCamel(form.getName()) + "Controller";
            Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(
                    DomainContext.get().getControllerSourceRoot(), DomainContext.get().getControllerPackage(),
                    controllerName + ".java");
            cu.setStorage(absulutePath);
            cu.setPackageDeclaration(DomainContext.get().getControllerPackage());
            cu.addImport(DomainContext.get().getDesignPackage() + ".*");
            cu.addImport(DomainContext.get().getEntityPackage() + ".*");
            if (hasSelectItem(form)) {
                // 因没有选择字段而不生成枚举时，这个包可能是不存在的，会导致编译错误，也无需导入，所以此处基于是否有选择字段进行判断
                cu.addImport(DomainContext.get().getEnumPackage() + ".*");
            }
            cu.addImport("java.util.*");
            cu.addImport("java.time.*");
            cu.addImport("java.math.*");
            cu.addImport("com.fasterxml.jackson.annotation.*");
            cu.addImport("java.util.stream.*");
            cu.addImport("org.springframework.util.*");
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            JavadocUtils.setJavadoc(coid, form.getTitle(), config.getAuthor());
            coid.addAnnotation(annotationExprService.springRestController());
            coid.addAnnotation(annotationExprService.springRequestMapping(
                    config.getCodeSnippet().getControllerRequestMapping().replace("${formName}", form.getVarName())));
            coid.setPublic(true).setName(controllerName);
            cu.addType(coid);
            coid.addMember(saveApiService.generateSaveInitDec(form));
            coid.addMember(listApiService.generateListInitDec(form));
            coid.addMember(getDetailApiService.generateGetDetailInitDec(form));
            coid.addMember(deleteApiService.generateDeleteInitDec(form));
            CompilationUnitUtils.writeJava(cu);
            controllerQualifiers.add(DomainContext.get().getControllerPackage() + "." + controllerName + ".*");
        }

        // 调用handler-transformer转换initDec
        handlerTransformer.process();

        // 编译controllerModule
        log.info("call MavenProjectClassLoaderUtils.compile for controllerModule");
        MavenProjectClassLoaderUtils.compile(new File(DomainContext.get().getControllerModule()), config.getJavaHome());

        // 调用query-transformer转换Design Chain
        queryTransformer.process();

        // 调用doc-analyzer分析接口文档
        if (config.getEnableDocAnalyzer()) {
            config.setMvcHandlerQualifierWildcards(controllerQualifiers);
            docAnalyzer.process();
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
            if (form.getIndices() == null) {
                form.setIndices(Lists.newArrayList());
            }
            form.getIndices().addFirst(index);
        }
    }

    private boolean hasSelectItem(FormDef form) {
        for (ItemDef item : form.getItems()) {
            if (item.getType() == ItemType.SELECT || item.getType() == ItemType.MULTI_SELECT) {
                return true;
            }
        }
        return false;
    }

    private List<FormDef> deserializeDSL() {
        try {
            return new YAMLMapper().readValue(Files.readString(config.getDslPath().toPath(), StandardCharsets.UTF_8),
                    new TypeReference<List<FormDef>>() {
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}