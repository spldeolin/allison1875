package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
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
    private InitBodyCollectProc initBodyCollectProc;

    @Inject
    private EnsureNoRepeationProc ensureNoRepeationProc;

    @Inject
    private ReqRespProc reqRespProc;

    @Inject
    private DtoProc dtoProc;

    @Inject
    private ServiceProc serviceProc;

    @Override
    public void process(AstForest astForest) {
        Set<CompilationUnit> toCreate = Sets.newHashSet();

        for (CompilationUnit cu : astForest) {
            for (ClassOrInterfaceDeclaration controller : controllerProc.collect(cu)) {
                ClassOrInterfaceDeclaration controllerClone = controller.clone();

                boolean transformed = false;
                for (BlockStmt initBody : initBodyCollectProc.collect(controller)) {
                    String firstLine = tryGetFirstLine(initBody.getStatements());
                    FirstLineDto firstLineDto = parseFirstLine(firstLine);
                    if (firstLineDto == null) {
                        continue;
                    }
                    log.info(firstLineDto);

                    // 当指定的handlerName在controller中已经存在同名handler时，handlerName后拼接Ex（递归，确保不会重名）
                    ensureNoRepeationProc.ensureNoRepeation(controller, firstLineDto);

                    // 校验init下的Req和Resp类
                    reqRespProc.checkInitBody(initBody, firstLineDto);

                    // 自底向上收集（广度优先遍历收集 + 反转）
                    List<ClassOrInterfaceDeclaration> dtos = dtoProc.collectDtosFromBottomToTop(initBody);

                    // 生成所有所需的Dto
                    ReqDtoRespDtoInfo reqDtoRespDtoInfo = reqRespProc.generateDtos(toCreate, cu, firstLineDto, dtos);

                    // 生成Service
                    SingleMethodServiceCuBuilder serviceBuilder = serviceProc
                            .generateServiceWithImpl(cu, firstLineDto, reqDtoRespDtoInfo);
                    toCreate.add(serviceBuilder.buildService());
                    toCreate.add(serviceBuilder.buildServiceImpl());

                    // 在controller中创建handler
                    controllerProc.createHandlerToController(firstLineDto, controller, controllerClone, serviceBuilder,
                            reqDtoRespDtoInfo);

                    transformed = true;
                }
                if (transformed) {
                    Imports.ensureImported(cu, handlerTransformerConfig.getPageTypeQualifier());
                    Imports.ensureImported(cu, AnnotationConstant.REQUEST_BODY_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.VALID_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.POST_MAPPING_QUALIFIER);
                    Imports.ensureImported(cu, AnnotationConstant.AUTOWIRED_QUALIFIER);
                    controller.replace(controllerClone);
                    toCreate.add(cu);
                }
            }
        }
        toCreate.forEach(Saves::save);
    }

    private String tryGetFirstLine(NodeList<Statement> statements) {
        if (CollectionUtils.isEmpty(statements)) {
            return null;
        }
        Statement first = statements.get(0);
        if (!first.getComment().filter(Comment::isLineComment).isPresent()) {
            return null;
        }
        String firstLineContent = first.getComment().get().asLineComment().getContent();
        return firstLineContent;
    }

    private FirstLineDto parseFirstLine(String firstLineContent) {
        if (StringUtils.isBlank(firstLineContent)) {
            return null;
        }
        firstLineContent = firstLineContent.trim();
        // extract to processor
        String[] parts = firstLineContent.split(" ");
        if (parts.length != 2) {
            return null;
        }

        FirstLineDto firstLineDto = new FirstLineDto();
        firstLineDto.setHandlerUrl(parts[0]);
        firstLineDto.setHandlerName(MoreStringUtils.slashToLowerCamel(parts[0]));
        firstLineDto.setHandlerDescription(parts[1]);
        firstLineDto.setMore(null); // privode handle
        return firstLineDto;
    }

}