package com.spldeolin.allison1875.htex.processor;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.builder.SingleMethodServiceCuBuilder;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.htex.HandlerTransformerConfig;
import com.spldeolin.allison1875.htex.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.htex.javabean.FirstLineDto;
import com.spldeolin.allison1875.htex.javabean.ReqDtoRespDtoInfo;

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
        serviceBuilder.method(createServiceMethodHandle
                .createMethodImpl(firstLineDto, reqDtoRespDtoInfo.getParamType(), reqDtoRespDtoInfo.getResultType()));
        return serviceBuilder;
    }

}