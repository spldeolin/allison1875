package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.file.Path;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-21
 */
@Log4j2
public class PathProc {

    private final AstForest astForest;

    @Getter
    private Path hostPath;

    @Getter
    private Path sourceRoot;

    @Getter
    private Path mapperXmlPath;

    public PathProc(AstForest astForest) {
        this.astForest = astForest;
    }

    public PathProc process() {
        hostPath = astForest.getHost();
        log.info("hostPath={}", hostPath);
        sourceRoot = astForest.getHostSourceRoot();
        log.info("sourceRoot={}", sourceRoot);
        mapperXmlPath = hostPath.resolve(PersistenceGeneratorConfig.getInstance().getMapperXmlDirectoryPath());
        log.info("mapperXmlPath={}", mapperXmlPath);
        return this;
    }

}