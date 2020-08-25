package com.spldeolin.allison1875.handlertransformer.strategy;

/**
 * @author Deolin 2020-06-26
 */
public interface PackageStrategy {

    String calcDtoPackage(String controllerPackage);

    String calcReqPackage(String controllerPackage);

    String calcRespPackage(String controllerPackage);

    String calcServicePackage(String controllerPackage);

    String calcServiceImplPackage(String controllerPackage);

}
