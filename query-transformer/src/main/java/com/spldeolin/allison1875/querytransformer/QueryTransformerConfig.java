package com.spldeolin.allison1875.querytransformer;

import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-09
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class QueryTransformerConfig extends Allison1875Config {

}