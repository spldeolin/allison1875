package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.base.Joiner;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.StringUtils;
import lombok.Data;

/**
 * @author Deolin 2020-06-01
 */
@Data
public class EndpointDto {

    private String cat;

    private String handlerSimpleName;

    private Collection<String> descriptionLines;

    private String url;

    private Collection<String> httpMethods;

    private Boolean isDeprecated;

    private JsonSchema requestBodyJsonSchema;

    private JsonSchema responseBodyJsonSchema;

    private String author;

    private String sourceCode;

    public String toStringPrettily() {
        String deprecatedNode = null;
        if (isDeprecated) {
            deprecatedNode = "> 该接口已被开发者标记为**已废弃**，不建议调用";
        }

        String comment = null;
        if (descriptionLines.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String line : descriptionLines) {
                if (StringUtils.isNotBlank(line)) {
                    sb.append(line).append("\n");
                } else {
                    sb.append("\n");
                }
            }
            // sb并不是只有换号符时
            if (StringUtils.isNotBlank(sb)) {
                sb.insert(0, "##### 注释\n");
                comment = sb.deleteCharAt(sb.length() - 1).toString();
            }
        }

        String developer = "##### 开发者\n";
        if (StringUtils.isNotBlank(author)) {
            developer += author;
        } else {
            developer += "未知的开发者";
        }

        String code = "##### 源码\n";
        code += sourceCode;

        String allison1875Note = "\n---\n";
        allison1875Note += "*该YApi文档" + BaseConstant.BY_ALLISON_1875 + "*";

        return Joiner.on('\n').skipNulls().join(deprecatedNode, comment, developer, code, allison1875Note);
    }

}