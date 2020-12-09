package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.exception.HandlerNameConflictException;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServicesResultDto;
import com.spldeolin.allison1875.handlertransformer.javabean.MetaInfo;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-22
 */
@Singleton
@Log4j2
public class HandlerTransformer implements Allison1875MainProcessor {

    @Inject
    private BlueprintAnalyzeProc blueprintAnalyzeProc;

    @Inject
    private BlueprintCollectProc blueprintCollectProc;

    @Inject
    private GenerateDtosProc generateDtosProc;

    @Inject
    private GenerateHandlerProc generateHandlerProc;

    @Inject
    private GenerateServicesProc generateServicesProc;

    @Override
    public void process(AstForest astForest) {
        AstForestContext.setCurrent(astForest);
        Collection<CompilationUnit> cus = Sets.newHashSet();

        for (CompilationUnit cu : AstForestContext.getCurrent()) {
            for (Pair<ClassOrInterfaceDeclaration, InitializerDeclaration> pair : blueprintCollectProc.process(cu)) {

                ClassOrInterfaceDeclaration controller = pair.getLeft();
                InitializerDeclaration blueprint = pair.getRight();

                MetaInfo metaInfo = blueprintAnalyzeProc.process(controller, blueprint);

                if (metaInfo.isLack()) {
                    continue;
                }


                Collection<CompilationUnit> dtoCus = generateDtosProc
                        .process(metaInfo.getSourceRoot(), metaInfo.getDtos());

                GenerateServicesResultDto generateServicesResult = generateServicesProc.process(metaInfo);
                CompilationUnit serviceCu = generateServicesResult.getServiceCu();
                CompilationUnit serviceImplCu = generateServicesResult.getServiceImplCu();

                try {
                    cus.add(generateHandlerProc.process(metaInfo, generateServicesResult.getServiceQualifier()));
                } catch (HandlerNameConflictException e) {
                    log.warn("handler[{}]在controller[{}]已存在了同名方法，不再生成", metaInfo.getHandlerName(),
                            metaInfo.getController().getName());
                    continue;
                }

                cus.addAll(dtoCus);
                cus.add(serviceCu);
                cus.add(serviceImplCu);
            }
        }

        Saves.save(cus);
    }

}
