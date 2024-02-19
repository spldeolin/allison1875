package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.service.AstForestResidenceService;
import com.spldeolin.allison1875.sqlapigenerator.SqlapiGeneratorConfig;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.TrackCoidDetectorService;

/**
 * @author Deolin 2024-01-22
 */
@Singleton
public class TrackCoidDetectorServiceImpl implements TrackCoidDetectorService {

    @Inject
    private AstForestResidenceService astForestResidenceService;

    @Inject
    private SqlapiGeneratorConfig config;

    @Override
    public TrackCoidDto detectTrackCoids(AstForest astForest) {
        TrackCoidDto result = new TrackCoidDto();

        for (String mapperXmlDirectoryPath : config.getCommonConfig().getMapperXmlDirectoryPaths()) {
            Path mapperXmlDirectory = astForestResidenceService.findModuleRoot(astForest.getPrimaryClass())
                    .resolve(mapperXmlDirectoryPath);
            FileUtils.iterateFiles(mapperXmlDirectory.toFile(), new String[]{"xml"}, true).forEachRemaining(xmlFile -> {
                if (config.getMapperName().equals(FilenameUtils.getBaseName(xmlFile.getName()))) {
                    result.getMapperXmls().add(xmlFile);
                }
            });
        }
        for (CompilationUnit cu : astForest) {
            cu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration)
                    .map(TypeDeclaration::asClassOrInterfaceDeclaration).ifPresent(coid -> {
                        if (config.getMapperName().equals(coid.getNameAsString())) {
                            result.setMapper(coid);
                            result.setMapperCu(cu);
                        }
                        if (config.getServiceName() != null) {
                            if (config.getServiceName().equals(coid.getNameAsString())) {
                                result.setService(coid);
                                result.setServiceCu(cu);
                            }
                            if (coid.getImplementedTypes().stream()
                                    .anyMatch(extendedType -> config.getServiceName().equals(extendedType.getNameAsString()))) {
                                result.getServiceImpls().add(coid);
                                result.getServiceImplCus().add(cu);
                            }
                        }
                        if (config.getControllerName() != null) {
                            if (config.getControllerName().equals(coid.getNameAsString())) {
                                result.setController(coid);
                                result.setControllerCu(cu);
                            }
                        }
                    });
        }
        return result;
    }

}