package com.spldeolin.allison1875.common.ast;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import com.github.javaparser.utils.CodeGenerationUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-02-02
 */
@Log4j2
public class MavenPathResolver {

    private static final MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();

    public static Path findMavenModule(Class<?> clazz) {
        Path result = CodeGenerationUtils.mavenModuleRoot(clazz);
        log.info("find this module path [{}]", result);
        return result;
    }

    public static Path findMavenProject(Class<?> clazz) {
        Path modulePath = findMavenModule(clazz);
        String pomPath = modulePath.resolve("pom.xml").toString();
        try {
            Model thisModule = mavenXpp3Reader.read(new FileReader(pomPath));
            return findParentPathRecursively(thisModule, modulePath);
        } catch (Exception e) {
            log.error("cannot get project root path", e);
            return modulePath;
        }
    }

    private static Path findParentPathRecursively(Model child, Path childPath)
            throws IOException, XmlPullParserException {
        Parent parent = child.getParent();
        if (parent == null || StringUtils.isEmpty(parent.getRelativePath())) {
            return childPath;
        }
        Path parentPomPath = childPath.resolve(parent.getRelativePath()).normalize();
        if (!parentPomPath.toFile().exists()) {
            return childPath;
        }
        Model parentModel = mavenXpp3Reader.read(new FileReader(parentPomPath.toString()));
        Path parentPath = parentPomPath.resolve("..").normalize();
        log.info("recursively find parent module path [{}]", parentPath);
        return findParentPathRecursively(parentModel, parentPath);
    }

}