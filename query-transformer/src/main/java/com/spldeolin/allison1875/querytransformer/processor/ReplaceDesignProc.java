package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-09
 */
@Log4j2
@Singleton
public class ReplaceDesignProc {

    @Inject
    private TransformMethodCallProc transformMethodCallProc;

    public List<Saves.Replace> process(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {
        List<Saves.Replace> replaces = Lists.newArrayList();

        // overwirte service
        String methodCallCode = transformMethodCallProc.process(designMeta, chainAnalysis);

        // ensure service import & autowired
        String autowiredField = StaticJavaParser.parseBodyDeclaration(
                String.format("@Autowired private %s %s;", designMeta.getMapperName(),
                        MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()))).toString();
        chainAnalysis.getChain().findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
            if (!service.getFieldByName(MoreStringUtils.lowerFirstLetter(designMeta.getMapperName())).isPresent()) {
                List<FieldDeclaration> fields = service.getFields();
                if (fields.size() > 0) {
                    String lastFieldCode = Iterables.getLast(fields).getTokenRange()
                            .orElseThrow(RangeAbsentException::new).toString();
                    replaces.add(new Replace(lastFieldCode, lastFieldCode + autowiredField));
                } else {
                    List<MethodDeclaration> methods = service.getMethods();
                    if (methods.size() > 0) {
                        String firstMethodCode = methods.get(0).getTokenRange().orElseThrow(RangeAbsentException::new)
                                .toString();
                        replaces.add(new Replace(firstMethodCode, autowiredField + firstMethodCode));
                    }
                }

                service.getMembers().add(0, StaticJavaParser.parseBodyDeclaration(
                        String.format("@Autowired private %s %s;", designMeta.getMapperName(),
                                MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()))));
                Imports.ensureImported(service, designMeta.getMapperQualifier());
                Imports.ensureImported(service, AnnotationConstant.AUTOWIRED_QUALIFIER);
            }
        });

        replaces.add(
                new Replace(chainAnalysis.getChain().getTokenRange().orElseThrow(RangeAbsentException::new).toString(),
                        methodCallCode));

        return replaces;
    }

}