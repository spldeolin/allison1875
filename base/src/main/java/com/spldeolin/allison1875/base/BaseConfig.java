package com.spldeolin.allison1875.base;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import com.spldeolin.allison1875.base.util.TimeUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875的全局配置
 *
 * @author Deolin 2020-02-08
 */
@Data
@Log4j2
@Accessors(chain = true)
public final class BaseConfig {

    private static final BaseConfig instace;

    static {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(timeModule());
        try {
            instace = mapper.readValue(ClassLoader.getSystemResourceAsStream("base-config.yml"), BaseConfig.class);
        } catch (IOException e) {
            log.error("BaseConfig static block failed.", e);
            throw new ConfigLoadingException();
        }

        instace.projectPaths.removeIf(Objects::isNull);
        if (instace.projectPaths.size() == 0) {
            log.error("未指定projectPath");
            throw new ConfigLoadingException();
        }

        calcCommonPath();
    }

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
     */
    private LocalDateTime targetFileSince;

    /**
     * 项目根目录路径，此项必填
     */
    private Collection<String> projectPaths;

    /**
     * 所有projectPaths的公有部分
     */
    @JsonIgnore
    private Path commonPart;

    private BaseConfig() {
    }

    public static BaseConfig getInstace() {
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

    /**
     * 重新覆盖projectPaths属性
     */
    public BaseConfig resetProjectPaths(Collection<String> projectPaths) {
        if (CollectionUtils.isNotEmpty(projectPaths)) {
            this.projectPaths = projectPaths;
            calcCommonPath();
        }
        return this;
    }

    private static void calcCommonPath() {
        List<String> paths = Lists.newArrayList(instace.projectPaths);
        String common = paths.get(0);
        for (String path : paths) {
            common = Strings.commonPrefix(common, path);
        }
        instace.commonPart = Paths.get(common);
    }

}
