package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.base.Joiner;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.util.StringUtils;

/**
 * @author Deolin 2020-06-01
 */
public class EndpointDto {

    private String cat;

    private String handlerSimpleName;

    private Collection<String> descriptionLines;

    private String url;

    private String httpMethod;

    private Boolean isDeprecated;

    private JsonSchema requestBodyJsonSchema;

    private JsonSchema responseBodyJsonSchema;

    private String author;

    private String sourceCode;

    public EndpointDto() {
    }

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

    public String getCat() {
        return this.cat;
    }

    public String getHandlerSimpleName() {
        return this.handlerSimpleName;
    }

    public Collection<String> getDescriptionLines() {
        return this.descriptionLines;
    }

    public String getUrl() {
        return this.url;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public Boolean getIsDeprecated() {
        return this.isDeprecated;
    }

    public JsonSchema getRequestBodyJsonSchema() {
        return this.requestBodyJsonSchema;
    }

    public JsonSchema getResponseBodyJsonSchema() {
        return this.responseBodyJsonSchema;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getSourceCode() {
        return this.sourceCode;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public void setHandlerSimpleName(String handlerSimpleName) {
        this.handlerSimpleName = handlerSimpleName;
    }

    public void setDescriptionLines(Collection<String> descriptionLines) {
        this.descriptionLines = descriptionLines;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setIsDeprecated(Boolean isDeprecated) {
        this.isDeprecated = isDeprecated;
    }

    public void setRequestBodyJsonSchema(JsonSchema requestBodyJsonSchema) {
        this.requestBodyJsonSchema = requestBodyJsonSchema;
    }

    public void setResponseBodyJsonSchema(JsonSchema responseBodyJsonSchema) {
        this.responseBodyJsonSchema = responseBodyJsonSchema;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EndpointDto)) {
            return false;
        }
        final EndpointDto other = (EndpointDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$cat = this.getCat();
        final Object other$cat = other.getCat();
        if (this$cat == null ? other$cat != null : !this$cat.equals(other$cat)) {
            return false;
        }
        final Object this$handlerSimpleName = this.getHandlerSimpleName();
        final Object other$handlerSimpleName = other.getHandlerSimpleName();
        if (this$handlerSimpleName == null ? other$handlerSimpleName != null
                : !this$handlerSimpleName.equals(other$handlerSimpleName)) {
            return false;
        }
        final Object this$descriptionLines = this.getDescriptionLines();
        final Object other$descriptionLines = other.getDescriptionLines();
        if (this$descriptionLines == null ? other$descriptionLines != null
                : !this$descriptionLines.equals(other$descriptionLines)) {
            return false;
        }
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) {
            return false;
        }
        final Object this$httpMethod = this.getHttpMethod();
        final Object other$httpMethod = other.getHttpMethod();
        if (this$httpMethod == null ? other$httpMethod != null : !this$httpMethod.equals(other$httpMethod)) {
            return false;
        }
        final Object this$isDeprecated = this.getIsDeprecated();
        final Object other$isDeprecated = other.getIsDeprecated();
        if (this$isDeprecated == null ? other$isDeprecated != null : !this$isDeprecated.equals(other$isDeprecated)) {
            return false;
        }
        final Object this$requestBodyJsonSchema = this.getRequestBodyJsonSchema();
        final Object other$requestBodyJsonSchema = other.getRequestBodyJsonSchema();
        if (this$requestBodyJsonSchema == null ? other$requestBodyJsonSchema != null
                : !this$requestBodyJsonSchema.equals(other$requestBodyJsonSchema)) {
            return false;
        }
        final Object this$responseBodyJsonSchema = this.getResponseBodyJsonSchema();
        final Object other$responseBodyJsonSchema = other.getResponseBodyJsonSchema();
        if (this$responseBodyJsonSchema == null ? other$responseBodyJsonSchema != null
                : !this$responseBodyJsonSchema.equals(other$responseBodyJsonSchema)) {
            return false;
        }
        final Object this$author = this.getAuthor();
        final Object other$author = other.getAuthor();
        if (this$author == null ? other$author != null : !this$author.equals(other$author)) {
            return false;
        }
        final Object this$sourceCode = this.getSourceCode();
        final Object other$sourceCode = other.getSourceCode();
        return this$sourceCode == null ? other$sourceCode == null : this$sourceCode.equals(other$sourceCode);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof EndpointDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $cat = this.getCat();
        result = result * PRIME + ($cat == null ? 43 : $cat.hashCode());
        final Object $handlerSimpleName = this.getHandlerSimpleName();
        result = result * PRIME + ($handlerSimpleName == null ? 43 : $handlerSimpleName.hashCode());
        final Object $descriptionLines = this.getDescriptionLines();
        result = result * PRIME + ($descriptionLines == null ? 43 : $descriptionLines.hashCode());
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        final Object $httpMethod = this.getHttpMethod();
        result = result * PRIME + ($httpMethod == null ? 43 : $httpMethod.hashCode());
        final Object $isDeprecated = this.getIsDeprecated();
        result = result * PRIME + ($isDeprecated == null ? 43 : $isDeprecated.hashCode());
        final Object $requestBodyJsonSchema = this.getRequestBodyJsonSchema();
        result = result * PRIME + ($requestBodyJsonSchema == null ? 43 : $requestBodyJsonSchema.hashCode());
        final Object $responseBodyJsonSchema = this.getResponseBodyJsonSchema();
        result = result * PRIME + ($responseBodyJsonSchema == null ? 43 : $responseBodyJsonSchema.hashCode());
        final Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        final Object $sourceCode = this.getSourceCode();
        result = result * PRIME + ($sourceCode == null ? 43 : $sourceCode.hashCode());
        return result;
    }

    public String toString() {
        return "EndpointDto(cat=" + this.getCat() + ", handlerSimpleName=" + this.getHandlerSimpleName()
                + ", descriptionLines=" + this.getDescriptionLines() + ", url=" + this.getUrl() + ", httpMethod=" + this
                .getHttpMethod() + ", isDeprecated=" + this.getIsDeprecated() + ", requestBodyJsonSchema=" + this
                .getRequestBodyJsonSchema() + ", responseBodyJsonSchema=" + this.getResponseBodyJsonSchema()
                + ", author=" + this.getAuthor() + ", sourceCode=" + this.getSourceCode() + ")";
    }

}