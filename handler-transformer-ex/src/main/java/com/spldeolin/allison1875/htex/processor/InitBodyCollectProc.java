package com.spldeolin.allison1875.htex.processor;

import java.util.Collection;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
public class InitBodyCollectProc {

    public Collection<BlockStmt> collect(ClassOrInterfaceDeclaration coid) {
        Collection<BlockStmt> result = Lists.newArrayList();
        for (BodyDeclaration<?> member : coid.getMembers()) {
            if (member.isInitializerDeclaration()) {
                result.add(member.asInitializerDeclaration().getBody());
            }
        }
        return result;
    }

}