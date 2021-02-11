package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.List;
import java.util.Set;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Sets;
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
    private EnsureNoRepeationProc ensureNoRepeationProc;

    @Inject
    private ReqRespProc reqRespProc;

    @Inject
    private DtoProc dtoProc;

    @Inject
    private ServiceProc serviceProc;

    @Inject
    private ParseFirstLineProc parseFirstLineProc;

    @Override
    public void process(AstForest astForest) {
        Set<CompilationUnit> toCreate = Sets.newHashSet();

        for (CompilationUnit cu : astForest) {
            for (ClassOrInterfaceDeclaration controller : controllerProc.collect(cu)) {
                boolean anyTransformed = false;
                for (InitializerDeclaration init : initializerCollectProc.collectInitializer(controller)) {
                    BlockStmt initBody = init.getBody().clone();
                    FirstLineDto firstLineDto = parseFirstLineProc.parse(initBody.getStatements());
                    if (firstLineDto == null) {
                        continue;
                    }
                    log.info("detect able to transform Initializer [{}] from Controller [{}].", firstLineDto,
                            controller.getNameAsString());

                    // 当指定的handlerName在controller中已经存在同名handler时，handlerName后拼接Ex（递归，确保不会重名）
                    ensureNoRepeationProc.ensureNoRepeation(controller, firstLineDto);

                    // 校验init下的Req和Resp类
                    reqRespProc.checkInitBody(initBody, firstLineDto);

                    // 自底向上收集（广度优先遍历收集 + 反转）
                    List<ClassOrInterfaceDeclaration> dtos = dtoProc.collectDtosFromBottomToTop(initBody);

                    // 创建所有所需的Javabean
                    ReqDtoRespDtoInfo reqDtoRespDtoInfo = reqRespProc.createJavabeans(toCreate, cu, firstLineDto, dtos);

                    // 创建Service
                    SingleMethodServiceCuBuilder serviceBuilder = serviceProc
                            .generateServiceWithImpl(cu, firstLineDto, reqDtoRespDtoInfo);
                    toCreate.add(serviceBuilder.buildService());
                    log.info("create Service [{}].", serviceBuilder.getService().getNameAsString());
                    toCreate.add(serviceBuilder.buildServiceImpl());
                    log.info("create ServiceImpl [{}].", serviceBuilder.getServiceImpl().getNameAsString());

                    // 在controller中创建handler
                    controllerProc
                            .createHandlerToController(firstLineDto, controller, serviceBuilder, reqDtoRespDtoInfo);

                    // 从controller中删除init
                    anyTransformed |= init.remove();
                    log.info("delete Initializer [{}] from Controller [{}].", firstLineDto,
                            controller.getNameAsString());
                }

                // controller中存在被转化成handler的构造代码块
                if (anyTransformed) {
                    Imports.ensureImported(cu, handlerTransformerConfig.getPageTypeQualifier());
                    Imports.ensureImported(cu, AnnotationConstant.REQUEST_BODY_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.VALID_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.POST_MAPPING_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.AUTOWIRED_QUALIFIER);
                    Imports.ensureImported(cu, ImportConstants.COLLECTION);
                    toCreate.add(cu);
                }
            }
        }
        toCreate.forEach(Saves::save);
    }

}