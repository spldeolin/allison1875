package com.spldeolin.allison1875.docanalyzer.showdoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Deolin 2020-06-04
 * @see https://www.showdoc.cc/page/102098
 */
@Data
public class ShowdocResponse {

    @JsonProperty("error_code")
    private Integer errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    private Data data;

    @lombok.Data
    public static class Data {

        @JsonProperty("page_id")
        private String pageId;

        @JsonProperty("author_uid")
        private String authorUid;

        @JsonProperty("author_username")
        private String authorUsername;

        @JsonProperty("item_id")
        private String itemId;

        @JsonProperty("cat_id")
        private String catId;

        @JsonProperty("page_title")
        private String pageTitle;

        @JsonProperty("page_comments")
        private String pageComments;

        @JsonProperty("page_content")
        private String pageContent;

        @JsonProperty("s_number")
        private String sNumber;

        @JsonProperty("addtime")
        private String addTime;

    }

}