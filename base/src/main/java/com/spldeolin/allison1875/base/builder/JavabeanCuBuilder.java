package com.spldeolin.allison1875.base.builder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.google.mu.util.Substring;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.ValidateUtils;

/**
 * @author Deolin 2020-12-26
 */
public class JavabeanCuBuilder {

    private SourceRoot sourceRoot;

    private PackageDeclaration packageDeclaration;

    private final Set<ImportDeclaration> importDeclarations = Sets.newLinkedHashSet();

    private ClassOrInterfaceDeclaration coid;

    private Javadoc javadoc;

    private final Set<AnnotationExpr> annotationExprs = Sets.newLinkedHashSet();

    private Boolean isFinal = false;

    private String javabeanName;

    private final LinkedHashSet<FieldDeclaration> fieldDeclarations = Sets.newLinkedHashSet();

    private ClassOrInterfaceDeclaration javabean;

    private String javabeanQualifier;

    public JavabeanCuBuilder sourceRoot(SourceRoot sourceRoot) {
        Objects.requireNonNull(sourceRoot, "sourceRoot cannot be null.");
        this.sourceRoot = sourceRoot;
        return this;
    }

    public JavabeanCuBuilder sourceRoot(Path sourceRootPath) {
        Objects.requireNonNull(sourceRootPath, "sourceRootPath cannot be null.");
        this.sourceRoot = new SourceRoot(sourceRootPath);
        return this;
    }

    public JavabeanCuBuilder packageDeclaration(String packageName) {
        Objects.requireNonNull(packageName, "packageName cannot be null.");
        this.packageDeclaration = new PackageDeclaration().setName(packageName);
        return this;
    }

    public JavabeanCuBuilder packageDeclaration(PackageDeclaration packageDeclaration) {
        this.packageDeclaration = packageDeclaration;
        return this;
    }

    public JavabeanCuBuilder importDeclaration(String importName, Boolean isAsterisk) {
        boolean endsWith = importName.endsWith(".*");
        if (isAsterisk && !endsWith) {
            importName = importName + ".*";
        }
        if (!isAsterisk && endsWith) {
            importName = Substring.last(".*").removeFrom(importName);
        }
        this.importDeclaration(importName);
        return this;
    }

    public JavabeanCuBuilder importDeclaration(ImportDeclaration importDeclaration) {
        Objects.requireNonNull(importDeclaration, "importDeclaration cannot be null.");
        importDeclarations.add(importDeclaration);
        return this;
    }

    public JavabeanCuBuilder importDeclaration(String importName) {
        if (importName.endsWith(".*")) {
            importDeclarations.add(new ImportDeclaration(Substring.last(".*").removeFrom(importName), false, true));
        } else {
            importDeclarations.add(new ImportDeclaration(importName, false, false));
        }
        return this;
    }

    public JavabeanCuBuilder importDeclarationsString(Collection<String> importNames) {
        importNames.forEach(this::importDeclaration);
        return this;
    }

    public JavabeanCuBuilder importDeclarations(Collection<ImportDeclaration> importDeclarations) {
        importDeclarations.forEach(this::importDeclaration);
        return this;
    }

    public JavabeanCuBuilder coid(ClassOrInterfaceDeclaration coid) {
        Objects.requireNonNull(coid, "coid cannot be null.");
        this.coid = coid;
        return this;
    }

    public JavabeanCuBuilder javadoc(String javadocDescription, String author) {
        if (StringUtils.isBlank(author)) {
            throw new IllegalArgumentException("author cannot be blank.");
        }
        javadocDescription = MoreObjects.firstNonNull(javadocDescription, "");
        Javadoc javadoc = new JavadocComment(javadocDescription).parse()
                .addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, author));
        this.javadoc = javadoc;
        return this;
    }

    public JavabeanCuBuilder javadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public JavabeanCuBuilder annotationExpr(AnnotationExpr annotationExpr) {
        annotationExprs.add(annotationExpr);
        return this;
    }

    public JavabeanCuBuilder annotationExpr(String annotation, String... var) {
        annotationExprs.add(StaticJavaParser.parseAnnotation(String.format(annotation, (Object) var)));
        return this;
    }

    public JavabeanCuBuilder isFinal(Boolean isFinal) {
        Objects.requireNonNull(isFinal, "isFinal cannot be null.");
        this.isFinal = isFinal;
        return this;
    }

    public JavabeanCuBuilder javabeanName(String javabeanName) {
        Objects.requireNonNull(javabeanName, "javabeanName cannot be null.");
        this.javabeanName = javabeanName;
        return this;
    }

    public JavabeanCuBuilder fieldDeclaration(FieldDeclaration fieldDeclaration) {
        this.fieldDeclarations.add(fieldDeclaration);
        return this;
    }

    public CompilationUnit build() {
        Set<ConstraintViolation<JavabeanCuBuilder>> violations = ValidateUtils.validate(this);
        if (violations.size() > 0) {
            throw new IllegalArgumentException(violations.toString());
        }

        CompilationUnit result = new CompilationUnit();

        // package声明
        result.setPackageDeclaration(packageDeclaration);

        // import声明
        result.setImports(new NodeList<>(importDeclarations));

        // coid
        ClassOrInterfaceDeclaration coid = this.coid;
        if (coid == null) {
            coid = new ClassOrInterfaceDeclaration();

            // 类级Javadoc
            if (javadoc != null) {
                coid.setJavadocComment(javadoc);
            }

            // 类级注解
            coid.addAnnotation(AnnotationConstant.DATA);
            annotationExprs.forEach(coid::addAnnotation);

            // 类签名
            coid.setPublic(true).setStatic(false).setFinal(isFinal).setInterface(false).setName(javabeanName);

            // Fileds
            fieldDeclarations.forEach(coid::addMember);
        }
        result.setTypes(new NodeList<>(coid));

        // CU的路径
        Path storage = sourceRoot.getRoot();
        if (packageDeclaration != null) {
            storage = storage.resolve(CodeGenerationUtils.packageToPath(packageDeclaration.getNameAsString()));
        }
        storage = storage.resolve(coid.getNameAsString() + ".java");
        result.setStorage(storage);

        this.javabean = coid;
        this.javabeanQualifier = coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);

        return result;
    }

    public ClassOrInterfaceDeclaration getJavabean() {
        if (javabean == null) {
            throw new IllegalStateException("build() not yet.");
        }
        return javabean;
    }

    public String getJavabeanQualifier() {
        if (javabean == null) {
            throw new IllegalStateException("build() not yet.");
        }
        return javabeanQualifier;
    }

}