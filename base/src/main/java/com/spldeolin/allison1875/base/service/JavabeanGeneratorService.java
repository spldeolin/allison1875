package com.spldeolin.allison1875.base.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.service.impl.JavabeanGeneratorServiceImpl;
import com.spldeolin.allison1875.base.service.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.service.javabean.JavabeanGeneration;

/**
 * @author Deolin 2024-01-01
 */
@ImplementedBy(JavabeanGeneratorServiceImpl.class)
public interface JavabeanGeneratorService {

    JavabeanGeneration generate(JavabeanArg arg);

}
