package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.io.File;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MavenUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
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

    @Override
    public Table<String, String, JsonPropertyDescriptionValueDto> analyzeFieldVars(AstForest astForest) {
        // jpdvs的分析范围
        Set<File> analyzsisJavaFiles = this.buildAnalysisScope(astForest);

        Table<String, String, JsonPropertyDescriptionValueDto> jpdvs = HashBasedTable.create();
        for (File javaFile : analyzsisJavaFiles) {
            CompilationUnit cu = CompilationUnitUtils.parseJava(javaFile);
            for (ClassOrInterfaceDeclaration coid : cu.findAll(ClassOrInterfaceDeclaration.class,
                    coid -> coid.getFullyQualifiedName().isPresent())) {
                // 忽略qulifier不存在因为coid可能是一个声明在class内部的class（比如handler-transformer转化前initDec中的类）
                String coidQualifier = coid.getFullyQualifiedName()
                        .orElseThrow(() -> new QualifierAbsentException(coid));
                for (FieldDeclaration field : coid.getFields()) {
                    List<String> fieldCommentLines = this.ananlyzeFieldCommentLines(field);
                    for (VariableDeclarator fieldVar : field.getVariables()) {
                        JsonPropertyDescriptionValueDto jpdv = new JsonPropertyDescriptionValueDto();
                        String fieldVarName = fieldVar.getNameAsString();
                        jpdv.setDescriptionLines(fieldCommentLines);
                        jpdvs.put(coidQualifier, fieldVarName, jpdv);
                    }
                }
            }
        }
        return null;
    }

    protected Set<File> buildAnalysisScope(AstForest astForest) {
        Set<File> result = Sets.newLinkedHashSet();
        // maven project
        FileUtils.iterateFiles(MavenUtils.findMavenProject(astForest.getPrimaryClass()), BaseConstant.JAVA_EXTENSIONS,
                true).forEachRemaining(result::add);
        // dependent projects
        for (File dependencyProjectDirectory : config.getDependencyProjectDirectories()) {
            FileUtils.iterateFiles(dependencyProjectDirectory, BaseConstant.JAVA_EXTENSIONS, true)
                    .forEachRemaining(result::add);
        }
        return result;
    }

    protected List<String> ananlyzeFieldCommentLines(FieldDeclaration field) {
        // 可拓展为分析Swagger注解等
        return JavadocUtils.getCommentAsLines(field);
    }

    protected boolean analyzeFieldIgnoreFlag(FieldDeclaration field) {
        for (String commentLine : this.ananlyzeFieldCommentLines(field)) {
            if (StringUtils.startsWithIgnoreCase(commentLine, "doc-ignore")) {
                return true;
            }
        }
        return false;
    }

}