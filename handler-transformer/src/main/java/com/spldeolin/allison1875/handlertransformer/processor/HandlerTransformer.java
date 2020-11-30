package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.exception.HandlerNameConflictException;
import com.spldeolin.allison1875.handlertransformer.javabean.MetaInfo;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-22
 */
@Log4j2
public class HandlerTransformer implements Allison1875MainProcessor<HandlerTransformerConfig, HandlerTransformer> {

    public static final ThreadLocal<HandlerTransformerConfig> CONFIG = ThreadLocal
            .withInitial(HandlerTransformerConfig::new);

    @Override
    public HandlerTransformer config(HandlerTransformerConfig config) {
        Set<ConstraintViolation<HandlerTransformerConfig>> violations = ValidateUtils.validate(config);
        if (violations.size() > 0) {
            log.warn("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<HandlerTransformerConfig> violation : violations) {
                log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        CONFIG.set(config);
        return this;
    }

    @Override
    public void process(AstForest astForest) {
        AstForestContext.setCurrent(astForest);
        Collection<CompilationUnit> cus = Sets.newHashSet();

        for (CompilationUnit cu : AstForestContext.getCurrent()) {
            for (Pair<ClassOrInterfaceDeclaration, InitializerDeclaration> pair : new BlueprintCollectProc(cu).process()
                    .getControllerAndBlueprints()) {

                ClassOrInterfaceDeclaration controller = pair.getLeft();
                InitializerDeclaration blueprint = pair.getRight();

                BlueprintAnalyzeProc blueprintAnalyzeProc = new BlueprintAnalyzeProc(controller, blueprint).process();
                MetaInfo metaInfo = blueprintAnalyzeProc.getMetaInfo();

                if (metaInfo.isLack()) {
                    continue;
                }

                GenerateDtosProc generateDtosProc = new GenerateDtosProc(metaInfo.getSourceRoot(), metaInfo.getDtos())
                        .process();
                Collection<CompilationUnit> dtoCus = generateDtosProc.getDtoCus();

                GenerateServicesProc generateServicesProc = new GenerateServicesProc(metaInfo).process();
                CompilationUnit serviceCu = generateServicesProc.getServiceCu();
                CompilationUnit serviceImplCu = generateServicesProc.getServiceImplCu();

                try {
                    GenerateHandlerProc generateHandlerProc = new GenerateHandlerProc(metaInfo,
                            generateServicesProc.getServiceQualifier()).process();
                    cus.add(generateHandlerProc.getControllerCu());
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
