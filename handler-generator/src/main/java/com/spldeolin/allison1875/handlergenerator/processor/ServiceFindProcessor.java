package com.spldeolin.allison1875.handlergenerator.processor;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import lombok.Getter;

/**
 * @author Deolin 2020-06-27
 */
public class ServiceFindProcessor {

    private final AstForest forest;

    private final Collection<String> serviceNames;

    @Getter
    private CompilationUnit cu;

    @Getter
    private ClassOrInterfaceDeclaration service;

    @Getter
    private ClassOrInterfaceDeclaration serviceImpl;

    public ServiceFindProcessor(AstForest forest, Collection<String> serviceNames) {
        this.forest = forest;
        this.serviceNames = serviceNames;
    }

    public boolean findFirst() {
        for (CompilationUnit cu : forest) {
            for (TypeDeclaration<?> td : cu.getTypes()) {
                if (td.isClassOrInterfaceDeclaration()) {
                    ClassOrInterfaceDeclaration coid = td.asClassOrInterfaceDeclaration();
                    if (this.service == null) {
                        if (serviceNames.contains(coid.getNameAsString())) {
                            this.service = coid;
                        }
                    }
                }
            }
        }


        return false;
    }


}
