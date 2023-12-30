package com.spldeolin.allison1875.handlertransformer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.FileFlush;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.constant.ImportConstant;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;
import com.spldeolin.allison1875.handlertransformer.service.ControllerService;
import com.spldeolin.allison1875.handlertransformer.service.DtoService;
import com.spldeolin.allison1875.handlertransformer.service.EnsureNoRepeatService;
import com.spldeolin.allison1875.handlertransformer.service.GenerateServicePairService;
import com.spldeolin.allison1875.handlertransformer.service.InitializerCollectService;
import com.spldeolin.allison1875.handlertransformer.service.MoreTransformService;
import com.spldeolin.allison1875.handlertransformer.service.ParseFirstLineService;
import com.spldeolin.allison1875.handlertransformer.service.ReqRespService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-22
 */
@Singleton
@Log4j2
public class HandlerTransformer implements Allison1875MainService {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private ControllerService controllerProc;

    @Inject
    private InitializerCollectService initializerCollectProc;

    @Inject
    private EnsureNoRepeatService ensureNoRepeationProc;

    @Inject
    private ReqRespService reqRespProc;

    @Inject
    private DtoService dtoProc;

    @Inject
    private ParseFirstLineService parseFirstLineProc;

    @Inject
    private GenerateServicePairService generateServicePairProc;

    @Inject
    private MoreTransformService moreTransformHandle;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();
        Map<String, ServicePairDto> qualifier2Pair = Maps.newHashMap();
        Map<String, ServicePairDto> name2Pair = Maps.newHashMap();

        for (CompilationUnit cu : astForest) {
//            LexicalPreservingPrinter.setup(cu);
            boolean anyTransformed = false;

            for (ClassOrInterfaceDeclaration controller : controllerProc.collect(cu)) {
                for (InitializerDeclaration init : initializerCollectProc.collectInitializer(controller)) {
                    BlockStmt initBody = init.getBody().clone();
                    FirstLineDto firstLineDto = parseFirstLineProc.parse(init);
                    if (firstLineDto == null) {
                        continue;
                    }
                    log.info("detect able to transform Initializer [{}] from Controller [{}].", firstLineDto,
                            controller.getNameAsString());

                    // 当指定的handlerName在controller中已经存在同名handler时，handlerName后拼接Ex（递归，确保不会重名）
                    ensureNoRepeationProc.inController(controller, firstLineDto);

                    // 校验init下的Req和Resp类
                    reqRespProc.checkInitBody(initBody, firstLineDto);

                    // 自底向上收集（广度优先遍历收集 + 反转）
                    List<ClassOrInterfaceDeclaration> dtos = dtoProc.collectDtosFromBottomToTop(initBody);

                    // 创建所有所需的Javabean
                    ReqDtoRespDtoInfo reqDtoRespDtoInfo = reqRespProc.createJavabeans(astForest, firstLineDto, dtos);
                    flushes.addAll(reqDtoRespDtoInfo.getJavabeanCus().stream().map(FileFlush::build)
                            .collect(Collectors.toList()));

                    // 创建Service Pair
                    GenerateServiceParam param = new GenerateServiceParam();
                    param.setCu(cu);
                    param.setFirstLineDto(firstLineDto);
                    param.setReqDtoRespDtoInfo(reqDtoRespDtoInfo);
                    param.setAstForest(astForest);
                    param.setQualifier2Pair(qualifier2Pair);
                    param.setName2Pair(name2Pair);
                    ServiceGeneration serviceGeneration = generateServicePairProc.generateService(param);
                    if (serviceGeneration == null) {
                        continue;
                    }
                    flushes.add(FileFlush.build(
                            serviceGeneration.getService().findCompilationUnit().orElseThrow(CuAbsentException::new)));
                    for (ClassOrInterfaceDeclaration serviceImpl : serviceGeneration.getServiceImpls()) {
                        flushes.add(
                                FileFlush.build(serviceImpl.findCompilationUnit().orElseThrow(CuAbsentException::new)));
                    }

                    // 在controller中创建handler，并替换掉
                    HandlerCreation handlerCreation = controllerProc.createHandlerToController(firstLineDto, controller,
                            serviceGeneration, reqDtoRespDtoInfo);
                    log.info("replace Initializer [{}] to Handler [{}] in Controller [{}].", firstLineDto,
                            handlerCreation.getHandler().getName(), controller.getName());

                    Imports.ensureImported(cu, handlerTransformerConfig.getPageTypeQualifier());
                    Imports.ensureImported(cu, AnnotationConstant.REQUEST_BODY_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.VALID_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.POST_MAPPING_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.AUTOWIRED_QUALIFIER);
                    Imports.ensureImported(cu, ImportConstant.COLLECTION);

                    // 更多的转化操作
                    Collection<CompilationUnit> moreCus = moreTransformHandle.transform(astForest.clone(), firstLineDto,
                            handlerCreation, reqDtoRespDtoInfo.getJavabeanQualifiers());
                    moreCus.forEach(moreCu -> flushes.add(FileFlush.build(moreCu)));

                    anyTransformed = true;
                }
            }

            if (anyTransformed) {
                flushes.add(FileFlush.build(cu));
            }
        }

        // write all to file
        if (flushes.size() > 0) {
            flushes.forEach(FileFlush::flush);
            log.info("# REMEBER REFORMAT CODE #");
        } else {
            log.warn("no valiad Initializer detected");
        }
    }

}