package com.spldeolin.allison1875.si.statute;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.util.Locations;
import lombok.extern.log4j.Log4j2;

/**
 * 报告出多次new ResponseInfo对象的方法
 *
 * @author Deolin 2020-02-16
 */
@Log4j2
public class MultiNewResponseInfoReporter {

    public static void main(String[] args) {
        StaticAstContainer.getCompilationUnits().forEach(cu -> cu.findAll(MethodDeclaration.class).forEach(method -> {

            if (method.findAll(ObjectCreationExpr.class, oce -> oce.getTypeAsString().equals("ResponseInfo")).size()
                    > 1) {
                log.info("{}:{}", Locations.getRelativePath(method), Locations.getBeginLine(method));
            }

        }));
    }

}
