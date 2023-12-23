package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.Collection;
import org.springframework.web.bind.annotation.RequestMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-04
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestMappingFullDto {

    Collection<String> combinedUrls;

    Collection<RequestMethod> combinedVerbs;

}