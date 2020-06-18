package com.spldeolin.allison1875.docanalyzer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import lombok.extern.log4j.Log4j2;

/**
 * 项目体量分析
 *
 * 分析内容是各种类型、方法的的个数和各自行数
 *
 * @author Deolin 2020-06-05
 */
@Log4j2
public class MassAnalyzer {

    public static void main(String[] args) {
        Map<Path, Multiset<String>> totalCus = Maps.newHashMap();
        Map<Path, Multiset<String>> totalClasses = Maps.newHashMap();
        Map<Path, Multiset<String>> totalInterfaces = Maps.newHashMap();
        Map<Path, Multiset<String>> totalEnums = Maps.newHashMap();
        Map<Path, Multiset<String>> totalAnnotations = Maps.newHashMap();
        Map<Path, Multiset<String>> totalMethods = Maps.newHashMap();
        Map<Path, Collection<String>> totalAbstractMethods = Maps.newHashMap();
        Map<Path, Multiset<String>> totalHandlers = Maps.newHashMap();

        List<String> notRequestBodyTotally = Lists.newArrayList();

        for (CompilationUnit cu : AstForest.getInstance()) {
            Path sourceRoot = cu.getStorage().orElseThrow(StorageAbsentException::new).getSourceRoot();
            getOrElseDefault(totalCus, sourceRoot, HashMultiset.create())
                    .add(Locations.getStorage(cu).getFileName(), calcLineCount(cu));
            for (TypeDeclaration<?> td : cu.findAll(TypeDeclaration.class)) {
                String qualifier = td.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
                int lineCount = calcLineCount(td);
                if (td.isClassOrInterfaceDeclaration()) {
                    ClassOrInterfaceDeclaration coid = td.asClassOrInterfaceDeclaration();
                    if (coid.isInterface()) {
                        getOrElseDefault(totalInterfaces, sourceRoot, HashMultiset.create()).add(qualifier, lineCount);
                    } else {
                        getOrElseDefault(totalClasses, sourceRoot, HashMultiset.create()).add(qualifier, lineCount);
                    }

                    if (isController(coid)) {
                        Class<?> controllerClass;
                        try {
                            controllerClass = tryReflectController(coid);
                        } catch (Exception e) {
                            continue;
                        }

                        Map<String, MethodDeclaration> methods = Maps.newHashMap();
                        for (MethodDeclaration method : coid.findAll(MethodDeclaration.class)) {
                            methods.put(MethodQualifiers.getShortestQualifiedSignature(method), method);
                        }

                        for (Method reflectionMethod : controllerClass.getDeclaredMethods()) {
                            if (AnnotatedElementUtils.findMergedAnnotation(reflectionMethod, RequestMapping.class)
                                    != null) {
                                String methodQualifier = MethodQualifiers
                                        .getShortestQualifiedSignature(reflectionMethod);
                                getOrElseDefault(totalHandlers, sourceRoot, HashMultiset.create())
                                        .add(methodQualifier, calcLineCount(methods.get(methodQualifier)));

                                for (Parameter parameter : reflectionMethod.getParameters()) {
                                    if (parameter.getAnnotation(RequestParam.class) != null) {
                                        notRequestBodyTotally.add(methodQualifier);
                                    }
                                    if (parameter.getAnnotation(PathVariable.class) != null) {
                                        notRequestBodyTotally.add(methodQualifier);
                                    }
                                    if (ArrayUtils.isEmpty(parameter.getAnnotations())) {
                                        notRequestBodyTotally.add(methodQualifier);
                                    }
                                }

                            }
                        }

                    }
                } else if (td.isEnumDeclaration()) {
                    getOrElseDefault(totalEnums, sourceRoot, HashMultiset.create()).add(qualifier, lineCount);
                } else if (td.isAnnotationDeclaration()) {
                    getOrElseDefault(totalAnnotations, sourceRoot, HashMultiset.create()).add(qualifier, lineCount);
                } else {
                    throw new RuntimeException("impossible unless bug");
                }
            }

            for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
                String qualifier = MethodQualifiers.getTypeQualifierWithMethodName(method);
                int lineCount = calcLineCount(method);
                if (method.getBody().isPresent()) {
                    getOrElseDefault(totalMethods, sourceRoot, HashMultiset.create()).add(qualifier, lineCount);
                } else {
                    getOrElseDefault(totalAbstractMethods, sourceRoot, Lists.newArrayList()).add(qualifier);
                }
            }

        }

        totalClasses.forEach(
                (sourceRoot, lineCounts) -> log.info("class 个数：{} [{}]", lineCounts.entrySet().size(), sourceRoot));
        totalClasses
                .forEach((sourceRoot, lineCounts) -> log.info("class 代码总行数：{} [{}]", lineCounts.size(), sourceRoot));

        totalInterfaces.forEach(
                (sourceRoot, lineCounts) -> log.info("interface 个数：{} [{}]", lineCounts.entrySet().size(), sourceRoot));
        totalInterfaces.forEach(
                (sourceRoot, lineCounts) -> log.info("interface 代码总行数：{} [{}]", lineCounts.size(), sourceRoot));

        totalEnums.forEach(
                (sourceRoot, lineCounts) -> log.info("enum 个数：{} [{}]", lineCounts.entrySet().size(), sourceRoot));
        totalEnums.forEach((sourceRoot, lineCounts) -> log.info("enum 代码总行数：{} [{}]", lineCounts.size(), sourceRoot));

        totalAnnotations.forEach((sourceRoot, lineCounts) -> log
                .info("annotation 个数：{} [{}]", lineCounts.entrySet().size(), sourceRoot));
        totalAnnotations.forEach(
                (sourceRoot, lineCounts) -> log.info("annotation 代码总行数：{} [{}]", lineCounts.size(), sourceRoot));

        totalMethods.forEach(
                (sourceRoot, lineCounts) -> log.info("方法 个数：{} [{}]", lineCounts.entrySet().size(), sourceRoot));
        totalMethods.forEach((sourceRoot, lineCounts) -> log.info("方法 代码总行数：{} [{}]", lineCounts.size(), sourceRoot));

        totalAbstractMethods
                .forEach((sourceRoot, lineCounts) -> log.info("抽象方法 个数：{} [{}]", lineCounts.size(), sourceRoot));

        totalHandlers.forEach(
                (sourceRoot, lineCounts) -> log.info("handler 个数：{} [{}]", lineCounts.entrySet().size(), sourceRoot));
        totalHandlers
                .forEach((sourceRoot, lineCounts) -> log.info("handler 代码总行数：{} [{}]", lineCounts.size(), sourceRoot));

        int allProjectLineCount = 0;
        for (Multiset<String> set : totalCus.values()) {
            allProjectLineCount += set.size();
        }
        log.info("全部项目的总行数：{}", allProjectLineCount);

        int allProjectTotalHandlerCount = 0;
        for (Multiset<String> set : totalHandlers.values()) {
            allProjectTotalHandlerCount += set.entrySet().size();
        }
        log.info("全部项目的handler 个数：{}", allProjectTotalHandlerCount);
        notRequestBodyTotally.forEach(log::info);
        log.info(notRequestBodyTotally.size());
    }

    private static Class<?> tryReflectController(ClassOrInterfaceDeclaration controller) throws ClassNotFoundException {
        return LoadClassUtils.loadClass(controller.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                Object.class.getClassLoader());
    }

    private static boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(QualifierConstants.CONTROLLER) || QualifierConstants.CONTROLLER
                        .equals(resolve.getName())) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

    private static int calcLineCount(Node node) {
        Optional<Range> range = node.getRange();
        return range.map(value -> value.end.line - value.begin.line + 1).orElse(0);
    }

    private static <T> T getOrElseDefault(Map<Path, T> map, Path key, T defalutValue) {
        T result = map.get(key);
        if (result == null) {
            map.put(key, defalutValue);
            return defalutValue;
        }
        return result;
    }

}
