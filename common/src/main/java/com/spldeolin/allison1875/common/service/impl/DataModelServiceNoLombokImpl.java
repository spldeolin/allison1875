package com.spldeolin.allison1875.common.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseBlock;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
public class DataModelServiceNoLombokImpl implements DataModelService {

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

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(absulutePath);
        cu.setPackageDeclaration(packageName);

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
        coid.setPublic(true).setInterface(false).setName(className);
        JavadocUtils.setJavadoc(coid, description, arg.getAuthor().trim() + " " + LocalDate.now());
        cu.addType(coid);

        // add fields
        for (FieldArg fieldArg : arg.getFieldArgs()) {
            FieldDeclaration field = coid.addField(fieldArg.getTypeQualifier().trim(), fieldArg.getFieldName().trim());
            field.setJavadocComment(MoreObjects.firstNonNull(fieldArg.getDescription(), "").trim()).setPrivate(true);
            // more for Field
            if (fieldArg.getMoreOperation() != null) {
                fieldArg.getMoreOperation().accept(coid, field);
            }
        }

        addGetterSetterToStringEqualsHashcode(arg.getFieldArgs(), coid);

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

    public void addGetterSetterToStringEqualsHashcode(List<FieldArg> fieldArgs, ClassOrInterfaceDeclaration coid) {
        // add getters
        for (FieldArg fieldArg : fieldArgs) {
            MethodDeclaration getter = new MethodDeclaration();
            if (StringUtils.isNotEmpty(fieldArg.getDescription())) {
                getter.setJavadocComment(fieldArg.getDescription().trim());
            }
            getter.setPublic(true).setType(fieldArg.getTypeQualifier())
                    .setName("get" + StringUtils.capitalize(fieldArg.getFieldName()));
            getter.setBody(parseBlock("{ return %s; }", fieldArg.getFieldName()));
            coid.addMember(getter);
        }

        // add setters
        for (FieldArg fieldArg : fieldArgs) {
            MethodDeclaration setter = new MethodDeclaration();
            if (StringUtils.isNotEmpty(fieldArg.getDescription())) {
                setter.setJavadocComment(fieldArg.getDescription().trim());
            }
            setter.setPublic(true).setType(coid.getNameAsString())
                    .setName("set" + StringUtils.capitalize(fieldArg.getFieldName()))
                    .addParameter(fieldArg.getTypeQualifier(), fieldArg.getFieldName());
            setter.setBody(parseBlock(
                    "{ this.%s = %s; return this; }", fieldArg.getFieldName(), fieldArg.getFieldName()));
            coid.addMember(setter);
        }

        // add toString
        MethodDeclaration toString = new MethodDeclaration();
        toString.addAnnotation(annotationExprService.javaOverride()).setPublic(true).setType("String")
                .setName("toString");
        StringBuilder addMces = new StringBuilder(256);
        for (FieldArg fieldArg : fieldArgs) {
            addMces.append(".add(\"").append(fieldArg.getFieldName()).append("=\" + ").append(fieldArg.getFieldName())
                    .append(")");
        }
        String toStringBody = String.format(
                "{ return new java.util.StringJoiner(\", \", %s.class.getSimpleName() + \"(\", \")\")%s.toString(); }",
                coid.getName(), addMces);
        toString.setBody(parseBlock(toStringBody));
        coid.addMember(toString);

        // add equals and hashCode
        MethodDeclaration equals = new MethodDeclaration();
        equals.addAnnotation(annotationExprService.javaOverride()).setPublic(true).setType("boolean").setName("equals")
                .addParameter("Object", "o");
        StringBuilder equalsBody = new StringBuilder(256);
        equalsBody.append("{ if (this == o) return true; ");
        equalsBody.append("if (o == null || getClass() != o.getClass()) return false; ");
        equalsBody.append(coid.getNameAsString()).append(" that = (").append(coid.getNameAsString()).append(") o; ");
        equalsBody.append("return ");
        if (fieldArgs.isEmpty()) {
            equalsBody.append("true");
        } else {
            for (int i = 0; i < fieldArgs.size(); i++) {
                if (i > 0) {
                    equalsBody.append(" && ");
                }
                equalsBody.append("java.util.Objects.equals(this.").append(fieldArgs.get(i).getFieldName())
                        .append(", that.").append(fieldArgs.get(i).getFieldName()).append(")");
            }
        }
        equalsBody.append("; }");
        equals.setBody(parseBlock(equalsBody.toString()));
        coid.addMember(equals);

        MethodDeclaration hashCode = new MethodDeclaration();
        hashCode.addAnnotation(annotationExprService.javaOverride()).setPublic(true).setType("int").setName("hashCode");
        StringBuilder hashCodeBody = new StringBuilder(128);
        hashCodeBody.append("{ return java.util.Objects.hash(");
        for (int i = 0; i < fieldArgs.size(); i++) {
            if (i > 0) {
                hashCodeBody.append(", ");
            }
            hashCodeBody.append(fieldArgs.get(i).getFieldName());
        }
        hashCodeBody.append("); }");
        hashCode.setBody(parseBlock(hashCodeBody.toString()));
        coid.addMember(hashCode);
    }

}
