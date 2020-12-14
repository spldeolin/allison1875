package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.RequestMappingFullDto;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class CopyEndpointProc {

    public Collection<EndpointDto> process(EndpointDto endpoint, RequestMappingFullDto requestMappingFullDto) {
        Collection<EndpointDto> copies = Lists.newArrayList();
        for (String combinedUrl : requestMappingFullDto.getCombinedUrls()) {
            for (RequestMethod combinedVerb : requestMappingFullDto.getCombinedVerbs()) {
                EndpointDto copy = endpoint.copy();
                copy.setUrl(combinedUrl);
                copy.setHttpMethod(StringUtils.lowerCase(combinedVerb.toString()));
                copies.add(copy);
            }
        }
        return copies;
    }

}