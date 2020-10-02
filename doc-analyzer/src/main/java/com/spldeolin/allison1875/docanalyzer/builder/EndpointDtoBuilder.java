package com.spldeolin.allison1875.docanalyzer.builder;

import java.util.Collection;
import org.springframework.web.bind.annotation.RequestMethod;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDto;

/**
 * @author Deolin 2020-06-01
 */
public class EndpointDtoBuilder {

    private String cat;

    private String handlerSimpleName;

    private Collection<String> descriptionLines;

    private boolean isDeprecated;

    private Collection<String> combinedUrls;

    private Collection<RequestMethod> combinedVerbs;

    private JsonSchema requestBodyJsonSchema;

    private JsonSchema responseBodyJsonSchema;

    private String author;

    private String sourceCode;

    public EndpointDtoBuilder() {
    }

    public Collection<EndpointDto> build() {
        Collection<EndpointDto> result = Lists.newArrayList();
        for (String combinedUrl : combinedUrls) {
            EndpointDto dto = new EndpointDto();
            dto.setCat(cat);
            dto.setHandlerSimpleName(handlerSimpleName);
            dto.setDescriptionLines(descriptionLines);
            dto.setUrl(combinedUrl);
            if (combinedVerbs.contains(RequestMethod.POST)) {
                dto.setHttpMethod("post");
            } else {
                RequestMethod first = Iterables.getFirst(combinedVerbs, null);
                if (first != null) {
                    dto.setHttpMethod(StringUtils.lowerCase(first.toString()));
                } else {
                    dto.setHttpMethod("");
                }
            }
            dto.setIsDeprecated(isDeprecated);
            dto.setRequestBodyJsonSchema(requestBodyJsonSchema);
            dto.setResponseBodyJsonSchema(responseBodyJsonSchema);
            dto.setAuthor(author);
            dto.setSourceCode(sourceCode);
            result.add(dto);
        }
        return result;
    }

    public String cat() {
        return this.cat;
    }

    public String handlerSimpleName() {
        return this.handlerSimpleName;
    }

    public Collection<String> descriptionLines() {
        return this.descriptionLines;
    }

    public boolean isDeprecated() {
        return this.isDeprecated;
    }

    public Collection<String> combinedUrls() {
        return this.combinedUrls;
    }

    public Collection<RequestMethod> combinedVerbs() {
        return this.combinedVerbs;
    }

    public JsonSchema requestBodyJsonSchema() {
        return this.requestBodyJsonSchema;
    }

    public JsonSchema responseBodyJsonSchema() {
        return this.responseBodyJsonSchema;
    }

    public String author() {
        return this.author;
    }

    public String sourceCode() {
        return this.sourceCode;
    }

    public EndpointDtoBuilder cat(String cat) {
        this.cat = cat;
        return this;
    }

    public EndpointDtoBuilder handlerSimpleName(String handlerSimpleName) {
        this.handlerSimpleName = handlerSimpleName;
        return this;
    }

    public EndpointDtoBuilder descriptionLines(Collection<String> descriptionLines) {
        this.descriptionLines = descriptionLines;
        return this;
    }

    public EndpointDtoBuilder isDeprecated(boolean isDeprecated) {
        this.isDeprecated = isDeprecated;
        return this;
    }

    public EndpointDtoBuilder combinedUrls(Collection<String> combinedUrls) {
        this.combinedUrls = combinedUrls;
        return this;
    }

    public EndpointDtoBuilder combinedVerbs(Collection<RequestMethod> combinedVerbs) {
        this.combinedVerbs = combinedVerbs;
        return this;
    }

    public EndpointDtoBuilder requestBodyJsonSchema(JsonSchema requestBodyJsonSchema) {
        this.requestBodyJsonSchema = requestBodyJsonSchema;
        return this;
    }

    public EndpointDtoBuilder responseBodyJsonSchema(JsonSchema responseBodyJsonSchema) {
        this.responseBodyJsonSchema = responseBodyJsonSchema;
        return this;
    }

    public EndpointDtoBuilder author(String author) {
        this.author = author;
        return this;
    }

    public EndpointDtoBuilder sourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EndpointDtoBuilder)) {
            return false;
        }
        final EndpointDtoBuilder other = (EndpointDtoBuilder) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$cat = this.cat();
        final Object other$cat = other.cat();
        if (this$cat == null ? other$cat != null : !this$cat.equals(other$cat)) {
            return false;
        }
        final Object this$handlerSimpleName = this.handlerSimpleName();
        final Object other$handlerSimpleName = other.handlerSimpleName();
        if (this$handlerSimpleName == null ? other$handlerSimpleName != null
                : !this$handlerSimpleName.equals(other$handlerSimpleName)) {
            return false;
        }
        final Object this$descriptionLines = this.descriptionLines();
        final Object other$descriptionLines = other.descriptionLines();
        if (this$descriptionLines == null ? other$descriptionLines != null
                : !this$descriptionLines.equals(other$descriptionLines)) {
            return false;
        }
        if (this.isDeprecated() != other.isDeprecated()) {
            return false;
        }
        final Object this$combinedUrls = this.combinedUrls();
        final Object other$combinedUrls = other.combinedUrls();
        if (this$combinedUrls == null ? other$combinedUrls != null : !this$combinedUrls.equals(other$combinedUrls)) {
            return false;
        }
        final Object this$combinedVerbs = this.combinedVerbs();
        final Object other$combinedVerbs = other.combinedVerbs();
        if (this$combinedVerbs == null ? other$combinedVerbs != null
                : !this$combinedVerbs.equals(other$combinedVerbs)) {
            return false;
        }
        final Object this$requestBodyJsonSchema = this.requestBodyJsonSchema();
        final Object other$requestBodyJsonSchema = other.requestBodyJsonSchema();
        if (this$requestBodyJsonSchema == null ? other$requestBodyJsonSchema != null
                : !this$requestBodyJsonSchema.equals(other$requestBodyJsonSchema)) {
            return false;
        }
        final Object this$responseBodyJsonSchema = this.responseBodyJsonSchema();
        final Object other$responseBodyJsonSchema = other.responseBodyJsonSchema();
        if (this$responseBodyJsonSchema == null ? other$responseBodyJsonSchema != null
                : !this$responseBodyJsonSchema.equals(other$responseBodyJsonSchema)) {
            return false;
        }
        final Object this$author = this.author();
        final Object other$author = other.author();
        if (this$author == null ? other$author != null : !this$author.equals(other$author)) {
            return false;
        }
        final Object this$sourceCode = this.sourceCode();
        final Object other$sourceCode = other.sourceCode();
        if (this$sourceCode == null ? other$sourceCode != null : !this$sourceCode.equals(other$sourceCode)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof EndpointDtoBuilder;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $cat = this.cat();
        result = result * PRIME + ($cat == null ? 43 : $cat.hashCode());
        final Object $handlerSimpleName = this.handlerSimpleName();
        result = result * PRIME + ($handlerSimpleName == null ? 43 : $handlerSimpleName.hashCode());
        final Object $descriptionLines = this.descriptionLines();
        result = result * PRIME + ($descriptionLines == null ? 43 : $descriptionLines.hashCode());
        result = result * PRIME + (this.isDeprecated() ? 79 : 97);
        final Object $combinedUrls = this.combinedUrls();
        result = result * PRIME + ($combinedUrls == null ? 43 : $combinedUrls.hashCode());
        final Object $combinedVerbs = this.combinedVerbs();
        result = result * PRIME + ($combinedVerbs == null ? 43 : $combinedVerbs.hashCode());
        final Object $requestBodyJsonSchema = this.requestBodyJsonSchema();
        result = result * PRIME + ($requestBodyJsonSchema == null ? 43 : $requestBodyJsonSchema.hashCode());
        final Object $responseBodyJsonSchema = this.responseBodyJsonSchema();
        result = result * PRIME + ($responseBodyJsonSchema == null ? 43 : $responseBodyJsonSchema.hashCode());
        final Object $author = this.author();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        final Object $sourceCode = this.sourceCode();
        result = result * PRIME + ($sourceCode == null ? 43 : $sourceCode.hashCode());
        return result;
    }

    public String toString() {
        return "EndpointDtoBuilder(cat=" + this.cat() + ", handlerSimpleName=" + this.handlerSimpleName()
                + ", descriptionLines=" + this.descriptionLines() + ", isDeprecated=" + this.isDeprecated()
                + ", combinedUrls=" + this.combinedUrls() + ", combinedVerbs=" + this.combinedVerbs()
                + ", requestBodyJsonSchema=" + this.requestBodyJsonSchema() + ", responseBodyJsonSchema=" + this
                .responseBodyJsonSchema() + ", author=" + this.author() + ", sourceCode=" + this.sourceCode() + ")";
    }

}
