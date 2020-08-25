package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.file.Path;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-21
 */
@Log4j2
@Getter
public class PathProc {

    private final Class<?> anyClassFromTargetProject;

    private Path projectPath;

    private Path sourceRootPath;

    private Path mapperXmlPath;

    public PathProc(Class<?> anyClassFromTargetProject) {
        this.anyClassFromTargetProject = anyClassFromTargetProject;
    }

    public PathProc process() {
        projectPath = CodeGenerationUtils.mavenModuleRoot(anyClassFromTargetProject);
        log.info("projectPath={}", projectPath);
        sourceRootPath = projectPath.resolve(PersistenceGeneratorConfig.getInstace().getJavaDirectoryLayout());
        log.info("sourceRootPath={}", sourceRootPath);
        mapperXmlPath = projectPath.resolve(PersistenceGeneratorConfig.getInstace().getMapperXmlDirectoryPath());
        log.info("mapperXmlPath={}", mapperXmlPath);
        return this;
    }

}