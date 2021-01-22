package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.CreateServiceMethodHandleResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
public class ServiceProc {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private CreateServiceMethodHandle createServiceMethodHandle;

    public SingleMethodServiceCuBuilder generateServiceWithImpl(CompilationUnit cu, FirstLineDto firstLineDto,
            ReqDtoRespDtoInfo reqDtoRespDtoInfo) {
        SingleMethodServiceCuBuilder serviceBuilder = new SingleMethodServiceCuBuilder();
        serviceBuilder.sourceRoot(Locations.getStorage(cu).getSourceRoot());
        serviceBuilder.servicePackageDeclaration(handlerTransformerConfig.getServicePackage());
        serviceBuilder.implPackageDeclaration(handlerTransformerConfig.getServiceImplPackage());
        serviceBuilder.importDeclarations(cu.getImports());
        List<String> imports = Lists
                .newArrayList("java.util.Collection", handlerTransformerConfig.getPageTypeQualifier());
        if (reqDtoRespDtoInfo.getReqDtoQualifier() != null) {
            imports.add(reqDtoRespDtoInfo.getReqDtoQualifier());
        }
        if (reqDtoRespDtoInfo.getRespDtoQualifier() != null) {
            imports.add(reqDtoRespDtoInfo.getRespDtoQualifier());
        }
        serviceBuilder.importDeclarationsString(imports);
        serviceBuilder.serviceName(MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "Service");

        // 使用handle创建service实现方法
        CreateServiceMethodHandleResult creation = createServiceMethodHandle
                .createMethodImpl(firstLineDto, reqDtoRespDtoInfo.getParamType(), reqDtoRespDtoInfo.getResultType());
        serviceBuilder.method(creation.getServiceMethod());
        serviceBuilder.importDeclarationsString(creation.getAppendImports());

        return serviceBuilder;
    }

}