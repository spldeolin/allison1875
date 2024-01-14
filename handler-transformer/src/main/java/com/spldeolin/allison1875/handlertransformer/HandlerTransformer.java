package com.spldeolin.allison1875.handlertransformer;

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
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.HandlerCreation;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.javabean.ServicePairDto;
import com.spldeolin.allison1875.handlertransformer.service.ControllerService;
import com.spldeolin.allison1875.handlertransformer.service.DtoService;
import com.spldeolin.allison1875.handlertransformer.service.GenerateServicePairService;
import com.spldeolin.allison1875.handlertransformer.service.InitializerCollectService;
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
    private HandlerTransformerConfig config;

    @Inject
    private ControllerService controllerService;

    @Inject
    private InitializerCollectService initializerCollectService;

    @Inject
    private ReqRespService reqRespService;

    @Inject
    private DtoService dtoService;

    @Inject
    private ParseFirstLineService parseFirstLineService;

    @Inject
    private GenerateServicePairService generateServicePairService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();
        Map<String, ServicePairDto> qualifier2Pair = Maps.newHashMap();
        Map<String, ServicePairDto> name2Pair = Maps.newHashMap();

        for (CompilationUnit cu : astForest) {
            boolean anyTransformed = false;

            for (ClassOrInterfaceDeclaration controller : controllerService.collect(cu)) {
                for (InitializerDeclaration init : initializerCollectService.collectInitializer(controller)) {
                    BlockStmt initBody = init.getBody().clone();
                    FirstLineDto firstLineDto = parseFirstLineService.parse(init);
                    if (firstLineDto == null) {
                        continue;
                    }
                    log.info("detect able to transform Initializer [{}] from Controller [{}].", firstLineDto,
                            controller.getNameAsString());

                    // 校验init下的Req和Resp类
                    reqRespService.checkInitBody(initBody, firstLineDto);

                    // 自底向上收集（广度优先遍历收集 + 反转）
                    List<ClassOrInterfaceDeclaration> dtos = dtoService.collectDtosFromBottomToTop(initBody);

                    // 创建所有所需的Javabean
                    ReqDtoRespDtoInfo reqDtoRespDtoInfo = reqRespService.createJavabeans(astForest, firstLineDto, dtos);
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
                    ServiceGeneration serviceGeneration = generateServicePairService.generateService(param);
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
                    HandlerCreation handlerCreation = controllerService.createHandlerToController(firstLineDto,
                            controller,
                            serviceGeneration, reqDtoRespDtoInfo);
                    log.info("replace Initializer [{}] to Handler [{}] in Controller [{}].", firstLineDto,
                            handlerCreation.getHandler().getName(), controller.getName());

                    cu.addImport(config.getPageTypeQualifier());
                    cu.addImport(ImportConstant.SPRING_REQUEST_BODY);
                    cu.addImport(ImportConstant.JAVAX_VALID);
                    cu.addImport(ImportConstant.SPRING_POST_MAPPING);
                    cu.addImport(ImportConstant.SPRING_AUTOWIRED);
                    cu.addImport(ImportConstant.JAVA_UTIL);

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