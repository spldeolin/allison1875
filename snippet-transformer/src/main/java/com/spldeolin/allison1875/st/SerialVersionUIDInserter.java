package com.spldeolin.allison1875.st;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticVcsContainer;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import com.spldeolin.allison1875.base.util.ast.Saves;
import lombok.extern.log4j.Log4j2;

/**
 * 为所有直接/间接实现了Serializable的类添加Serializable属性
 *
 * @author Deolin 2020-02-08
 */
@Log4j2
public class SerialVersionUIDInserter implements Serializable {

    private static final long serialVersionUID = -3947214463641793679L;

    public static void main(String[] args) {
        new SerialVersionUIDInserter().processor();
    }

    private void processor() {
        Random random = new Random();
        StaticJavaParser.getConfiguration().setAttributeComments(false)
                .setDoNotAssignCommentsPrecedingEmptyLines(false);

        Collection<CompilationUnit> updates = Lists.newLinkedList();
        StaticVcsContainer.removeIfNotContain(StaticAstContainer.getCompilationUnits()).forEach(cu -> {
            MutableBoolean hasSetup = new MutableBoolean(false);
            cu.getTypes().stream().filter(this::isClassAndSerialVersionUIDAbsent)
                    .map(TypeDeclaration::asClassOrInterfaceDeclaration).forEach(coid -> {
                ResolvedReferenceTypeDeclaration resolveClass;
                try {
                    resolveClass = coid.resolve();
                } catch (Exception e) {
                    log.warn(e.getMessage());
                    return;
                }

                if (isNotSerializable(resolveClass)) {
                    return;
                }

                if (hasSetup.isFalse()) {
                    hasSetup.setTrue();
                    updates.add(LexicalPreservingPrinter.setup(cu));
                }


                BodyDeclaration<?> field = StaticJavaParser.parseBodyDeclaration(
                        f("private static final long serialVersionUID = %sL;", String.valueOf(random.nextLong())));
                try {
                    coid.addMember(field);
                } catch (Exception e) {
                    log.warn(cu.getStorage().orElseThrow(StorageAbsentException::new).getPath()
                            + "添加serialVersionUID失败");
                }
            });
        });

        updates.forEach(cu -> {
            try {
                Saves.originalSave(cu);
            } catch (Exception e) {
                log.warn(cu.getStorage().orElseThrow(StorageAbsentException::new).getPath(), e);
            }
        });
    }

    private boolean isClassAndSerialVersionUIDAbsent(TypeDeclaration<?> type) {
        return type.isClassOrInterfaceDeclaration() && !type.getFieldByName("serialVersionUID").isPresent();
    }

    private boolean isNotSerializable(ResolvedReferenceTypeDeclaration resolved) {
        return resolved.getAllAncestors().stream()
                .noneMatch(ancestor -> ancestor.getId().equals("java.io.Serializable"));
    }

}
