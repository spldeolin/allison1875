package com.spldeolin.allison1875.base;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
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
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Iterables;
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
     * projectPaths的第一个路径
     */
    @Deprecated
    private Path projectPath;

    /**
     * 项目根目录路径，此项必填
     */
    private Collection<Path> projectPaths;

    /**
     * Maven全局配置setting.xml的路径
     */
    private Path mavenGlobalSettingXmlPath;

    /**
     * maven客户端的路径
     */
    private Path mavenHome;

    /**
     * 开启类加载时，此项必填。
     * 使用脚本前需要先确保项目已经执行过mvn clean package，然后填入打包后的jar文件或是war文件路径
     */
    private Path warOrFatJarPath;

    /**
     * 是否禁用类加载收集策略来收集CU，部分工具需要启用类加载
     * 禁用后可以加快收集速度，但是AST node将会不再支持resolved、calculateResolvedType等方法
     * （这个配置项默认为true，是否设置为false不由用户通过config.yml配置决定，而是由每个决定）
     */
    private Boolean collectWithLoadingClass = true;

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
     */
    private LocalDateTime giveUpResultAddedSinceTime;

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
            instace.projectPath = Iterables.getFirst(instace.projectPaths, null);

        } catch (Exception e) {
            log.error("BaseConfig.getInstance failed.", e);
            throw new ConfigLoadingException();
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

}
