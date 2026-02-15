package com.spldeolin.allison1875.formgenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.DdlService;
import com.spldeolin.allison1875.formgenerator.service.InitDecService;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformer;
import com.spldeolin.allison1875.handlertransformer.config.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.service.impl.ServiceLayerExpansionServiceImplManager;
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
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private HandlerTransformer handlerTransformer;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private InitDecService initDecService;

    @Inject
    private ServiceLayerExpansionServiceImplManager serviceMethodServiceImplManager;

    @Inject
    private FormGeneratorServiceLayerExpansionServiceImpl formGeneratorServiceLayerExpansionService;

    @Inject
    private DdlService ddlService;

    @Override
    public void process(AstForest astForest) {
        List<FormDef> forms = deserializeDSL();
        if (CollectionUtils.isEmpty(forms)) {
            log.warn("no form definitions detected");
            return;
        }

        List<FileFlush> flushes = Lists.newArrayList(); // 多组件flushes合成为一个

        // 生成DDL
        String ddl = ddlService.generateDdl(forms);
        Path ddlSql = astForest.getSourceRoot().resolve("../../../../sql/ddl.sql");
        log.info("build ddl.sql, path={}", ddlSql.normalize());
        flushes.add(FileFlush.build(ddlSql.toFile(), ddl));

        // 调用persistence-generator
        persistenceGeneratorConfig.setDdl(ddl);
        persistenceGeneratorConfig.setEnableGenerateDesign(false);
        flushes.addAll(persistenceGenerator.process());

        List<CompilationUnit> astForestWithUnflushedCus = Lists.newArrayList(astForest);
        for (FormDef formDef : forms) {

            // 生成controller和initDec
            CompilationUnit cu = CompilationUnitUtils.newBaseCurrentAstForest();
            String controllerName = MoreStringUtils.toUpperCamel(formDef.getName()) + "Controller";
            Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getSourceRoot(),
                    commonConfig.getControllerPackage(), controllerName + ".java");
            cu.setStorage(absulutePath);
            cu.setPackageDeclaration(commonConfig.getControllerPackage());
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            JavadocUtils.setJavadoc(coid, formDef.getTitle(), "ballcat");
            coid.addAnnotation(annotationExprService.springRestController());
            coid.setPublic(true).setName(controllerName);
            cu.addType(coid);

            // 生成CURD接口的initDec
            coid.addMember(initDecService.buildCreateHandler(formDef));
            coid.addMember(initDecService.buildListHandler(formDef));
            coid.addMember(initDecService.buildGetDetailHandler(formDef));
            coid.addMember(initDecService.buildUpdateHandler(formDef));
            coid.addMember(initDecService.buildDeleteHandler(formDef));

            astForestWithUnflushedCus.add(cu);
        }

        // 运行时替换form-generator中ServiceMethodService的实现类
        serviceMethodServiceImplManager.setCurrentImpl(formGeneratorServiceLayerExpansionService);

        // 调用handler-transformer转换initDec
        flushes.addAll(handlerTransformer.process(astForestWithUnflushedCus));

        // write all to file
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
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