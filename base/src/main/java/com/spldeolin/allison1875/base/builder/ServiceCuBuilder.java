package com.spldeolin.allison1875.base.builder;

import java.nio.file.Path;
import java.util.Collection;
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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.google.mu.util.Substring;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ValidateUtils;

/**
 * @author Deolin 2021-01-10
 */
public class ServiceCuBuilder {

    private SourceRoot sourceRoot;

    private PackageDeclaration packageDeclaration;

    private final Set<ImportDeclaration> importDeclarations = Sets.newLinkedHashSet();

    private Javadoc javadoc;

    private final Set<AnnotationExpr> annotationExprs = Sets.newLinkedHashSet();

    private String serviceName;

    private MethodDeclaration method;

    private ClassOrInterfaceDeclaration service;

    private String serviceQualifier;

    private String serviceVarName;

    private String methodName;

    public ServiceCuBuilder sourceRoot(SourceRoot sourceRoot) {
        Objects.requireNonNull(sourceRoot, "sourceRoot cannot be null.");
        this.sourceRoot = sourceRoot;
        return this;
    }

    public ServiceCuBuilder sourceRoot(Path sourceRootPath) {
        Objects.requireNonNull(sourceRootPath, "sourceRootPath cannot be null.");
        this.sourceRoot = new SourceRoot(sourceRootPath);
        return this;
    }

    public ServiceCuBuilder packageDeclaration(String packageName) {
        Objects.requireNonNull(packageName, "packageName cannot be null.");
        this.packageDeclaration = new PackageDeclaration().setName(packageName);
        return this;
    }

    public ServiceCuBuilder packageDeclaration(PackageDeclaration packageDeclaration) {
        this.packageDeclaration = packageDeclaration;
        return this;
    }

    public ServiceCuBuilder importDeclaration(String importName, Boolean isAsterisk) {
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

    public ServiceCuBuilder importDeclaration(ImportDeclaration importDeclaration) {
        Objects.requireNonNull(importDeclaration, "importDeclaration cannot be null.");
        importDeclarations.add(importDeclaration);
        return this;
    }

    public ServiceCuBuilder importDeclaration(String importName) {
        if (importName.endsWith(".*")) {
            importDeclarations.add(new ImportDeclaration(Substring.last(".*").removeFrom(importName), false, true));
        } else {
            importDeclarations.add(new ImportDeclaration(importName, false, false));
        }
        return this;
    }

    public ServiceCuBuilder importDeclarationsString(Collection<String> importNames) {
        importNames.forEach(this::importDeclaration);
        return this;
    }

    public ServiceCuBuilder importDeclarations(Collection<ImportDeclaration> importDeclarations) {
        importDeclarations.forEach(this::importDeclaration);
        return this;
    }

    public ServiceCuBuilder javadoc(String javadocDescription, String author) {
        if (StringUtils.isBlank(author)) {
            throw new IllegalArgumentException("author cannot be blank.");
        }
        javadocDescription = MoreObjects.firstNonNull(javadocDescription, "");
        Javadoc javadoc = new JavadocComment(javadocDescription).parse()
                .addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, author));
        this.javadoc = javadoc;
        return this;
    }

    public ServiceCuBuilder javadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public ServiceCuBuilder annotationExpr(AnnotationExpr annotationExpr) {
        annotationExprs.add(annotationExpr);
        return this;
    }

    public ServiceCuBuilder annotationExpr(String annotation, String... var) {
        annotationExprs.add(StaticJavaParser.parseAnnotation(String.format(annotation, (Object) var)));
        return this;
    }

    public ServiceCuBuilder serviceName(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName cannot be null.");
        this.serviceName = serviceName;
        return this;
    }

    public ServiceCuBuilder method(MethodDeclaration method) {
        Objects.requireNonNull(serviceName, "method cannot be null.");
        this.method = method;
        return this;
    }

    public CompilationUnit build() {
        Set<ConstraintViolation<ServiceCuBuilder>> violations = ValidateUtils.validate(this);
        if (violations.size() > 0) {
            throw new IllegalArgumentException(violations.toString());
        }

        CompilationUnit result = new CompilationUnit();

        // package声明
        result.setPackageDeclaration(packageDeclaration);

        // import声明
        result.setImports(new NodeList<>(importDeclarations));

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();

        // 类级Javadoc
        if (javadoc != null) {
            coid.setJavadocComment(javadoc);
        }

        // 类级注解
        annotationExprs.forEach(coid::addAnnotation);

        // 类签名
        coid.setPublic(true).setStatic(false).setInterface(true).setName(serviceName);

        // 方法签名
        MethodDeclaration method = new MethodDeclaration().setType(this.method.getType()).setName(this.method.getName())
                .setParameters(this.method.getParameters());
        method.setBody(null);
        coid.addMember(method);

        result.setTypes(new NodeList<>(coid));

        // CU的路径
        Path storage = sourceRoot.getRoot();
        if (packageDeclaration != null) {
            storage = storage.resolve(CodeGenerationUtils.packageToPath(packageDeclaration.getNameAsString()));
        }
        storage = storage.resolve(coid.getNameAsString() + ".java");
        result.setStorage(storage);

        this.service = coid;
        this.serviceQualifier = coid.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        this.serviceVarName = MoreStringUtils.lowerFirstLetter(serviceName);
        this.methodName = method.getNameAsString();

        return result;
    }

    public ClassOrInterfaceDeclaration getJavabean() {
        if (service == null) {
            throw new IllegalStateException("build() not yet.");
        }
        return service;
    }

    public String getJavabeanQualifier() {
        if (service == null) {
            throw new IllegalStateException("build() not yet.");
        }
        return serviceQualifier;
    }

    public String getServiceVarName() {
        if (service == null) {
            throw new IllegalArgumentException("build() not yet.");
        }
        return serviceVarName;
    }

    public String getMethodName() {
        if (service == null) {
            throw new IllegalArgumentException("build() not yet.");
        }
        return methodName;
    }

}