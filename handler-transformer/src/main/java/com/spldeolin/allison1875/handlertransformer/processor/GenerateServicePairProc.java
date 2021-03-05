package com.spldeolin.allison1875.handlertransformer.processor;

import com.google.inject.Singleton;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceImplParam;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceParam;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceGeneration;
import com.spldeolin.allison1875.handlertransformer.javabean.ServiceImplGeneration;

/**
 * @author Deolin 2021-03-05
 */
@Singleton
public class GenerateServicePairProc {

    public ServiceGeneration generateService(GenerateServiceParam param) {
        FirstLineDto firstLineDto = param.getFirstLineDto();


        ServiceGeneration result = new ServiceGeneration();
        return result;
    }

    public ServiceImplGeneration generateServiceImpl(GenerateServiceImplParam param) {

        ServiceImplGeneration result = new ServiceImplGeneration();
        return result;
    }

}