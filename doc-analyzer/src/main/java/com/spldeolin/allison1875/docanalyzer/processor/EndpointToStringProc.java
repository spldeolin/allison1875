package com.spldeolin.allison1875.docanalyzer.processor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.LotNo.ModuleAbbr;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.handle.MoreHandlerAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;

/**
 * @author Deolin 2020-12-04
 */
@Singleton
public class EndpointToStringProc {

    @Inject
    private MoreHandlerAnalysisHandle moreHandlerAnalysisHandle;

    public String toString(EndpointDto dto) {
        String deprecatedNode = null;
        if (dto.getIsDeprecated()) {
            deprecatedNode = "> 该接口已被开发者标记为**已废弃**，不建议调用";
        }

        String comment = null;
        if (dto.getDescriptionLines().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String line : dto.getDescriptionLines()) {
                if (StringUtils.isNotBlank(line)) {
                    sb.append(line).append("\n");
                } else {
                    sb.append("\n");
                }
            }
            // sb并不是只有换号符时
            if (StringUtils.isNotBlank(sb)) {
                sb.insert(0, "##### 注释\n");
                comment = StringEscapeUtils.escapeHtml4(sb.deleteCharAt(sb.length() - 1).toString());
            }
        }

        String developer = "##### 开发者\n";
        if (StringUtils.isNotBlank(dto.getAuthor())) {
            developer += dto.getAuthor();
        } else {
            developer += "未知的开发者";
        }

        String code = "##### 源码\n";
        code += dto.getSourceCode();

        String moreText = moreHandlerAnalysisHandle.moreToString(dto.getMore());

        String allison1875Note = "\n---\n";
        allison1875Note += LotNo.TAG_PREFIXION + LotNo.build(ModuleAbbr.DA, JsonUtils.toJson(dto), true);

        return Joiner.on('\n').skipNulls().join(deprecatedNode, comment, developer, code, moreText, allison1875Note);
    }

}