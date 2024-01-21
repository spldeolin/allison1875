package com.spldeolin.allison1875.sqlapigenerator.service;

import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;

/**
 * @author Deolin 2024-01-21
 */
public interface ListCoidsOnTrackService {

    CoidsOnTrackDto listCoidsOnTrack(AstForest astForest);

}