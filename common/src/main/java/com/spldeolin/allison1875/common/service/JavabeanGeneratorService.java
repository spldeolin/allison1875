package com.spldeolin.allison1875.common.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.impl.JavabeanGeneratorServiceImpl;

/**
 * @author Deolin 2024-01-01
 */
@ImplementedBy(JavabeanGeneratorServiceImpl.class)
public interface JavabeanGeneratorService {

    JavabeanGeneration generate(JavabeanArg arg);

}
