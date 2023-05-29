package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.stmt.Statement;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-07-14
 */
@Data
@Accessors(chain = true)
public class MapOrMultimapBuiltDto {

    private List<Statement> statements;

}