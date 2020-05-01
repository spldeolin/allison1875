package com.spldeolin.allison1875.base;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ext.NioPathDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import com.spldeolin.allison1875.base.util.TimeUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的全局配置
 *
 * @author Deolin 2020-02-08
 */
@Data
@Log4j2
public final class BaseConfig {

    private static BaseConfig instace;

    /**
     * 项目根目录路径，此项必填
     */
    private Collection<Path> projectPaths;

    /**
     * 项目下模块的源码路径、编译后classpath路径、额外jar文件的路径。
     * 如果是单模块项目，那么主体则是项目本身。
     *
     * 可以使用CompileSourceAndCopyDependencyTool对projectPaths进行编译、jar拷贝后，
     * 产生这个配置的yaml片段
     */
    private Collection<ProjectModule> projectModules;

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
     */
    private LocalDateTime targetFileSince;

    /**
     * 所有projectPaths的公有部分
     */
    @JsonIgnore
    private Path commonPart;

    @JsonIgnore
    private Map<Path, ProjectModule> projectModulesMap = Maps.newHashMap();

    private BaseConfig() {
    }

    public static BaseConfig getInstace() {
        if (instace != null) {
            return instace;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule forNioPath = new SimpleModule();
        forNioPath.addDeserializer(Path.class, new NioPathDeserializer());
        mapper.registerModule(forNioPath);
        mapper.registerModule(timeModule());

        try {
            instace = mapper.readValue(ClassLoader.getSystemResourceAsStream("base-config.yml"), BaseConfig.class);
        } catch (Exception e) {
            log.error("BaseConfig.getInstance failed.", e);
            throw new ConfigLoadingException();
        }

        StringBuilder commonPart = new StringBuilder();
        List<String> paths = getInstace().getProjectPaths().stream().map(Path::toString).collect(Collectors.toList());
        String firstPath = paths.get(0);
        for (int i = 0; i < firstPath.length(); i++) {
            final int ii = i;
            char firstPathEachChar = firstPath.charAt(ii);
            if (paths.stream().allMatch(path -> path.charAt(ii) == firstPathEachChar)) {
                commonPart.append(firstPathEachChar);
            } else {
                break;
            }
        }
        instace.setCommonPart(Paths.get(commonPart.toString()));

        if (instace.getProjectModules() != null) {
            instace.getProjectModules()
                    .forEach(module -> instace.getProjectModulesMap().put(module.getSourceCodePath(), module));
        }

        return instace;
    }

    private static SimpleModule timeModule() {
        SimpleModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter date = TimeUtils.DEFAULT_DATE_FORMATTER;
        DateTimeFormatter time = TimeUtils.DEFAULT_TIME_FORMATTER;
        DateTimeFormatter dateTime = TimeUtils.DEFAULT_DATE_TIME_FORMATTER;
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(date))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(date))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(time))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(time))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTime))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTime));
        return javaTimeModule;
    }

    public static void main(String[] args) {
        System.out.println(getInstace().getCommonPart());
    }

    @Data
    public static class ProjectModule {

        private Path sourceCodePath;

        private Path classesPath;

        private Path externalJarsPath;

    }

}
