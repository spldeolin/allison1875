package com.spldeolin.allison1875.da.view.rap;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.definition.ApiDefinition;
import lombok.Data;

/**
 * @author Deolin 2019-10-21
 */
@Data
public class ActionListVo {

    private Long pageId;

    private Long id;

    private String requestType;

    private String responseTemplate;

    private String name;

    private String description;

    private String requestUrl;

    private List<ParameterListVo> requestParameterList;

    private List<ParameterListVo> responseParameterList;

    public static ActionListVo build(ApiDefinition apiDto) {
        ActionListVo result = new ActionListVo();
        result.setPageId(389L);
        result.setId(-2333L);
        result.setRequestType("2");
        result.setResponseTemplate("");
        result.setName(apiDto.description());
        result.setDescription("");
        result.setRequestUrl(apiDto.uri().toString());
        result.setRequestParameterList(Lists.newArrayList());
        result.setResponseParameterList(Lists.newArrayList());
        return result;
    }

}