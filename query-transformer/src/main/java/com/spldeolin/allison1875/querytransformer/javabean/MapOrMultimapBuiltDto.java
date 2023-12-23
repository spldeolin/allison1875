package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import com.github.javaparser.ast.stmt.Statement;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-07-14
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapOrMultimapBuiltDto {

    List<Statement> statements;

}