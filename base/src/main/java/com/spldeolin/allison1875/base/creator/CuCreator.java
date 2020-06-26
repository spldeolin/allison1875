package com.spldeolin.allison1875.base.creator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import lombok.Data;

/**
 * @author Deolin 2020-01-25
 */
@Data
public class CuCreator {

    private CompilationUnit cu;

    private TypeDeclaration<?> pt;

    private String primaryTypeName;

    private String primaryTypeQualifier;

    private String primaryTypeInstanceName;

    private final Path sourceRoot;

    private final String packageName;

    private final Collection<ImportDeclaration> imports;

    private final TypeCreator primaryTypeCreator;

    public CuCreator(Path sourceRoot, String packageName, Collection<ImportDeclaration> imports,
            TypeCreator primaryTypeCreator) {
        this.sourceRoot = sourceRoot;
        this.packageName = packageName;
        this.imports = imports;
        this.primaryTypeCreator = primaryTypeCreator;
    }

    public CompilationUnit create(boolean saveNow) {
        Path storage = CodeGenerationUtils.packageAbsolutePath(sourceRoot, packageName);
        TypeDeclaration<?> primaryType = primaryTypeCreator.createType();
        pt = primaryType;
        primaryTypeName = primaryType.getNameAsString();
        primaryTypeQualifier = packageName + "." + primaryTypeName;
        primaryTypeInstanceName = StringUtils.lowerFirstLetter(primaryTypeName);

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(storage.resolve(primaryTypeName + ".java"), StandardCharsets.UTF_8);
        cu.setPackageDeclaration(packageName);
        imports.forEach(cu::addImport);
        cu.addType(primaryType);

        if (saveNow) {
            Saves.prettySave(cu);
        }

        this.cu = cu;
        return cu;
    }

    public interface TypeCreator {

        TypeDeclaration<?> createType();

    }

}
