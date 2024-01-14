package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.RequestMappingFullDto;
import com.spldeolin.allison1875.docanalyzer.service.CopyEndpointService;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class CopyEndpointServiceImpl implements CopyEndpointService {

    @Override
    public List<EndpointDto> copy(EndpointDto endpoint, RequestMappingFullDto requestMappingFullDto) {
        List<EndpointDto> copies = Lists.newArrayList();
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