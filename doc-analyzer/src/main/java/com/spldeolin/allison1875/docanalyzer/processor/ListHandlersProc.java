package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.constant.ControllerMarkerConstant;
import com.spldeolin.allison1875.docanalyzer.dto.ControllerFullDto;
import com.spldeolin.allison1875.docanalyzer.dto.HandlerFullDto;
import lombok.extern.log4j.Log4j2;

/**
 * 内聚了 遍历Class controllerClass下handler的功能
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
class ListHandlersProc {

    MethodCollectProc methodCollectProc = new MethodCollectProc();

    ListControllersProc listControllersProc = new ListControllersProc();

    Collection<HandlerFullDto> process(AstForest astForest) {
        Collection<ControllerFullDto> controllers = listControllersProc.process(astForest);

        Collection<HandlerFullDto> result = Lists.newArrayList();
        for (ControllerFullDto controller : controllers) {
            Map<String, MethodDeclaration> methodsByShortestQualifier = methodCollectProc
                    .collectMethods(controller.getCoid());

            for (Method reflectionHandler : controller.getReflection().getDeclaredMethods()) {
                if (!this.isHandler(reflectionHandler)) {
                    continue;
                }

                MethodDeclaration handler = methodsByShortestQualifier
                        .get(MethodQualifiers.getShortestQualifiedSignature(reflectionHandler));
                if (handler == null) {
                    // 可能是源码删除了某个handler但未编译，所以reflectionMethod存在，但MethodDeclaration已经不存在了
                    // 这种情况没有继续处理该handler的必要了
                    continue;
                }

                if (findIgnoreFlag(handler)) {
                    continue;
                }

                // doc-cat标志
                String handlerCat = findCat(handler);
                if (handlerCat == null) {
                    handlerCat = controller.getCat();
                }

                result.add(new HandlerFullDto(handlerCat, handler, reflectionHandler, controller));
            }
        }
        return result;
    }

    private boolean isHandler(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) != null;
    }

    private boolean findIgnoreFlag(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_IGNORE)) {
                return true;
            }
        }
        return false;
    }

    private String findCat(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_CAT)) {
                String catContent = org.apache.commons.lang3.StringUtils
                        .removeStartIgnoreCase(line, ControllerMarkerConstant.DOC_CAT).trim();
                if (catContent.length() > 0) {
                    return catContent;
                }
            }
        }
        return null;
    }

}
