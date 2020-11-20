package com.spldeolin.allison1875.base.creator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;

/**
 * @author Deolin 2020-01-25
 */
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

    public CuCreator(Path sourceRoot, String packageName, List<String> imports, TypeCreator primaryTypeCreator) {
        this.sourceRoot = sourceRoot;
        this.packageName = packageName;
        this.imports = imports.stream().map(one -> new ImportDeclaration(one, false, false))
                .collect(Collectors.toList());
        this.primaryTypeCreator = primaryTypeCreator;
    }

    public CuCreator(Path sourceRoot, String packageName, Collection<ImportDeclaration> imports,
            TypeCreator primaryTypeCreator) {
        this.sourceRoot = sourceRoot;
        this.packageName = packageName;
        this.imports = imports;
        this.primaryTypeCreator = primaryTypeCreator;
    }

    public CompilationUnit create(boolean saveNow) {
        if (this.cu == null) {
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
            this.cu = cu;
        }

        if (saveNow) {
            Saves.save(cu);
        }

        return cu;
    }

    public CompilationUnit getCu() {
        return this.cu;
    }

    public TypeDeclaration<?> getPt() {
        return this.pt;
    }

    public String getPrimaryTypeName() {
        return this.primaryTypeName;
    }

    public String getPrimaryTypeQualifier() {
        return this.primaryTypeQualifier;
    }

    public String getPrimaryTypeInstanceName() {
        return this.primaryTypeInstanceName;
    }

    public Path getSourceRoot() {
        return this.sourceRoot;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public Collection<ImportDeclaration> getImports() {
        return this.imports;
    }

    public TypeCreator getPrimaryTypeCreator() {
        return this.primaryTypeCreator;
    }

    public void setCu(CompilationUnit cu) {
        this.cu = cu;
    }

    public void setPt(TypeDeclaration<?> pt) {
        this.pt = pt;
    }

    public void setPrimaryTypeName(String primaryTypeName) {
        this.primaryTypeName = primaryTypeName;
    }

    public void setPrimaryTypeQualifier(String primaryTypeQualifier) {
        this.primaryTypeQualifier = primaryTypeQualifier;
    }

    public void setPrimaryTypeInstanceName(String primaryTypeInstanceName) {
        this.primaryTypeInstanceName = primaryTypeInstanceName;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CuCreator)) {
            return false;
        }
        final CuCreator other = (CuCreator) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$cu = this.getCu();
        final Object other$cu = other.getCu();
        if (this$cu == null ? other$cu != null : !this$cu.equals(other$cu)) {
            return false;
        }
        final Object this$pt = this.getPt();
        final Object other$pt = other.getPt();
        if (this$pt == null ? other$pt != null : !this$pt.equals(other$pt)) {
            return false;
        }
        final Object this$primaryTypeName = this.getPrimaryTypeName();
        final Object other$primaryTypeName = other.getPrimaryTypeName();
        if (this$primaryTypeName == null ? other$primaryTypeName != null
                : !this$primaryTypeName.equals(other$primaryTypeName)) {
            return false;
        }
        final Object this$primaryTypeQualifier = this.getPrimaryTypeQualifier();
        final Object other$primaryTypeQualifier = other.getPrimaryTypeQualifier();
        if (this$primaryTypeQualifier == null ? other$primaryTypeQualifier != null
                : !this$primaryTypeQualifier.equals(other$primaryTypeQualifier)) {
            return false;
        }
        final Object this$primaryTypeInstanceName = this.getPrimaryTypeInstanceName();
        final Object other$primaryTypeInstanceName = other.getPrimaryTypeInstanceName();
        if (this$primaryTypeInstanceName == null ? other$primaryTypeInstanceName != null
                : !this$primaryTypeInstanceName.equals(other$primaryTypeInstanceName)) {
            return false;
        }
        final Object this$sourceRoot = this.getSourceRoot();
        final Object other$sourceRoot = other.getSourceRoot();
        if (this$sourceRoot == null ? other$sourceRoot != null : !this$sourceRoot.equals(other$sourceRoot)) {
            return false;
        }
        final Object this$packageName = this.getPackageName();
        final Object other$packageName = other.getPackageName();
        if (this$packageName == null ? other$packageName != null : !this$packageName.equals(other$packageName)) {
            return false;
        }
        final Object this$imports = this.getImports();
        final Object other$imports = other.getImports();
        if (this$imports == null ? other$imports != null : !this$imports.equals(other$imports)) {
            return false;
        }
        final Object this$primaryTypeCreator = this.getPrimaryTypeCreator();
        final Object other$primaryTypeCreator = other.getPrimaryTypeCreator();
        return this$primaryTypeCreator == null ? other$primaryTypeCreator == null
                : this$primaryTypeCreator.equals(other$primaryTypeCreator);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CuCreator;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $cu = this.getCu();
        result = result * PRIME + ($cu == null ? 43 : $cu.hashCode());
        final Object $pt = this.getPt();
        result = result * PRIME + ($pt == null ? 43 : $pt.hashCode());
        final Object $primaryTypeName = this.getPrimaryTypeName();
        result = result * PRIME + ($primaryTypeName == null ? 43 : $primaryTypeName.hashCode());
        final Object $primaryTypeQualifier = this.getPrimaryTypeQualifier();
        result = result * PRIME + ($primaryTypeQualifier == null ? 43 : $primaryTypeQualifier.hashCode());
        final Object $primaryTypeInstanceName = this.getPrimaryTypeInstanceName();
        result = result * PRIME + ($primaryTypeInstanceName == null ? 43 : $primaryTypeInstanceName.hashCode());
        final Object $sourceRoot = this.getSourceRoot();
        result = result * PRIME + ($sourceRoot == null ? 43 : $sourceRoot.hashCode());
        final Object $packageName = this.getPackageName();
        result = result * PRIME + ($packageName == null ? 43 : $packageName.hashCode());
        final Object $imports = this.getImports();
        result = result * PRIME + ($imports == null ? 43 : $imports.hashCode());
        final Object $primaryTypeCreator = this.getPrimaryTypeCreator();
        result = result * PRIME + ($primaryTypeCreator == null ? 43 : $primaryTypeCreator.hashCode());
        return result;
    }

    public String toString() {
        return "CuCreator(cu=" + this.getCu() + ", pt=" + this.getPt() + ", primaryTypeName=" + this
                .getPrimaryTypeName() + ", primaryTypeQualifier=" + this.getPrimaryTypeQualifier()
                + ", primaryTypeInstanceName=" + this.getPrimaryTypeInstanceName() + ", sourceRoot=" + this
                .getSourceRoot() + ", packageName=" + this.getPackageName() + ", imports=" + this.getImports()
                + ", primaryTypeCreator=" + this.getPrimaryTypeCreator() + ")";
    }

    public interface TypeCreator {

        TypeDeclaration<?> createType();

    }

}
