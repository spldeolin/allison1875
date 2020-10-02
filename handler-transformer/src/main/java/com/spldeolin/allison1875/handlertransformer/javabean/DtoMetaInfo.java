package com.spldeolin.allison1875.handlertransformer.javabean;

import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.ImportDeclaration;
import com.google.common.collect.ImmutableList;

/**
 * @author Deolin 2020-06-26
 */
public class DtoMetaInfo {

    private final String packageName;

    private final String typeQualifier;

    private final String typeName;

    private final String dtoName;

    private final Pair<String, String> asVariableDeclarator;

    private final ImmutableList<ImportDeclaration> imports;

    private final ImmutableList<Pair<String, String>> variableDeclarators;

    DtoMetaInfo(String packageName, String typeQualifier, String typeName, String dtoName,
            Pair<String, String> asVariableDeclarator, ImmutableList<ImportDeclaration> imports,
            ImmutableList<Pair<String, String>> variableDeclarators) {
        this.packageName = packageName;
        this.typeQualifier = typeQualifier;
        this.typeName = typeName;
        this.dtoName = dtoName;
        this.asVariableDeclarator = asVariableDeclarator;
        this.imports = imports;
        this.variableDeclarators = variableDeclarators;
    }

    public static DtoMetaInfoBuilder builder() {
        return new DtoMetaInfoBuilder();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getTypeQualifier() {
        return this.typeQualifier;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public String getDtoName() {
        return this.dtoName;
    }

    public Pair<String, String> getAsVariableDeclarator() {
        return this.asVariableDeclarator;
    }

    public ImmutableList<ImportDeclaration> getImports() {
        return this.imports;
    }

    public ImmutableList<Pair<String, String>> getVariableDeclarators() {
        return this.variableDeclarators;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DtoMetaInfo)) {
            return false;
        }
        final DtoMetaInfo other = (DtoMetaInfo) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$packageName = this.getPackageName();
        final Object other$packageName = other.getPackageName();
        if (this$packageName == null ? other$packageName != null : !this$packageName.equals(other$packageName)) {
            return false;
        }
        final Object this$typeQualifier = this.getTypeQualifier();
        final Object other$typeQualifier = other.getTypeQualifier();
        if (this$typeQualifier == null ? other$typeQualifier != null
                : !this$typeQualifier.equals(other$typeQualifier)) {
            return false;
        }
        final Object this$typeName = this.getTypeName();
        final Object other$typeName = other.getTypeName();
        if (this$typeName == null ? other$typeName != null : !this$typeName.equals(other$typeName)) {
            return false;
        }
        final Object this$dtoName = this.getDtoName();
        final Object other$dtoName = other.getDtoName();
        if (this$dtoName == null ? other$dtoName != null : !this$dtoName.equals(other$dtoName)) {
            return false;
        }
        final Object this$asVariableDeclarator = this.getAsVariableDeclarator();
        final Object other$asVariableDeclarator = other.getAsVariableDeclarator();
        if (this$asVariableDeclarator == null ? other$asVariableDeclarator != null
                : !this$asVariableDeclarator.equals(other$asVariableDeclarator)) {
            return false;
        }
        final Object this$imports = this.getImports();
        final Object other$imports = other.getImports();
        if (this$imports == null ? other$imports != null : !this$imports.equals(other$imports)) {
            return false;
        }
        final Object this$variableDeclarators = this.getVariableDeclarators();
        final Object other$variableDeclarators = other.getVariableDeclarators();
        return this$variableDeclarators == null ? other$variableDeclarators == null
                : this$variableDeclarators.equals(other$variableDeclarators);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DtoMetaInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $packageName = this.getPackageName();
        result = result * PRIME + ($packageName == null ? 43 : $packageName.hashCode());
        final Object $typeQualifier = this.getTypeQualifier();
        result = result * PRIME + ($typeQualifier == null ? 43 : $typeQualifier.hashCode());
        final Object $typeName = this.getTypeName();
        result = result * PRIME + ($typeName == null ? 43 : $typeName.hashCode());
        final Object $dtoName = this.getDtoName();
        result = result * PRIME + ($dtoName == null ? 43 : $dtoName.hashCode());
        final Object $asVariableDeclarator = this.getAsVariableDeclarator();
        result = result * PRIME + ($asVariableDeclarator == null ? 43 : $asVariableDeclarator.hashCode());
        final Object $imports = this.getImports();
        result = result * PRIME + ($imports == null ? 43 : $imports.hashCode());
        final Object $variableDeclarators = this.getVariableDeclarators();
        result = result * PRIME + ($variableDeclarators == null ? 43 : $variableDeclarators.hashCode());
        return result;
    }

    public String toString() {
        return "DtoMetaInfo(packageName=" + this.getPackageName() + ", typeQualifier=" + this.getTypeQualifier()
                + ", typeName=" + this.getTypeName() + ", dtoName=" + this.getDtoName() + ", asVariableDeclarator="
                + this.getAsVariableDeclarator() + ", imports=" + this.getImports() + ", variableDeclarators=" + this
                .getVariableDeclarators() + ")";
    }

    public static class DtoMetaInfoBuilder {

        private String packageName;

        private String typeQualifier;

        private String typeName;

        private String dtoName;

        private Pair<String, String> asVariableDeclarator;

        private ImmutableList<ImportDeclaration> imports;

        private ImmutableList<Pair<String, String>> variableDeclarators;

        DtoMetaInfoBuilder() {
        }

        public DtoMetaInfo.DtoMetaInfoBuilder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public DtoMetaInfo.DtoMetaInfoBuilder typeQualifier(String typeQualifier) {
            this.typeQualifier = typeQualifier;
            return this;
        }

        public DtoMetaInfo.DtoMetaInfoBuilder typeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public DtoMetaInfo.DtoMetaInfoBuilder dtoName(String dtoName) {
            this.dtoName = dtoName;
            return this;
        }

        public DtoMetaInfo.DtoMetaInfoBuilder asVariableDeclarator(Pair<String, String> asVariableDeclarator) {
            this.asVariableDeclarator = asVariableDeclarator;
            return this;
        }

        public DtoMetaInfo.DtoMetaInfoBuilder imports(ImmutableList<ImportDeclaration> imports) {
            this.imports = imports;
            return this;
        }

        public DtoMetaInfo.DtoMetaInfoBuilder variableDeclarators(
                ImmutableList<Pair<String, String>> variableDeclarators) {
            this.variableDeclarators = variableDeclarators;
            return this;
        }

        public DtoMetaInfo build() {
            return new DtoMetaInfo(packageName, typeQualifier, typeName, dtoName, asVariableDeclarator, imports,
                    variableDeclarators);
        }

        public String toString() {
            return "DtoMetaInfo.DtoMetaInfoBuilder(packageName=" + this.packageName + ", typeQualifier="
                    + this.typeQualifier + ", typeName=" + this.typeName + ", dtoName=" + this.dtoName
                    + ", asVariableDeclarator=" + this.asVariableDeclarator + ", imports=" + this.imports
                    + ", variableDeclarators=" + this.variableDeclarators + ")";
        }

    }

}