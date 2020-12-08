package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.nio.file.Path;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-08
 */
@Data
@Accessors(chain = true)
public class PathDto {

    private Path hostPath;

    private Path sourceRoot;

    private Path mapperXmlPath;

}