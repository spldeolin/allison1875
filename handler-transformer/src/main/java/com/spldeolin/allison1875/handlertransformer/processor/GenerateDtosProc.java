package com.spldeolin.allison1875.handlertransformer.processor;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.handlertransformer.javabean.DtoMetaInfo;

/**
 * @author Deolin 2020-08-28
 */
public class GenerateDtosProc {

    public Collection<CompilationUnit> process(Path sourceRoot, Collection<DtoMetaInfo> dtoMetaInfos) {
        Collection<CompilationUnit> dtoCus = Lists.newArrayList();
        for (DtoMetaInfo dtoMetaInfo : dtoMetaInfos) {
            Collection<ImportDeclaration> imports = Lists.newArrayList(dtoMetaInfo.getImports());
            imports.add(new ImportDeclaration("lombok.Data", false, false));
            imports.add(new ImportDeclaration("lombok.experimental.Accessors", false, false));
            imports.add(new ImportDeclaration("java.util.Collection", false, false));

            CuCreator cuCreator = new CuCreator(sourceRoot, dtoMetaInfo.getPackageName(), imports, () -> {
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                Javadoc javadoc = new JavadocComment("").parse()
                        .addBlockTag("author", HandlerTransformer.CONFIG.get().getAuthor() + " " + LocalDate.now());
                coid.setJavadocComment(javadoc);
                coid.addAnnotation(StaticJavaParser.parseAnnotation("@Data"));
                coid.addAnnotation(StaticJavaParser.parseAnnotation("@Accessors(chain = true)"));
                coid.setPublic(true).setName(dtoMetaInfo.getTypeName());
                for (Pair<String, String> pair : dtoMetaInfo.getVariableDeclarators()) {
                    FieldDeclaration field = StaticJavaParser.parseBodyDeclaration(pair.getRight() + ";")
                            .asFieldDeclaration();
                    field.setPrivate(true);
                    if (pair.getLeft() != null) {
                        field.setJavadocComment(pair.getLeft());
                    }
                    coid.addMember(field);
                }
                return coid;
            });
            dtoCus.add(cuCreator.create(false));
        }
        return dtoCus;
    }

}