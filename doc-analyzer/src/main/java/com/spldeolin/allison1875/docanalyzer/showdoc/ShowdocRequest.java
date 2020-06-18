package com.spldeolin.allison1875.docanalyzer.showdoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Deolin 2020-06-04
 * @see https://www.showdoc.cc/page/102098
 */
@Data
public class ShowdocRequest {

    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("api_token")
    private String apiToken;

    @JsonProperty("cat_name")
    private String catName;

    @JsonProperty("page_title")
    private String pageTitle;

    @JsonProperty("page_content")
    private String pageContent;

    @JsonProperty("s_number")
    private Integer sNumber;

}