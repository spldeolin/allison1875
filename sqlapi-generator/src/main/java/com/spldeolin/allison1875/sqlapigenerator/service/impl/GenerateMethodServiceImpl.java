package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.sqlapigenerator.SqlapiGeneratorConfig;
import com.spldeolin.allison1875.sqlapigenerator.exception.UnsupportedSqlException;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ControllerMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.MapperMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ServiceMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.service.GenerateMethodService;

/**
 * @author Deolin 2024-01-22
 */
@Singleton
public class GenerateMethodServiceImpl implements GenerateMethodService {

    @Inject
    private SqlapiGeneratorConfig config;

    @Override
    public List<String> generateMapperXmlMethod(MapperMethodGenerationDto mapperMethodGeneration) {
        String sql = config.getSql();
        String methodName = mapperMethodGeneration.getMethod().getNameAsString();
        String paramTypeQualifier = mapperMethodGeneration.getParamTypeQualifier();
        String resultTypeQualifier = mapperMethodGeneration.getResultTypeQualifier();

        List<String> result = Lists.newArrayList();
        if (StringUtils.startsWithIgnoreCase(sql, "insert")) {
            result.add(String.format("<insert id=\"%s\" parameterType=\"%s\"", methodName, paramTypeQualifier));
            result.add(sql);
            result.add("</insert>");
        } else if (StringUtils.startsWithIgnoreCase(sql, "select")) {
            result.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultType=\"%s\">", methodName,
                    paramTypeQualifier, resultTypeQualifier));
            result.add(sql);
            result.add("</select>");
        } else if (StringUtils.startsWithIgnoreCase(sql, "update")) {
            result.add(String.format("<update id=\"%s\" parameterType=\"%s\" resultType=\"int\">", methodName,
                    paramTypeQualifier));
            result.add(sql);
            result.add("</update>");
        } else if (StringUtils.startsWithIgnoreCase(sql, "delete")) {
            result.add(String.format("<delete id=\"%s\" parameterType=\"%s\" resultType=\"int\">", methodName,
                    paramTypeQualifier));
            result.add(sql);
            result.add("</delete>");
        } else {
            throw new UnsupportedSqlException(
                    "Only SQL starting with insert, select, update, and delete is supported. current=" + sql);
        }
        return result;
    }

    @Override
    public MapperMethodGenerationDto generateMapperMethod(CoidsOnTrackDto coidsOnTrack) {
        MethodDeclaration method = new MethodDeclaration();
        method.setName(config.getMethodName());

        String sql = config.getSql();
        if (StringUtils.startsWithIgnoreCase(sql, "insert")) {
            method.setType("void");
        } else if (StringUtils.startsWithIgnoreCase(sql, "select")) {
            // TODO 如何分析出生成的Mapper方法需要什么参数Javabena、需要什么返回值Javabean？
            //  遍历astForest，找出每个coid下的fieldNames的列表
            //  用正则找出每个${}里的内容，记录成varNames
            //  根据varNames匹配fieldNames，找出coid
            //  以上的行为可靠性不高——对于简单varNames，有些coid的fieldName能对上，但他不是一个Javabean；或者没有按预期找到想要的类
            //  可以通过config.enableXxx开启或关闭上述行为；开启时找不到或者关闭时
            //   如果是select，通过jsqlparser找出select子句中的列
            //   如果是insert，返回值固定为void，如果是update或delete，返回值固定为int

        } else if (StringUtils.startsWithIgnoreCase(sql, "update")) {
            method.setType("int");
        } else if (StringUtils.startsWithIgnoreCase(sql, "delete")) {
            method.setType("int");
        } else {
            throw new UnsupportedSqlException(
                    "Only SQL starting with insert, select, update, and delete is supported. current=" + sql);
        }


        MapperMethodGenerationDto result = new MapperMethodGenerationDto();
        result.setMethod(method);
//        result.setParamTypeQualifier();
//        result.setResultTypeQualifier();
        return null;
    }

    @Override
    public ServiceMethodGenerationDto generateServiceMethod(CoidsOnTrackDto coidsOnTrack) {
        return null;
    }

    @Override
    public ControllerMethodGenerationDto generateControllerMethod(CoidsOnTrackDto coidsOnTrack) {
        return null;
    }

}