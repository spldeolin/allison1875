package com.spldeolin.allison1875.da.markdown;

import java.util.Collection;
import lombok.Data;

/**
 * @author Deolin 2020-02-17
 */
@Data
public class RequestBodyFieldVo {

    private String linkName;

    private String description;

    private Collection<String> jsonTypeAndFormats;

    private String validators;

}