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
import com.spldeolin.allison1875.common.javabean.AddInjectFieldRetval;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.service.MemberAdderService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateMapperMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceImplMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.MethodAdderService;
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
    private MethodAdderService methodAdderService;

    @Inject
    private MemberAdderService memberAdderService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        TrackCoidDto trackCoid = trackCoidDetectorService.detectTrackCoids(astForest);
        log.info("list trackCoid={}", trackCoid);

        // generate Mapper方法
        GenerateMapperMethodRetval generateMapperMethodRetval = methodGeneratorService.generateMapperMethod(trackCoid,
                astForest);
        flushes.addAll(generateMapperMethodRetval.getFlushes());

        // add Mapper方法
        LexicalPreservingPrinter.setup(trackCoid.getMapperCu());
        methodAdderService.addMethodToCoid(generateMapperMethodRetval.getMethod().clone(), trackCoid.getMapper());
        importExprService.extractQualifiedTypeToImport(trackCoid.getMapperCu());
        flushes.add(FileFlush.buildLexicalPreserving(trackCoid.getMapperCu()));

        // generate MapperXml方法
        List<String> xmlMethodCodeLines = methodGeneratorService.generateMapperXmlMethod(generateMapperMethodRetval);

        // add MapperXml方法
        List<FileFlush> xmlFlushes = methodAdderService.addMethodToXml(xmlMethodCodeLines, trackCoid);
        flushes.addAll(xmlFlushes);

        /*
            Service层
         */
        if (trackCoid.getService() != null) {
            // generate Service方法
            GenerateServiceMethodRetval generateServiceMethodRetval = methodGeneratorService.generateServiceMethod(
                    trackCoid, generateMapperMethodRetval.getMethod());
            flushes.addAll(generateServiceMethodRetval.getFlushes());
            // add Service方法，然后extractImports
            LexicalPreservingPrinter.setup(trackCoid.getServiceCu());
            methodAdderService.addMethodToCoid(generateServiceMethodRetval.getMethod().clone(), trackCoid.getService());
            importExprService.extractQualifiedTypeToImport(trackCoid.getServiceCu());
            flushes.add(FileFlush.buildLexicalPreserving(trackCoid.getServiceCu()));

            for (int i = 0; i < trackCoid.getServiceImplCus().size(); i++) {
                CompilationUnit serviceImplCu = trackCoid.getServiceImplCus().get(i);
                ClassOrInterfaceDeclaration serviceImpl = trackCoid.getServiceImpls().get(i);
                LexicalPreservingPrinter.setup(serviceImplCu);
                // add inject Mapper
                AddInjectFieldRetval addInjectMapperFieldRetval = memberAdderService.addInjectField(
                        trackCoid.getMapper(), serviceImpl);
                String mapperVarName = addInjectMapperFieldRetval.getFieldVarName();
                // generate ServiceImpl方法
                GenerateServiceImplMethodRetval generateServiceImplMethodRetval =
                        methodGeneratorService.generateServiceImplMethod(
                        mapperVarName, generateServiceMethodRetval.getMethod().clone(),
                        generateMapperMethodRetval.getMethod());
                flushes.addAll(generateServiceMethodRetval.getFlushes());
                // add ServiceImpl方法，然后extractImports
                methodAdderService.addMethodToCoid(generateServiceImplMethodRetval.getMethodImpl().clone(),
                        serviceImpl);
                importExprService.extractQualifiedTypeToImport(serviceImplCu);
                flushes.add(FileFlush.buildLexicalPreserving(serviceImplCu));
            }

            /*
                Controller层
             */
            if (trackCoid.getController() != null) {
                LexicalPreservingPrinter.setup(trackCoid.getControllerCu());
                // add inject Service
                AddInjectFieldRetval addInjectServiceFieldRetval = memberAdderService.addInjectField(
                        trackCoid.getService(), trackCoid.getController());
                // generate MvcHandler
                GenerateMvcHandlerRetval generateMvcHandlerRetval = methodGeneratorService.generateMvcHandler(trackCoid,
                        addInjectServiceFieldRetval.getFieldVarName(), generateServiceMethodRetval, astForest);
                flushes.addAll(generateMvcHandlerRetval.getFlushes());
                // add MvcHandler，然后extractImports
                methodAdderService.addMethodToCoid(generateMvcHandlerRetval.getMethod().clone(),
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