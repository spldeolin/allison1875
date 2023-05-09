package com.spldeolin.allison1875.startransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.expr.Expression;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2023-05-02
 */
@Data
@Accessors(chain = true)
public class ChainAnalysisDto {

    private String cftEntityName;

    private String cftDesignName;

    private String cftDesignQulifier;

    private Expression cftSecondArgument;

    private List<PhraseDto> phrases;

    // 这里还是需要metaInfo的，3个方案：A约定大于配置，通过entityName从astForest中寻找Mapper；B汇总到一个类，pg生产时写到design包的MetaInfo包下；C从XxxxDesign进行读取

    // 引申出一个问题，如何确定XxxxMapper.queryByYyy是否存在？

    // 同时解决2个问题的方案似乎是D将star-transformer的chain转化为query-transformer的chain，带来问题是要执行2种源码工具，似乎可以接受？
    // 用户以allison1875v5.0提供的StarSchema为开头写star chain -> 执行star-transformer将chain转化成query chain和数据组装code ->
    // 执行query-transformer

}