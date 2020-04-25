package com.spldeolin.allison1875.da.deprecated.core.processor;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.spldeolin.allison1875.da.deprecated.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.deprecated.core.enums.BodyStructureEnum;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 复杂的Body
 *
 * <pre>
 *  1. 可能是难以解析每个field之间的关系
 *  e.g.: @ResponseBody public Map<UserVo, AccountVo> ...
 *
 *  2. 可能是合法的，但在常规web开发中极少出现的返回类型，或是解析的性能成本较高
 *  e.g.: @ResponseBody public List<Long>[][][][][] ...
 *
 *  3. 可能是在非运行期无法确定类型的泛化返回类型
 *  e.g.: @ResponseBody public FoodVo<OrangeDetailVo> ....
 *  e.g.: @ResponseBody public JSONObject ....
 * </pre>
 *
 * 这类情况不会出现太多，解析成field的成本也比较高，解析出来后也无法以主流的表格形式来描述field之间的关系，所以只提供一个Json Schema作为特殊处理
 *
 * @author Deolin 2020-02-20
 */
@Accessors(fluent = true)
class ChaosBodyProcessor extends BodyStructureProcessor {

    @Setter
    private JsonSchema jsonSchema;

    @Override
    ChaosBodyProcessor moreProcess(ApiDefinition api) {
        moreCheckStatus();

        if (super.forRequestBodyOrNot) {
            api.requestBodyChaosJsonSchema(jsonSchema);
        } else {
            api.responseBodyChaosJsonSchema(jsonSchema);
        }
        return this;
    }

    @Override
    BodyStructureEnum calcBodyStructure() {
        return BodyStructureEnum.chaos;
    }

    private void moreCheckStatus() {
        if (jsonSchema == null) {
            throw new IllegalStateException("jsonSchema cannot be absent.");
        }
    }

}
