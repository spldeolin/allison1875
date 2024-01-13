package com.spldeolin.allison1875.docanalyzer.service;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.docanalyzer.javabean.ControllerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.ListControllersServiceImpl;

/**
 * 内聚了 遍历AstForest中每一个controller的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ListControllersServiceImpl.class)
public interface ListControllersService {

    Collection<ControllerFullDto> listControllers(AstForest astForest);

}
