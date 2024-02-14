package com.spldeolin.allison1875.sqlapigenerator;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateControllerMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateMapperMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.MemberAdderService;
import com.spldeolin.allison1875.sqlapigenerator.service.MethodGeneratorService;
import com.spldeolin.allison1875.sqlapigenerator.service.TrackCoidDetectorService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-20
 */
@Slf4j
public class SqlapiGenerator implements Allison1875MainService {

    @Inject
    private TrackCoidDetectorService trackCoidDetectorService;

    @Inject
    private MethodGeneratorService methodGeneratorService;

    @Inject
    private MemberAdderService memberAdderService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        TrackCoidDto trackCoid = trackCoidDetectorService.detectTrackCoids(astForest);
        log.info("list trackCoid={}", trackCoid);

        // 生成Mapper方法，以及方法签名所需的Javabean
        GenerateMapperMethodRetval generateMapperMethodRetval = methodGeneratorService.generateMapperMethod(trackCoid,
                astForest);
        flushes.addAll(generateMapperMethodRetval.getFlushes());

        // 将Mapper方法追加到Mapper
        LexicalPreservingPrinter.setup(trackCoid.getMapperCu());
        memberAdderService.addMethodToCoid(generateMapperMethodRetval.getMethod().clone(), trackCoid.getMapper());
        importExprService.extractQualifiedTypeToImport(trackCoid.getMapperCu());
        flushes.add(FileFlush.buildLexicalPreserving(trackCoid.getMapperCu()));

        // 生成Mapper xml方法
        List<String> xmlMethodCodeLines = methodGeneratorService.generateMapperXmlMethod(generateMapperMethodRetval);

        // 将Mapper xml方法追加到Mapper xml
        List<FileFlush> xmlFlushes = memberAdderService.addMethodToXml(xmlMethodCodeLines, trackCoid);
        flushes.addAll(xmlFlushes);

        if (trackCoid.getService() != null) {
            // 生成Service方法，以及方法签名所需的Javabean
            GenerateServiceMethodRetval generateServiceMethodRetval = methodGeneratorService.generateServiceMethod(
                    trackCoid, generateMapperMethodRetval);
            flushes.addAll(generateServiceMethodRetval.getFlushes());

            // 将Service方法追加到Service
            LexicalPreservingPrinter.setup(trackCoid.getServiceCu());
            memberAdderService.addMethodToCoid(generateServiceMethodRetval.getMethod().clone(), trackCoid.getService());
            importExprService.extractQualifiedTypeToImport(trackCoid.getServiceCu());
            flushes.add(FileFlush.buildLexicalPreserving(trackCoid.getServiceCu()));

            // 将Service方法追加到ServiceImpl
            for (int i = 0; i < trackCoid.getServiceImplCus().size(); i++) {
                CompilationUnit serviceImplCu = trackCoid.getServiceImplCus().get(i);
                ClassOrInterfaceDeclaration serviceImpl = trackCoid.getServiceImpls().get(i);
                LexicalPreservingPrinter.setup(serviceImplCu);
                memberAdderService.ensureAuwired(trackCoid.getMapper(), serviceImpl);
                memberAdderService.addMethodToCoid(generateServiceMethodRetval.getMethodImpl().clone(), serviceImpl);
                importExprService.extractQualifiedTypeToImport(serviceImplCu);
                flushes.add(FileFlush.buildLexicalPreserving(serviceImplCu));
            }
            if (trackCoid.getController() != null) {
                // 生成Controller方法，以及方法签名所需的Javabean
                GenerateControllerMethodRetval generateControllerMethodRetval =
                        methodGeneratorService.generateControllerMethod(
                        trackCoid, generateServiceMethodRetval, astForest);
                flushes.addAll(generateControllerMethodRetval.getFlushes());

                // 将Controller方法追加到Controller
                LexicalPreservingPrinter.setup(trackCoid.getControllerCu());
                memberAdderService.ensureAuwired(trackCoid.getService(), trackCoid.getController());
                memberAdderService.addMethodToCoid(generateControllerMethodRetval.getMethod().clone(),
                        trackCoid.getController());
                importExprService.extractQualifiedTypeToImport(trackCoid.getControllerCu());
                flushes.add(FileFlush.buildLexicalPreserving(trackCoid.getControllerCu()));
            }
        }

        // flush
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info(BaseConstant.REMEMBER_REFORMAT_CODE_ANNOUNCE);
        } else {
            log.warn("nothing happened");
        }
    }

}