package com.spldeolin.allison1875.sqlapigenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.ListCoidsOnTrackServiceImpl;

/**
 * @author Deolin 2024-01-21
 */
@ImplementedBy(ListCoidsOnTrackServiceImpl.class)
public interface ListCoidsOnTrackService {

    CoidsOnTrackDto listCoidsOnTrack(AstForest astForest);

}