package com.spldeolin.allison1875.docanalyzer.processor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.common.collect.Lists;
import lombok.Getter;

/**
 * 内聚了 对请求URL和请求动词解析的功能
 *
 * @author Deolin 2020-06-10
 */
class RequestMappingProcessor {

    private static final PathMatcher pathMatcher = new AntPathMatcher();

    private final String[] cPaths;

    private final RequestMethod[] cVerbs;

    @Getter
    private Collection<String> combinedUrls;

    @Getter
    private Collection<RequestMethod> combinedVerbs;

    public RequestMappingProcessor(Class<?> controllerClass) {
        RequestMapping controllerRequestMapping = findRequestMappingAnnoOrElseNull(controllerClass);
        cPaths = findValueFromAnno(controllerRequestMapping);
        cVerbs = findVerbFromAnno(controllerRequestMapping);
    }

    public void analyze(Method reflectionMethod) {
        RequestMapping methodRequestMapping = findRequestMappingAnnoOrElseNull(reflectionMethod);
        String[] mPaths = methodRequestMapping.value();
        RequestMethod[] mVerbs = methodRequestMapping.method();
        combinedUrls = combineUrl(cPaths, mPaths);
        combinedVerbs = combineVerb(cVerbs, mVerbs);
    }

    private RequestMapping findRequestMappingAnnoOrElseNull(AnnotatedElement annotated) {
        return AnnotatedElementUtils.findMergedAnnotation(annotated, RequestMapping.class);
    }

    private String[] findValueFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new String[0] : controllerRequestMapping.value();
    }

    private RequestMethod[] findVerbFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new RequestMethod[0] : controllerRequestMapping.method();
    }

    private Collection<RequestMethod> combineVerb(RequestMethod[] cVerbs, RequestMethod[] mVerbs) {
        Collection<RequestMethod> combinedVerbs = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cVerbs)) {
            combinedVerbs.addAll(Arrays.asList(cVerbs));
        }
        if (ArrayUtils.isNotEmpty(mVerbs)) {
            combinedVerbs.addAll(Arrays.asList(mVerbs));
        }
        if (combinedVerbs.size() == 0) {
            combinedVerbs.addAll(Arrays.asList(RequestMethod.values()));
        }
        return combinedVerbs;
    }

    private Collection<String> combineUrl(String[] cPaths, String[] mPaths) {
        Collection<String> combinedUrls = Lists.newArrayList();
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

    private Collection<String> ensureAllStartWithSlash(Collection<String> urls) {
        Collection<String> result = Lists.newArrayList();
        for (String url : urls) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            result.add(url);
        }
        return result;
    }

}
