package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.file.Path;
import org.apache.logging.log4j.Logger;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * @author Deolin 2020-08-21
 */
public class PathProc {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(PathProc.class);

    private final AstForest astForest;

    private Path hostPath;

    private Path sourceRoot;

    private Path mapperXmlPath;

    public PathProc(AstForest astForest) {
        this.astForest = astForest;
    }

    public PathProc process() {
        hostPath = astForest.getHost();
        log.info("hostPath={}", hostPath);
        sourceRoot = astForest.getHostSourceRoot();
        log.info("sourceRoot={}", sourceRoot);
        mapperXmlPath = hostPath.resolve(PersistenceGenerator.CONFIG.get().getMapperXmlDirectoryPath());
        log.info("mapperXmlPath={}", mapperXmlPath);
        return this;
    }

    public Path getHostPath() {
        return this.hostPath;
    }

    public Path getSourceRoot() {
        return this.sourceRoot;
    }

    public Path getMapperXmlPath() {
        return this.mapperXmlPath;
    }

}