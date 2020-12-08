package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.file.Path;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.javabean.PathDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-21
 */
@Log4j2
public class PathProc {

    public PathDto process(AstForest astForest) {
        Path hostPath = astForest.getHost();
        log.info("hostPath={}", hostPath);
        Path sourceRoot = astForest.getHostSourceRoot();
        log.info("sourceRoot={}", sourceRoot);
        Path mapperXmlPath = hostPath.resolve(PersistenceGenerator.CONFIG.get().getMapperXmlDirectoryPath());
        log.info("mapperXmlPath={}", mapperXmlPath);
        return new PathDto().setHostPath(hostPath).setSourceRoot(sourceRoot).setMapperXmlPath(mapperXmlPath);
    }

}