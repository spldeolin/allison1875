package com.spldeolin.allison1875.common.test.packageinfo.p1.p2.p3.p4.p5.p6;

import java.io.File;
import java.util.List;
import com.github.javaparser.ast.PackageDeclaration;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.test.AstForestTestImpl;
import com.spldeolin.allison1875.common.util.JavadocUtils;

/**
 * @author Deolin 2025-02-19
 */
public class LongPackageName {

    public static void main(String[] args) {
        AstForest astForest = new AstForestTestImpl(new File("common/src/test/java"));
        astForest.tryFindCu("com.spldeolin.allison1875.common.test.packageinfo.p1.p2.p3.p4.p5.p6.LongPackageName")
                .ifPresent(cu -> {
                    PackageDeclaration pd = cu.getPackageDeclaration()
                            .orElseThrow(() -> new RuntimeException("impossible"));
                    List<String> commentInPackageInfos = JavadocUtils.getDescriptionFirstLineInPackageInfos(pd,
                            astForest);

                    System.out.println(commentInPackageInfos);
                });
    }

}
