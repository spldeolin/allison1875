package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Collection;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-07-14
 */
@Data
@Accessors(chain = true)
public class MapOrMultimapBuiltDto {

    private String code;

    private final Collection<String> imports = Lists.newArrayList();

}