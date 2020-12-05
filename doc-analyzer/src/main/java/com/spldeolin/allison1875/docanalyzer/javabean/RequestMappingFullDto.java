package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.Collection;
import org.springframework.web.bind.annotation.RequestMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Deolin 2020-12-04
 */
@Data
@AllArgsConstructor
public class RequestMappingFullDto {

    private Collection<String> combinedUrls;

    private Collection<RequestMethod> combinedVerbs;

}