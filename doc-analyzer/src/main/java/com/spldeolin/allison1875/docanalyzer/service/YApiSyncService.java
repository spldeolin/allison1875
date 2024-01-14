package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.YApiSyncServiceImpl;

/**
 * 将endpoints同步到YApi
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(YApiSyncServiceImpl.class)
public interface YApiSyncService {

    void outputToYApi(List<EndpointDto> endpoints) throws Exception;

}