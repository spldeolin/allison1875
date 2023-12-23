package com.spldeolin.allison1875.docanalyzer.service;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.ListHandlersServiceImpl;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ListHandlersServiceImpl.class)
public interface ListHandlersService {

    Collection<HandlerFullDto> process(AstForest astForest);

}
