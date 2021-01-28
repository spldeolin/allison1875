package com.spldeolin.allison1875.docanalyzer.processor;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-28
 */
@Singleton
@Log4j2
public class GetProjectRootPathProc {

    private static final MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();

    public Path getTopAncestor(Class<?> primaryClass) {
        Path thisModulePath = CodeGenerationUtils.mavenModuleRoot(primaryClass);
        String pomPath = thisModulePath.resolve("pom.xml").toString();
        try {
            Model thisModule = mavenXpp3Reader.read(new FileReader(pomPath));
            return getParentPathRecursively(thisModule, thisModulePath, true);
        } catch (Exception e) {
            log.error("cannot get project root path", e);
            return thisModulePath;
        }
    }

    public Path getParentPathRecursively(Model child, Path childPath, boolean firstEntry)
            throws IOException, XmlPullParserException {
        if (firstEntry) {
            log.info("detect this module path [{}].", childPath);
        }
        Parent parent = child.getParent();
        Path parentPomPath = childPath.resolve(parent.getRelativePath()).normalize();
        if (Files.exists(parentPomPath)) {
            Model parentModel = mavenXpp3Reader.read(new FileReader(parentPomPath.toString()));
            Path parentPath = parentPomPath.resolve("..").normalize();
            log.info("recursively detect parent module path [{}].", parentPath);
            return getParentPathRecursively(parentModel, parentPath, false);
        } else {
            return childPath;
        }
    }

}