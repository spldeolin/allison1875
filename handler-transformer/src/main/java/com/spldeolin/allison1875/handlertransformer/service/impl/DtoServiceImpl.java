package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.service.DtoService;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
public class DtoServiceImpl implements DtoService {

    @Override
    public List<ClassOrInterfaceDeclaration> detectDtosBottomTop(BlockStmt initBody) {
        List<ClassOrInterfaceDeclaration> dtos = Lists.newArrayList();
        initBody.walk(TreeTraversal.BREADTHFIRST, node -> {
            if (node instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration coid = (ClassOrInterfaceDeclaration) node;
                if (!coid.isInterface()) {
                    dtos.add(coid);
                }
            }
        });
        Collections.reverse(dtos);
        return dtos;
    }

}