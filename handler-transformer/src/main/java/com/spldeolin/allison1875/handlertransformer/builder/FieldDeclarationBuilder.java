package com.spldeolin.allison1875.handlertransformer.builder;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.util.ValidateUtils;

/**
 * @author Deolin 2020-12-26
 */
public class FieldDeclarationBuilder {

    private Javadoc javadoc;

    private final LinkedHashSet<AnnotationExpr> annotationExprs = Sets.newLinkedHashSet();

    @NotNull
    private Type type;

    @NotBlank
    private String fieldName;

    public FieldDeclarationBuilder javadoc(String javadocDescription) {
        javadocDescription = MoreObjects.firstNonNull(javadocDescription, "");
        Javadoc javadoc = new JavadocComment(javadocDescription).parse();
        this.javadoc = javadoc;
        return this;
    }

    public FieldDeclarationBuilder javadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public FieldDeclarationBuilder annotationExpr(AnnotationExpr annotationExpr) {
        annotationExprs.add(annotationExpr);
        return this;
    }

    public FieldDeclarationBuilder annotationExpr(String annotation, String... var) {
        annotationExprs.add(StaticJavaParser.parseAnnotation(String.format(annotation, (Object) var)));
        return this;
    }

    public FieldDeclarationBuilder type(Type type) {
        this.type = type;
        return this;
    }

    public FieldDeclarationBuilder type(String type, String... vars) {
        this.type = StaticJavaParser.parseType(String.format(type, (Object) vars));
        return this;
    }

    public FieldDeclarationBuilder fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public FieldDeclaration build() {
        Set<ConstraintViolation<FieldDeclarationBuilder>> violations = ValidateUtils.VALIDATOR.validate(this);
        if (violations.size() > 0) {
            throw new IllegalArgumentException(violations.toString());
        }

        FieldDeclaration result = new FieldDeclaration();
        // Field级Javadoc
        if (javadoc != null) {
            result.setJavadocComment(javadoc);
        }
        // Field级注解
        for (AnnotationExpr annotationExpr : annotationExprs) {
            result.addAnnotation(annotationExpr);
        }
        // Field
        result.setPrivate(true).addVariable(new VariableDeclarator(type, fieldName));
        return result;
    }

}