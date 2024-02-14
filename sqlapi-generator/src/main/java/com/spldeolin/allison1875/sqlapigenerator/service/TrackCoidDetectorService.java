package com.spldeolin.allison1875.sqlapigenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.TrackCoidDetectorServiceImpl;

/**
 * @author Deolin 2024-01-21
 */
@ImplementedBy(TrackCoidDetectorServiceImpl.class)
public interface TrackCoidDetectorService {

    TrackCoidDto detectTrackCoids(AstForest astForest);

}