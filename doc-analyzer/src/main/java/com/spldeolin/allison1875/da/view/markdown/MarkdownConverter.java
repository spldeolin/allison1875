package com.spldeolin.allison1875.da.view.markdown;

import java.util.Collection;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.exception.FreeMarkerPrintExcpetion;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.domain.BodyFieldDomain;
import com.spldeolin.allison1875.da.core.domain.ValidatorDomain;
import com.spldeolin.allison1875.da.core.enums.BodyType;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-18
 */
@Log4j2
public class MarkdownConverter {

    public void convert(Collection<ApiDomain> apis) {
        for (ApiDomain api : apis) {
            SimpleMdOutputVo vo = new SimpleMdOutputVo();
            vo.setUri(Iterables.getFirst(api.uri(), ""));
            vo.setDescription(api.description());
            vo.setIsRequestBodyNone(BodyType.none == api.requestBodyType());
            vo.setIsRequestBodyChaos(BodyType.chaos == api.requestBodyType());
            vo.setIsResponseBodyNone(BodyType.none == api.responseBodyType());
            vo.setIsResponseBodyChaos(BodyType.chaos == api.responseBodyType());
            vo.setLocation("");

            if (!vo.getIsRequestBodyChaos() && !vo.getIsRequestBodyNone()) {
                Collection<RequestBodyFieldVo> fieldVos = Lists.newArrayList();
                for (BodyFieldDomain field : api.requestBodyFieldsFlatly()) {
                    RequestBodyFieldVo fieldVo = new RequestBodyFieldVo();
                    fieldVo.setLinkName(nullToEmpty(field.fieldName()));
                    fieldVo.setDescription(nullToEmpty(field.description()));
                    fieldVo.setJsonTypeAndFormat(converterTypeAndFormat(field));
                    fieldVo.setValidators(convertValidators(field.nullable(), field.validators()));
                    fieldVos.add(fieldVo);
                }
                vo.setRequestBodyFields(fieldVos);
                vo.setAnyValidatorsExist(fieldVos.stream().allMatch(fieldVo -> "".equals(fieldVo.getValidators())));
            }
            if (!vo.getIsResponseBodyChaos() && !vo.getIsResponseBodyNone()) {
                Collection<ResponseBodyFieldVo> fieldVos = Lists.newArrayList();
                for (BodyFieldDomain field : api.responseBodyFields()) {
                    ResponseBodyFieldVo fieldVo = new ResponseBodyFieldVo();
                    fieldVo.setLinkName(nullToEmpty(field.fieldName()));
                    fieldVo.setDescription(nullToEmpty(field.description()));
                    fieldVo.setJsonTypeAndFormat(converterTypeAndFormat(field));
                    fieldVos.add(fieldVo);
                }
                vo.setResponseBodyFields(fieldVos);
            }

            try {
                FreeMarkerPrinter.print(vo, vo.getDescription());
            } catch (FreeMarkerPrintExcpetion e) {
                log.warn("FreeMarkerPrinter print failed.", e);
            }
        }

    }

    private String converterTypeAndFormat(BodyFieldDomain field) {
        return Joiner.on(" ").skipNulls().join(field.jsonType().getValue(), field.stringFormat(), field.numberFormat());
    }

    private String convertValidators(Boolean nullable, Collection<ValidatorDomain> validators) {
        StringBuilder result = new StringBuilder(64);

        if (Boolean.FALSE.equals(nullable)) {
            result.append("必填");
            result.append("　");
        }

        if (validators != null && validators.size() > 0) {
            Collection<String> parts = Lists.newLinkedList();
            validators.forEach(validator -> {
                parts.add(validator.validatorType().getDescription());
                parts.add(validator.note());
            });
            Joiner.on("　").skipNulls().appendTo(result, parts);
        }

        if (result.length() == 0) {
            return "";
        } else {
            return result.toString();
        }
    }

    private String nullToEmpty(String s) {
        if (s == null) {
            s = "";
        }
        return s;
    }

}
