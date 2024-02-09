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
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.service.ListCoidsOnTrackService;

/**
 * @author Deolin 2024-01-22
 */
@Singleton
public class ListCoidsOnTrackServiceImpl implements ListCoidsOnTrackService {

    @Inject
    private AstForestResidenceService astForestResidenceService;

    @Inject
    private SqlapiGeneratorConfig config;

    @Override
    public CoidsOnTrackDto listCoidsOnTrack(AstForest astForest) {
        CoidsOnTrackDto cot = new CoidsOnTrackDto();

        for (String mapperXmlDirectoryPath : config.getMapperXmlDirectoryPaths()) {
            Path mapperXmlDirectory = astForestResidenceService.findModuleRoot(astForest.getPrimaryClass())
                    .resolve(mapperXmlDirectoryPath);
            FileUtils.iterateFiles(mapperXmlDirectory.toFile(), new String[]{"xml"}, true).forEachRemaining(xmlFile -> {
                if (config.getMapperName().equals(FilenameUtils.getBaseName(xmlFile.getName()))) {
                    cot.getMapperXmls().add(xmlFile);
                }
            });
        }
        for (CompilationUnit cu : astForest) {
            cu.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration)
                    .map(TypeDeclaration::asClassOrInterfaceDeclaration).ifPresent(coid -> {
                        if (config.getMapperName().equals(coid.getNameAsString())) {
                            cot.setMapper(coid);
                            cot.setMapperCu(cu);
                        }
                        if (config.getServiceName() != null) {
                            if (config.getServiceName().equals(coid.getNameAsString())) {
                                cot.setService(coid);
                                cot.setServiceCu(cu);
                            }
                            if (coid.getImplementedTypes().stream()
                                    .anyMatch(extendedType -> config.getServiceName().equals(extendedType.getNameAsString()))) {
                                cot.getServiceImpls().add(coid);
                                cot.getServiceImplCus().add(cu);
                            }
                        }
                        if (config.getControllerName() != null) {
                            if (config.getControllerName().equals(coid.getNameAsString())) {
                                cot.setController(coid);
                                cot.setControllerCu(cu);
                            }
                        }
                    });
        }
        return cot;
    }

}