package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.sqlapigenerator.SqlapiGeneratorConfig;
import com.spldeolin.allison1875.sqlapigenerator.exception.AnalyzeSqlException;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ControllerMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.MapperMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ServiceMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.service.GenerateMethodService;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * @author Deolin 2024-01-22
 */
@Singleton
public class GenerateMethodServiceImpl implements GenerateMethodService {

    @Inject
    private SqlapiGeneratorConfig config;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Inject
    private AntiDuplicationService antiDuplicationService;

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
            throw new AnalyzeSqlException(
                    "Only SQL starting with insert, select, update, and delete is supported. current=" + sql);
        }
        return result;
    }

    @Override
    public MapperMethodGenerationDto generateMapperMethod(CoidsOnTrackDto coidsOnTrack, AstForest astForest) {
        MapperMethodGenerationDto result = new MapperMethodGenerationDto();
        MethodDeclaration method = new MethodDeclaration();

        String methodName = antiDuplicationService.getNewMethodNameIfExist(config.getMethodName(),
                coidsOnTrack.getMapper());
        method.setName(methodName);

        Statement stmt;
        try {
            stmt = CCJSqlParserUtil.parse(config.getSql());
        } catch (JSQLParserException e) {
            throw new AnalyzeSqlException("fail to parse SQL, sql=" + config.getSql(), e);
        }

        if (stmt instanceof Insert) {
            method.setType("void");
        } else if (stmt instanceof Select) {
            // param
            JavabeanArg arg = new JavabeanArg();
            arg.setAstForest(astForest);
            arg.setPackageName(config.getMapperConditionPackage());
            arg.setClassName(MoreStringUtils.upperFirstLetter(config.getMethodName()) + "Cond");
            arg.setAuthorName(config.getAuthor());
            arg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration generation = javabeanGeneratorService.generate(arg);
            result.setParamTypeQualifier(generation.getJavabeanQualifier());
            result.getFlushes().add(generation.getFileFlush());
            method.addParameter(generation.getJavabeanQualifier(), "cond");

            // result
            arg = new JavabeanArg();
            arg.setAstForest(astForest);
            arg.setPackageName(config.getMapperRecordPackage());
            arg.setClassName(MoreStringUtils.upperFirstLetter(config.getMethodName()) + "Record");
            arg.setAuthorName(config.getAuthor());
            arg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            generation = javabeanGeneratorService.generate(arg);
            result.setResultTypeQualifier(generation.getJavabeanQualifier());
            result.getFlushes().add(generation.getFileFlush());
            if (config.getSelectListOrOne()) {
                method.setType(String.format("List<%s>", generation.getJavabeanQualifier()));
            } else {
                method.setType(generation.getJavabeanQualifier());
            }
        } else if (stmt instanceof Update) {
            method.setType("int");
        } else if (stmt instanceof Delete) {
            method.setType("int");
        } else {
            throw new AnalyzeSqlException(
                    "only SQL starting with insert, select, update, and delete is supported. current="
                            + config.getSql());
        }
        method.setBody(null);
        result.setMethod(method);
        return result;
    }

    @Override
    public ServiceMethodGenerationDto generateServiceMethod(CoidsOnTrackDto coidsOnTrack,
            MapperMethodGenerationDto mapperMethodGeneration) {
        MethodDeclaration mapperMethod = mapperMethodGeneration.getMethod();

        MethodDeclaration serviceMethod = new MethodDeclaration();
        mapperMethod.getJavadoc().ifPresent(serviceMethod::setJavadocComment);
        serviceMethod.setType(mapperMethod.getType());
        serviceMethod.setName(mapperMethod.getName());
        serviceMethod.setParameters(mapperMethod.getParameters());
        serviceMethod.setBody(null);

        MethodDeclaration serviceImplMethod = new MethodDeclaration();
        mapperMethod.getJavadoc().ifPresent(serviceImplMethod::setJavadocComment);
        serviceImplMethod.addAnnotation(AnnotationConstant.OVERRIDE);
        serviceImplMethod.setPublic(true);
        serviceImplMethod.setType(mapperMethod.getType());
        serviceImplMethod.setName(mapperMethod.getName());
        serviceImplMethod.setParameters(mapperMethod.getParameters());
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(String.format("return %s.%s(cond);",
                MoreStringUtils.lowerFirstLetter(coidsOnTrack.getMapper().getNameAsString()),
                mapperMethodGeneration.getMethod().getNameAsString())));
        serviceImplMethod.setBody(body);

        ServiceMethodGenerationDto result = new ServiceMethodGenerationDto();
        result.setMethod(serviceMethod);
        result.setMethodImpl(serviceImplMethod);
        return result;
    }

    @Override
    public ControllerMethodGenerationDto generateControllerMethod(CoidsOnTrackDto coidsOnTrack,
            ServiceMethodGenerationDto serviceMethodGeneration, AstForest astForest) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist(config.getMethodName(),
                coidsOnTrack.getController());
        MethodDeclaration method = new MethodDeclaration();
        method.addAnnotation(StaticJavaParser.parseAnnotation(String.format("@PostMapping(\"%s\")", methodName)));
        method.setPublic(true);
        method.setType(serviceMethodGeneration.getMethod().getType());
        method.setName(methodName);
        Parameter param = serviceMethodGeneration.getMethod().getParameter(0).clone();
        param.addAnnotation(AnnotationConstant.REQUEST_BODY);
        param.addAnnotation(AnnotationConstant.VALID);
        method.addParameter(param);
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(String.format("return %s.%s(cond);",
                MoreStringUtils.lowerFirstLetter(coidsOnTrack.getService().getNameAsString()),
                serviceMethodGeneration.getMethod().getNameAsString())));
        method.setBody(body);

        coidsOnTrack.getControllerCu().addImport(ImportConstant.SPRING_POST_MAPPING);
        coidsOnTrack.getControllerCu().addImport(ImportConstant.SPRING_REQUEST_BODY);
        coidsOnTrack.getControllerCu().addImport(ImportConstant.JAVAX_VALID);

        ControllerMethodGenerationDto result = new ControllerMethodGenerationDto();
        result.setMethod(method);
        return result;
    }

}