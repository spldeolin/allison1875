package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.SpringMvcHandlerServiceImpl;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(SpringMvcHandlerServiceImpl.class)
public interface SpringMvcHandlerService {

    List<HandlerFullDto> listHandlers(AstForest astForest);

}
