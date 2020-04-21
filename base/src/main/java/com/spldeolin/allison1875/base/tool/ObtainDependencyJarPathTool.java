package com.spldeolin.allison1875.base.tool;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.SystemOutHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.util.TimeUtils;
import lombok.extern.log4j.Log4j2;

/**
 * 这是个独立运行的Tool
 * 这个Tool的作用是在对目标项目下的每个pom文件执行mvn dependency:list 命令
 * 以此来获取该项目所有module所依赖的jar文件所在路径
 * 获取到的路径会被覆盖地保存到项目根目录的.dependency-jar-paths.tmp文件中
 *
 * .dependency-jar-paths.tmp文件会被Allison1875的其他模块所使用
 * 所以每次项目的pom文件中dependencies标签内部发生变更，都应该重新执行这个Tool，确保一致
 *
 * @author Deolin 2020-04-19
 */
@Log4j2
public class ObtainDependencyJarPathTool {

    public static void main(String[] args) {
        BaseConfig.getInstace().getProjectPaths().forEach(projectPath -> {
            Collection<String> jarPaths = Sets.newHashSet();
            Iterator<File> xmls = FileUtils.iterateFiles(projectPath.toFile(), new String[]{"xml"}, true);
            while (xmls.hasNext()) {
                File file = xmls.next();
                if ("pom".equals(FilenameUtils.getBaseName(file.getName()))) {
                    jarPaths.addAll(obtainFromPom(file.getPath()));
                }
            }
            try {
                FileUtils.writeLines(projectPath.resolve(".dependency-jar-paths.tmp").toFile(), jarPaths);
            } catch (IOException e) {
                log.error("FileUtils.writeLines({})", projectPath.resolve(".dependency-jar-paths.tmp").toFile(), e);
            }
        });
    }

    private static Collection<String> obtainFromPom(String pomPath) {
        Collection<String> result = Lists.newLinkedList();
        File output = null;
        try {
            output = File.createTempFile(
                    "allision1875-temp-dependency-list" + TimeUtils.toString(LocalDateTime.now(), "-yyyyMMdd-HHmmss"),
                    null);
            output.deleteOnExit();
        } catch (IOException e) {
            log.error(e);
        }
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomPath));
        request.setGlobalSettingsFile(BaseConfig.getInstace().getMavenGlobalSettingXmlPath().toFile());
        request.setGoals(Collections.singletonList("dependency:list -Dexcludes=com.topaiebiz"));
        Properties properties = new Properties();
        properties.setProperty("outputFile", output.getPath()); // redirect output to a file
        properties.setProperty("outputAbsoluteArtifactFilename", "true"); // with paths
        properties.setProperty("includeScope", "runtime"); // only runtime (scope compile + runtime)
        // if only interested in scope runtime, you may replace with excludeScope = compile
        request.setProperties(properties);

        Invoker invoker = new DefaultInvoker();
        // the Maven home can be omitted if the "maven.home" system property is set
        invoker.setMavenHome(BaseConfig.getInstace().getMavenHome().toFile());
        invoker.setOutputHandler(new SystemOutHandler()); // not interested in Maven output itself

        try {
            if (invoker.execute(request).getExitCode() != 0) {
                return result;
            }
        } catch (MavenInvocationException e) {
            log.error(e);
        }

        Pattern pattern = Pattern.compile("(?:compile|runtime):(.*\\.jar)");
        try (LineIterator lineItr = FileUtils.lineIterator(output)) {
            while (lineItr.hasNext()) {
                String line = lineItr.nextLine();
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    // group 1 contains the path to the file
                    result.add(matcher.group(1));
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return result;
    }

}
