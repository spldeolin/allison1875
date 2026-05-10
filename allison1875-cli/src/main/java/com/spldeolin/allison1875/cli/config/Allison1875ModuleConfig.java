package com.spldeolin.allison1875.cli.config;

import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule;
import com.spldeolin.allison1875.formgenerator.FormGeneratorModule;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule;
import com.spldeolin.allison1875.querytransformer.QueryTransformerModule;
import com.spldeolin.allison1875.startransformer.StarTransformerModule;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Maven Plugin 专用配置，继承 {@link Config} 并扩展各功能模块的 Guice Module 类名。
 * 用户通常无需修改这些字段，仅在需要自定义/扩展 Module 实现时覆盖。
 *
 * @author Deolin 2026-03-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Allison1875ModuleConfig extends Config {

    /** doc-analyzer 功能所使用的 Guice Module 实现类全限定名 */
    String docAnalyzerModule = DocAnalyzerModule.class.getName();

    /** handler-transformer 功能所使用的 Guice Module 实现类全限定名 */
    String handlerTransformerModule = HandlerTransformerModule.class.getName();

    /** persistence-generator 功能所使用的 Guice Module 实现类全限定名 */
    String persistenceGeneratorModule = PersistenceGeneratorModule.class.getName();

    /** query-transformer 功能所使用的 Guice Module 实现类全限定名 */
    String queryTransformerModule = QueryTransformerModule.class.getName();

    /** star-transformer 功能所使用的 Guice Module 实现类全限定名 */
    String starTransformerModule = StarTransformerModule.class.getName();

    /** form-generator 功能所使用的 Guice Module 实现类全限定名 */
    String formGeneratorModule = FormGeneratorModule.class.getName();

}
