package com.spldeolin.allison1875.common.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;

/**
 * @author Deolin 2024-01-01
 */
@ImplementedBy(DataModelServiceImpl.class)
public interface DataModelService {

    DataModelGeneration generateDataModel(DataModelArg arg);

}
