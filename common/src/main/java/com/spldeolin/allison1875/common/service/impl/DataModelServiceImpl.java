package com.spldeolin.allison1875.common.service.impl;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Pair;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.dto.FieldArg;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-05-26
 */
@Singleton
@Slf4j
public class DataModelServiceImpl implements DataModelService {

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportExprService importExprService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public DataModelGeneration generateDataModel(DataModelArg arg) {
        String packageName = arg.getPackageName().trim();
        String className = arg.getClassName().trim();
        String description = MoreObjects.firstNonNull(arg.getDescription(), "").trim();
        Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(arg.getSourceRoot(),
                packageName, className + ".java");

        if (absulutePath.toFile().exists()) {
            if (arg.getDataModelExistenceResolution() == FileExistenceResolutionEnum.OVERWRITE) {
                log.info("Entity [{}] is exist, use [overwrite] resolution", className);
            } else if (arg.getDataModelExistenceResolution() == FileExistenceResolutionEnum.RENAME) {
                String oldClassName = className;
                log.info("Entity [{}] is exist, use [rename] resolution", oldClassName);
                absulutePath = antiDuplicationService.getNewPathIfExist(absulutePath);
                className = FilenameUtils.getBaseName(absulutePath.toString());
            } else {
                throw new Allison1875Exception(
                        "unknown FileExistenceResolutionEnum [" + arg.getDataModelExistenceResolution() + "]");
            }
        }

        CompilationUnit cu = StaticJavaParser.parse(""); // 采用parse("")的方式初始化empty cu，目的是设置符号解析器
        cu.setStorage(absulutePath);
        cu.setPackageDeclaration(packageName);

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
        coid.addAnnotation(annotationExprService.lombokData());
        coid.addAnnotation(annotationExprService.lombokAccessors());
        coid.addAnnotation(annotationExprService.lombokFieldDefaultsPrivate());
        coid.setPublic(true).setInterface(false).setName(className);
        JavadocUtils.setJavadoc(coid, description, arg.getAuthor().trim() + " " + LocalDate.now());
        cu.addType(coid);

        for (FieldArg fieldArg : arg.getFieldArgs()) {
            FieldDeclaration field = coid.addField(fieldArg.getTypeQualifier().trim(), fieldArg.getFieldName().trim());
            field.setJavadocComment(MoreObjects.firstNonNull(fieldArg.getDescription(), "").trim());
            // more for Field
            if (fieldArg.getMoreOperation() != null) {
                fieldArg.getMoreOperation().accept(coid, field);
            }
        }

        // more for DataModel
        if (arg.getMoreOperation() != null) {
            arg.getMoreOperation().accept(cu, coid);
        }

        importExprService.extractQualifiedTypeToImport(cu);
        CompilationUnitUtils.writeJava(cu);

        DataModelGeneration result = new DataModelGeneration();
        result.setCu(cu);
        result.setDtoName(className);
        result.setDtoQualifier(packageName + "." + className);
        result.setCoid(coid);
        result.setPath(absulutePath);
        return result;
    }

}
