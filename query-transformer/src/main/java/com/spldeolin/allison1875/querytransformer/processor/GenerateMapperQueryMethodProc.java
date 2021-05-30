package com.spldeolin.allison1875.querytransformer.processor;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.FieldAbsentException;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.VerbEnum;
import com.spldeolin.allison1875.querytransformer.javabean.CriterionDto;
import com.spldeolin.allison1875.querytransformer.javabean.QueryMeta;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Log4j2
public class GenerateMapperQueryMethodProc {

    @Inject
    private QueryTransformerConfig queryTransformerConfig;

    public ClassOrInterfaceDeclaration process(AstForest astForest, QueryMeta queryMeta, String queryMethodName,
            Collection<CriterionDto> criterions) {
        ClassOrInterfaceDeclaration mapper = findMapper(astForest, queryMeta);
        if (mapper == null) {
            return null;
        }
        ClassOrInterfaceDeclaration entity = findEntity(astForest, queryMeta);
        if (entity == null) {
            return mapper;
        }

        MethodDeclaration queryMethod = new MethodDeclaration();
        queryMethod.setType(StaticJavaParser.parseType(String.format("List<%s>", queryMeta.getEntityName())));
        queryMethod.setName(queryMethodName);
        for (CriterionDto criterion : criterions) {
            VerbEnum operator = VerbEnum.of(criterion.getOperator());
            if (operator == VerbEnum.NOT_NULL || operator == VerbEnum.IS_NULL) {
                continue;
            }
            String propertyName = criterion.getParameterName();
            Optional<FieldDeclaration> field = entity.getFieldByName(propertyName);
            String propertyType;
            if (field.isPresent()) {
                propertyType = field.orElseThrow(FieldAbsentException::new).getCommonType().toString();
            } else {
                propertyType = queryTransformerConfig.getEntityCommonPropertyTypes().get(propertyName);
            }
            if (operator == VerbEnum.IN || operator == VerbEnum.NOT_IN) {
                propertyType = "Collection<" + propertyType + ">";
            }
            criterion.setParameterType(propertyType);
            Parameter parameter = new Parameter();
            String argumentName = propertyName;
            if (operator == VerbEnum.IN || operator == VerbEnum.NOT_IN) {
                argumentName = English.plural(propertyName);
            }
            parameter.addAnnotation(StaticJavaParser.parseAnnotation(String.format("@Param(\"%s\")", argumentName)));
            parameter.setType(propertyType);
            parameter.setName(argumentName);
            for (ImportDeclaration anImport : entity.findCompilationUnit().orElseThrow(CuAbsentException::new)
                    .getImports()) {
                Imports.ensureImported(mapper, anImport.getNameAsString());
            }
            queryMethod.addParameter(parameter);
            queryMethod.setBody(null);
        }
        mapper.getMembers().add(0, queryMethod);
        Saves.save(mapper.findCompilationUnit().orElseThrow(CuAbsentException::new));

        return mapper;
    }

    private ClassOrInterfaceDeclaration findMapper(AstForest astForest, QueryMeta queryMeta) {
        try {
            String mapperQualifier = queryMeta.getMapperQualifier();
            Path mapperPath = astForest.getPrimaryJavaRoot()
                    .resolve(mapperQualifier.replace('.', File.separatorChar) + ".java");
            return StaticJavaParser.parse(mapperPath).getTypes().get(0).asClassOrInterfaceDeclaration();
        } catch (Exception e) {
            log.warn("寻找Mapper失败", e);
            return null;
        }
    }

    private ClassOrInterfaceDeclaration findEntity(AstForest astForest, QueryMeta queryMeta) {
        try {
            String mapperQualifier = queryMeta.getEntityQualifier();
            Path mapperPath = astForest.getPrimaryJavaRoot()
                    .resolve(mapperQualifier.replace('.', File.separatorChar) + ".java");
            return StaticJavaParser.parse(mapperPath).getTypes().get(0).asClassOrInterfaceDeclaration();
        } catch (Exception e) {
            log.warn("寻找Entity失败", e);
            return null;
        }
    }

}