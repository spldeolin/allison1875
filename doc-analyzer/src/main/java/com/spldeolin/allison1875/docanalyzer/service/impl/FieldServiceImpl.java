package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.io.File;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeFieldVarsRetval;
import com.spldeolin.allison1875.docanalyzer.service.EnumService;
import com.spldeolin.allison1875.docanalyzer.service.FieldService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-23
 */
@Singleton
@Slf4j
public class FieldServiceImpl implements FieldService {

    @Inject
    private DocAnalyzerConfig config;

    @Inject
    private EnumService enumService;

    @Override
    public Table<String, String, AnalyzeFieldVarsRetval> analyzeFieldVars() {
        // 枚举项和jpdvs的分析范围
        Set<File> analyzsisJavaFiles = this.buildAnalysisScope();

        Table<String, String, AnalyzeFieldVarsRetval> result = HashBasedTable.create();
        for (File javaFile : analyzsisJavaFiles) {
            CompilationUnit cu = CompilationUnitUtils.parseJava(javaFile);
            for (ClassOrInterfaceDeclaration coid : cu.findAll(ClassOrInterfaceDeclaration.class,
                    coid -> coid.getFullyQualifiedName().isPresent())) {
                // 忽略qulifier不存在因为coid可能是一个声明在class内部的class（比如handler-transformer转化前initDec中的类）
                String coidQualifier = coid.getFullyQualifiedName()
                        .orElseThrow(() -> new Allison1875Exception("Node '" + coid.getName() + "' has no Qualifier"));
                for (FieldDeclaration field : coid.getFields()) {
                    List<String> fieldCommentLines = this.ananlyzeFieldCommentLines(field);
                    String deprecatedDescription = this.analyzeDeprecatedDescription(field);
                    String sinceVersion = this.analyzeSinceVersion(field);
                    for (VariableDeclarator fieldVar : field.getVariables()) {
                        AnalyzeFieldVarsRetval dto = new AnalyzeFieldVarsRetval();
                        String fieldVarName = fieldVar.getNameAsString();
                        dto.getCommentLines().addAll(fieldCommentLines);
                        dto.setDeprecatedDescription(deprecatedDescription);
                        dto.setSinceVersion(sinceVersion);
                        dto.getAnalyzeEnumConstantsRetvals().addAll(enumService.analyzeEnumConstants(fieldVar));
                        dto.getMoreDocLines().addAll(this.analyzeMoreAndGenerateDoc(field, fieldVar));
                        result.put(coidQualifier, fieldVarName, dto);
                    }
                }
            }
        }

        result.putAll(this.getAnalyzeFieldVarsRetvalFromThirdParty());

        return result;
    }

    protected String analyzeSinceVersion(FieldDeclaration field) {
        List<String> lines = JavadocUtils.getTagDescriptionAsLines(field, Type.SINCE, null);
        return Joiner.on(System.lineSeparator()).join(lines);
    }

    protected String analyzeDeprecatedDescription(FieldDeclaration field) {
        List<String> lines = JavadocUtils.getTagDescriptionAsLines(field, Type.DEPRECATED, null);
        return Joiner.on(System.lineSeparator()).join(lines);
    }

    protected Set<File> buildAnalysisScope() {
        Set<File> retval = Sets.newLinkedHashSet();
        FileUtils.iterateFiles(AstForestContext.get().cloneWithResetting().getSourceRoot().toFile(),
                BaseConstant.JAVA_EXTENSIONS, true).forEachRemaining(retval::add);
        // dependent dirs or javas
        for (File dirOrJavaFile : config.getDependencyDirsOrJavaFilePath()) {
            if (dirOrJavaFile.exists()) {
                if (dirOrJavaFile.isDirectory()) {
                    FileUtils.iterateFiles(dirOrJavaFile, BaseConstant.JAVA_EXTENSIONS, true)
                            .forEachRemaining(retval::add);
                }
                if (FilenameUtils.getExtension(dirOrJavaFile.getPath()).equals(BaseConstant.JAVA_EXTENSIONS[0])) {
                    retval.add(dirOrJavaFile);
                }
            }
        }
        if (!retval.isEmpty()) {
            log.info("{} external java files found in {}", retval.size(), config.getDependencyDirsOrJavaFilePath());
        }
        return retval;
    }

    protected List<String> ananlyzeFieldCommentLines(FieldDeclaration field) {
        // 可拓展为分析Swagger注解等
        return JavadocUtils.getDescriptionAsLines(field);
    }

    protected Table<String, String, AnalyzeFieldVarsRetval> getAnalyzeFieldVarsRetvalFromThirdParty() {
        return HashBasedTable.create();
    }

    protected List<String> analyzeMoreAndGenerateDoc(FieldDeclaration field, VariableDeclarator fieldVar) {
        return Lists.newArrayList();
    }

}