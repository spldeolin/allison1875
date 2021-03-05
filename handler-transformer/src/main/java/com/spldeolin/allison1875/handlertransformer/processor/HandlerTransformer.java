package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.constant.ImportConstants;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-22
 */
@Singleton
@Log4j2
public class HandlerTransformer implements Allison1875MainProcessor {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private CreateServiceMethodHandle createServiceMethodHandle;

    @Inject
    private ControllerProc controllerProc;

    @Inject
    private InitializerCollectProc initializerCollectProc;

    @Inject
    private EnsureNoRepeatProc ensureNoRepeationProc;

    @Inject
    private ReqRespProc reqRespProc;

    @Inject
    private DtoProc dtoProc;

    @Inject
    private ServiceProc serviceProc;

    @Inject
    private ParseFirstLineProc parseFirstLineProc;

    @Inject
    private GenerateServicePairProc generateServicePairProc;

    @Override
    public void process(AstForest astForest) {
        int detectCount = 0;
        for (CompilationUnit cu : astForest) {
            for (ClassOrInterfaceDeclaration controller : controllerProc.collect(cu)) {
                for (InitializerDeclaration init : initializerCollectProc.collectInitializer(controller)) {
                    BlockStmt initBody = init.getBody().clone();
                    FirstLineDto firstLineDto = parseFirstLineProc.parse(init.getBody().getStatements());
                    if (firstLineDto == null) {
                        continue;
                    }
                    log.info("detect able to transform Initializer [{}] from Controller [{}].", firstLineDto,
                            controller.getNameAsString());
                    detectCount++;

                    // 当指定的handlerName在controller中已经存在同名handler时，handlerName后拼接Ex（递归，确保不会重名）
                    ensureNoRepeationProc.inController(controller, firstLineDto);

                    // 校验init下的Req和Resp类
                    reqRespProc.checkInitBody(initBody, firstLineDto);

                    // 自底向上收集（广度优先遍历收集 + 反转）
                    List<ClassOrInterfaceDeclaration> dtos = dtoProc.collectDtosFromBottomToTop(initBody);

                    // 创建所有所需的Javabean
                    ReqDtoRespDtoInfo reqDtoRespDtoInfo = reqRespProc.createJavabeans(cu, firstLineDto, dtos);

                    // 创建Service
                    SingleMethodServiceCuBuilder serviceBuilder = serviceProc
                            .generateServiceWithImpl(cu, firstLineDto, reqDtoRespDtoInfo);

                    ServiceGeneration serviceGeneration = generateServicePairProc.generateService(null);

                    // 创建ServiceImpl
                    Saves.add(serviceBuilder.buildServiceImpl());
                    log.info("generate ServiceImpl [{}].", serviceBuilder.getServiceImpl().getNameAsString());

                    // 在controller中创建handler
                    controllerProc
                            .createHandlerToController(firstLineDto, controller, serviceGeneration, reqDtoRespDtoInfo);

                    // 从controller中删除init
                    boolean anyTransformed = init.remove();
                    log.info("delete Initializer [{}] from Controller [{}].", firstLineDto,
                            controller.getNameAsString());

                    // 每个Initializer转化完毕后SaveAll一次
                    if (anyTransformed) {
                        Imports.ensureImported(cu, handlerTransformerConfig.getPageTypeQualifier());
                        Imports.ensureImported(cu, AnnotationConstant.REQUEST_BODY_QUALIFIER);
                        Imports.ensureImported(cu, AnnotationConstant.VALID_QUALIFIER);
                        Imports.ensureImported(cu, AnnotationConstant.POST_MAPPING_QUALIFIER);
                        Imports.ensureImported(cu, AnnotationConstant.AUTOWIRED_QUALIFIER);
                        Imports.ensureImported(cu, ImportConstants.COLLECTION);
                        Saves.add(cu);
                        Saves.saveAll();
                    }
                }
            }
        }

        if (detectCount == 0) {
            log.warn("no Initializer detect.");
        }
    }

}