package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeRequestMappingRetval;
import com.spldeolin.allison1875.docanalyzer.service.RequestMappingService;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 对请求URL和请求动词解析的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class RequestMappingServiceImpl implements RequestMappingService {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * composed annotation的全限定名 → 隐含的HTTP method的映射
     *
     * <p>使用LinkedHashMap保持查找顺序
     */
    private static final Map<String, RequestMethod> COMPOSED_ANNOTATION_METHODS = new LinkedHashMap<>();

    static {
        COMPOSED_ANNOTATION_METHODS.put("org.springframework.web.bind.annotation.GetMapping", RequestMethod.GET);
        COMPOSED_ANNOTATION_METHODS.put("org.springframework.web.bind.annotation.PostMapping", RequestMethod.POST);
        COMPOSED_ANNOTATION_METHODS.put("org.springframework.web.bind.annotation.PutMapping", RequestMethod.PUT);
        COMPOSED_ANNOTATION_METHODS.put("org.springframework.web.bind.annotation.DeleteMapping", RequestMethod.DELETE);
        COMPOSED_ANNOTATION_METHODS.put("org.springframework.web.bind.annotation.PatchMapping", RequestMethod.PATCH);
    }

    @Inject
    private Config config;

    @Override
    public AnalyzeRequestMappingRetval analyzeRequestMapping(Class<?> controllerClass, Method reflectionMethod) {
        Annotation controllerRequestMapping = findRequestMappingAnnoOrElseNull(controllerClass);
        String[] controllerPaths = findValueFromAnno(controllerRequestMapping);
        RequestMethod[] controllerVerbs = findVerbFromAnno(controllerRequestMapping);

        Annotation methodRequestMapping = findRequestMappingAnnoOrElseNull(reflectionMethod);
        String[] methodPaths = invokeValue(methodRequestMapping);
        RequestMethod[] methodVerbs = invokeMethod(methodRequestMapping);

        // 将controller层的path与method层的path进行组合
        List<String> combinedUrls = combineUrl(controllerPaths, methodPaths);

        // 为组合后的url添加全局前缀
        String globalUrlPrefix = config.getGlobalUrlPrefix();
        if (StringUtils.isNotBlank(globalUrlPrefix)) {
            if (globalUrlPrefix.startsWith("/")) {
                combinedUrls.replaceAll(combineUrl -> globalUrlPrefix + combineUrl);
            } else {
                combinedUrls.replaceAll(combineUrl -> "/" + globalUrlPrefix + combineUrl);
            }
        }

        final MutableBoolean questionMark = new MutableBoolean(false);

        // 为组合后的url添加@RequestMapping.params条件
        String[] controllerParams = invokeParams(controllerRequestMapping);
        String[] methodParams = invokeParams(methodRequestMapping);
        combinedUrls.replaceAll(combinedUrl -> {
            StringBuilder sb = new StringBuilder(combinedUrl);
            for (String param : controllerParams) {
                if (questionMark.isFalse()) {
                    questionMark.setTrue();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(param);
            }
            for (String param : methodParams) {
                if (questionMark.isFalse()) {
                    questionMark.setTrue();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(param);
            }
            return sb.toString();
        });

        List<RequestMethod> combinedVerbs = combineVerb(controllerVerbs, methodVerbs);
        return new AnalyzeRequestMappingRetval(combinedUrls, combinedVerbs);
    }

    /**
     * 优先查找原始的composed annotation（@GetMapping、@PostMapping等），避免
     * {@link AnnotatedElementUtils#findMergedAnnotation}在跨ClassLoader场景下
     * {@code @AliasFor}属性合并失败导致value()返回空数组的问题。
     *
     * <p>仅当不存在composed annotation时，才回退到查找@RequestMapping本身。
     */
    protected Annotation findRequestMappingAnnoOrElseNull(AnnotatedElement annotated) {
        ClassLoader cl = AstForestContext.get().getClassLoader();

        // 优先查找composed annotation（@GetMapping、@PostMapping等），直接返回原始注解
        for (String composedAnnoName : COMPOSED_ANNOTATION_METHODS.keySet()) {
            try {
                Class<? extends Annotation> composedAnnoClass = (Class<? extends Annotation>) cl.loadClass(
                        composedAnnoName);
                Annotation found = AnnotatedElementUtils.findMergedAnnotation(annotated, composedAnnoClass);
                if (found != null) {
                    return found;
                }
            } catch (ClassNotFoundException e) {
                // 目标项目classpath中没有该注解类，跳过
            }
        }

        // 回退：查找@RequestMapping本身
        try {
            Class<? extends Annotation> requestMappingClass = (Class<? extends Annotation>) cl.loadClass(
                    "org.springframework.web.bind.annotation.RequestMapping");
            return AnnotatedElementUtils.findMergedAnnotation(annotated, requestMappingClass);
        } catch (ClassNotFoundException e) {
            log.error("cannot load RequestMapping class from target project ClassLoader", e);
            return null;
        }
    }

    private String[] findValueFromAnno(Annotation requestMapping) {
        return requestMapping == null ? new String[0] : invokeValue(requestMapping);
    }

    private RequestMethod[] findVerbFromAnno(Annotation requestMapping) {
        return requestMapping == null ? new RequestMethod[0] : invokeMethod(requestMapping);
    }

    private String[] invokeValue(Annotation requestMapping) {
        if (requestMapping == null) {
            return new String[0];
        }
        try {
            return (String[]) requestMapping.annotationType().getMethod("value").invoke(requestMapping);
        } catch (NoSuchMethodException e) {
            // 注解没有value()属性，属于预期情况，静默跳过
            return new String[0];
        } catch (Exception e) {
            log.warn("fail to invoke value() on annotation {}, cause: {}", requestMapping.annotationType().getName(),
                    e.toString());
            return new String[0];
        }
    }

    /**
     * 从注解中获取HTTP method。
     *
     * <p>对于composed annotation（如@PostMapping），其method()属性默认为空数组，
     * 需要从注解类型名推断隐含的HTTP method。
     */
    private RequestMethod[] invokeMethod(Annotation requestMapping) {
        if (requestMapping == null) {
            return new RequestMethod[0];
        }
        try {
            Object[] rawMethods = (Object[]) requestMapping.annotationType().getMethod("method").invoke(requestMapping);
            if (rawMethods.length > 0) {
                RequestMethod[] result = new RequestMethod[rawMethods.length];
                for (int i = 0; i < rawMethods.length; i++) {
                    result[i] = RequestMethod.valueOf(rawMethods[i].toString());
                }
                return result;
            }
        } catch (NoSuchMethodException e) {
            // 注解没有method()属性，属于预期情况，静默跳过，直接走下方的推断逻辑
        } catch (Exception e) {
            log.warn("fail to invoke method() on annotation {}, cause: {}", requestMapping.annotationType().getName(),
                    e.toString());
        }

        // method()返回空数组时，从composed annotation的类型名推断隐含的HTTP method
        String annoTypeName = requestMapping.annotationType().getName();
        RequestMethod impliedMethod = COMPOSED_ANNOTATION_METHODS.get(annoTypeName);
        if (impliedMethod != null) {
            return new RequestMethod[]{impliedMethod};
        }

        return new RequestMethod[0];
    }

    private String[] invokeParams(Annotation requestMapping) {
        if (requestMapping == null) {
            return new String[0];
        }
        try {
            return (String[]) requestMapping.annotationType().getMethod("params").invoke(requestMapping);
        } catch (NoSuchMethodException e) {
            // 注解没有params()属性，属于预期情况，静默跳过
            return new String[0];
        } catch (Exception e) {
            log.warn("fail to invoke params() on annotation {}, cause: {}", requestMapping.annotationType().getName(),
                    e.toString());
            return new String[0];
        }
    }

    private List<RequestMethod> combineVerb(RequestMethod[] cVerbs, RequestMethod[] mVerbs) {
        List<RequestMethod> combinedVerbs = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cVerbs)) {
            combinedVerbs.addAll(Arrays.asList(cVerbs));
        }
        if (ArrayUtils.isNotEmpty(mVerbs)) {
            combinedVerbs.addAll(Arrays.asList(mVerbs));
        }
        if (CollectionUtils.isEmpty(combinedVerbs)) {
            combinedVerbs.addAll(Arrays.asList(RequestMethod.values()));
        }
        return combinedVerbs;
    }

    private List<String> combineUrl(String[] cPaths, String[] mPaths) {
        List<String> combinedUrls = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cPaths) && ArrayUtils.isNotEmpty(mPaths)) {
            for (String cPath : cPaths) {
                for (String mPath : mPaths) {
                    combinedUrls.add(pathMatcher.combine(cPath, mPath));
                }
            }
        } else if (ArrayUtils.isEmpty(cPaths)) {
            combinedUrls.addAll(Arrays.asList(mPaths));
        } else if (ArrayUtils.isEmpty(mPaths)) {
            combinedUrls.addAll(Arrays.asList(cPaths));
        } else {
            combinedUrls.add("/");
        }
        combinedUrls = ensureAllStartWithSlash(combinedUrls);
        return combinedUrls;
    }

    private List<String> ensureAllStartWithSlash(List<String> urls) {
        List<String> result = Lists.newArrayList();
        for (String url : urls) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            result.add(url);
        }
        return result;
    }

}
