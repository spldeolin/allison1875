package com.spldeolin.allison1875.inspector.dto;

import java.time.LocalDateTime;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.Locations;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-02-22
 */
@Accessors(chain = true)
public class LawlessDto {

    @JsonProperty("源码位置")
    private String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    @JsonProperty("全限定名")
    private String qualifier;

    @JsonProperty("规约号")
    private String statuteNo;

    @JsonProperty("详细信息")
    private String message;

    @JsonProperty("作者")
    private String author;

    @JsonProperty("修复者")
    private String fixer;

    @JsonProperty("修复时间")
    private LocalDateTime fixedAt;

    public LawlessDto(Node node, String qualifier, String message) {
        this.sourceCode = Locations.getRelativePathWithLineNo(node);
        this.qualifier = qualifier;
        this.message = message;
        this.author = Authors.getAuthor(node);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("\n\t源码位置", sourceCode).append("\n\t全限定名", qualifier)
                .append("\n\t规约号", statuteNo).append("\n\t详细信息", message).append("\n\t作者", author).toString();
    }

    public String getSourceCode() {
        return this.sourceCode;
    }

    public String getQualifier() {
        return this.qualifier;
    }

    public String getStatuteNo() {
        return this.statuteNo;
    }

    public String getMessage() {
        return this.message;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getFixer() {
        return this.fixer;
    }

    public LocalDateTime getFixedAt() {
        return this.fixedAt;
    }

    public LawlessDto setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
        return this;
    }

    public LawlessDto setQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public LawlessDto setStatuteNo(String statuteNo) {
        this.statuteNo = statuteNo;
        return this;
    }

    public LawlessDto setMessage(String message) {
        this.message = message;
        return this;
    }

    public LawlessDto setAuthor(String author) {
        this.author = author;
        return this;
    }

    public LawlessDto setFixer(String fixer) {
        this.fixer = fixer;
        return this;
    }

    public LawlessDto setFixedAt(LocalDateTime fixedAt) {
        this.fixedAt = fixedAt;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LawlessDto)) {
            return false;
        }
        final LawlessDto other = (LawlessDto) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$sourceCode = this.getSourceCode();
        final Object other$sourceCode = other.getSourceCode();
        if (this$sourceCode == null ? other$sourceCode != null : !this$sourceCode.equals(other$sourceCode)) {
            return false;
        }
        final Object this$qualifier = this.getQualifier();
        final Object other$qualifier = other.getQualifier();
        if (this$qualifier == null ? other$qualifier != null : !this$qualifier.equals(other$qualifier)) {
            return false;
        }
        final Object this$statuteNo = this.getStatuteNo();
        final Object other$statuteNo = other.getStatuteNo();
        if (this$statuteNo == null ? other$statuteNo != null : !this$statuteNo.equals(other$statuteNo)) {
            return false;
        }
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) {
            return false;
        }
        final Object this$author = this.getAuthor();
        final Object other$author = other.getAuthor();
        if (this$author == null ? other$author != null : !this$author.equals(other$author)) {
            return false;
        }
        final Object this$fixer = this.getFixer();
        final Object other$fixer = other.getFixer();
        if (this$fixer == null ? other$fixer != null : !this$fixer.equals(other$fixer)) {
            return false;
        }
        final Object this$fixedAt = this.getFixedAt();
        final Object other$fixedAt = other.getFixedAt();
        if (this$fixedAt == null ? other$fixedAt != null : !this$fixedAt.equals(other$fixedAt)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof LawlessDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $sourceCode = this.getSourceCode();
        result = result * PRIME + ($sourceCode == null ? 43 : $sourceCode.hashCode());
        final Object $qualifier = this.getQualifier();
        result = result * PRIME + ($qualifier == null ? 43 : $qualifier.hashCode());
        final Object $statuteNo = this.getStatuteNo();
        result = result * PRIME + ($statuteNo == null ? 43 : $statuteNo.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        final Object $fixer = this.getFixer();
        result = result * PRIME + ($fixer == null ? 43 : $fixer.hashCode());
        final Object $fixedAt = this.getFixedAt();
        result = result * PRIME + ($fixedAt == null ? 43 : $fixedAt.hashCode());
        return result;
    }

}
