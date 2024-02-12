package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcControllerDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.MvcControllerDectectorServiceImpl;

/**
 * 从AstForest发现mvcController
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MvcControllerDectectorServiceImpl.class)
public interface MvcControllerDetectorService {

    List<MvcControllerDto> detectMvcControllers(AstForest astForest);

}
