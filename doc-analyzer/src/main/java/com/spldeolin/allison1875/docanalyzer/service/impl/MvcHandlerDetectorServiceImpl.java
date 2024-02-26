package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcControllerDto;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerDetectorService;
import com.spldeolin.allison1875.docanalyzer.util.LoadClassUtils;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2020-06-10
 */
@Slf4j
@Singleton
public class MvcHandlerDetectorServiceImpl implements MvcHandlerDetectorService {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public List<MvcHandlerDto> detectMvcHandler(AstForest astForest) {
        List<MvcHandlerDto> result = Lists.newArrayList();

        for (CompilationUnit cu : astForest) {
            if (!CompilationUnitUtils.getCuAbsolutePath(cu).startsWith(astForest.getAstForestRoot())) {
                // 非宿主controller
                continue;
            }
            for (ClassOrInterfaceDeclaration coid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                if (isNotController(coid)) {
                    continue;
                }
                if (isIgnore(coid)) {
                    continue;
                }

                // 反射controller，如果失败那么这个controller就没有继续处理的必要了
                Class<?> mvcControllerReflection;
                try {
                    mvcControllerReflection = tryReflectController(coid);
                } catch (ClassNotFoundException e) {
                    continue;
                }

                // 分类
                String mvcControllerCat = getControllerCat(coid);

                Map<String/*shortestQualifiedSignature*/, MethodDeclaration> mds = this.listMethods(coid);

                for (Method methodReflection : mvcControllerReflection.getDeclaredMethods()) {
                    if (isNotMvcHandler(methodReflection)) {
                        continue;
                    }

                    MethodDeclaration mvcHandlerMd = mds.get(
                            MethodQualifierUtils.getShortestQualifiedSignature(methodReflection));

                    if (mvcHandlerMd == null) {
                        // 可能是源码删除了某个mvcHandler但未编译，所以反射对象存在，但ast对象已经不存在了
                        // 这种情况没有继续处理该mvcHandler的必要了
                        continue;
                    }

                    if (isIgnore(mvcHandlerMd)) {
                        continue;
                    }

                    MvcControllerDto mvcController = new MvcControllerDto();
                    mvcController.setCat(mvcControllerCat);
                    mvcController.setCoid(coid);
                    mvcController.setReflection(mvcControllerReflection);
                    MvcHandlerDto mvcHandler = new MvcHandlerDto();
                    mvcHandler.setMvcController(mvcController);
                    mvcHandler.setCat(mvcControllerCat);
                    mvcHandler.setMd(mvcHandlerMd);
                    mvcHandler.setReflection(methodReflection);
                    result.add(mvcHandler);
                }
            }
        }
        return result;
    }

    private boolean isNotMvcHandler(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) == null;
    }

    private Map<String, MethodDeclaration> listMethods(ClassOrInterfaceDeclaration mvcControllerCoid) {
        Map<String, MethodDeclaration> methods = Maps.newHashMap();
        for (MethodDeclaration method : mvcControllerCoid.findAll(MethodDeclaration.class)) {
            try {
                methods.put(MethodQualifierUtils.getShortestQualifiedSignature(method), method);
            } catch (Exception e) {
                log.warn("fail to get shortest qualified signature [{}]", method.getNameAsString(), e);
            }
        }
        return methods;
    }

    private boolean isNotController(ClassOrInterfaceDeclaration coid) {
        return !annotationExprService.isAnnotated(annotationExprService.springController().getNameAsString(), coid)
                && !annotationExprService.isAnnotated(annotationExprService.springRestController().getNameAsString(),
                coid);
    }

    protected boolean isIgnore(ClassOrInterfaceDeclaration coid) {
        return false;
    }

    protected boolean isIgnore(MethodDeclaration coid) {
        return false;
    }

    private Class<?> tryReflectController(ClassOrInterfaceDeclaration controller) throws ClassNotFoundException {
        String qualifier = controller.getFullyQualifiedName()
                .orElseThrow(() -> new QualifierAbsentException(controller));
        try {
            return LoadClassUtils.loadClass(qualifier, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("cannot load class [{}]", qualifier);
            throw e;
        }
    }

    protected String getControllerCat(ClassOrInterfaceDeclaration controller) {
        String controllerCat = Iterables.getFirst(JavadocUtils.getCommentAsLines(controller), "");
        if (StringUtils.isEmpty(controllerCat)) {
            controllerCat = controller.getNameAsString();
        }
        return controllerCat;
    }

}
