package com.spldeolin.allison1875.inspector.javabean;

import java.nio.file.Path;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-07
 */
@Data
@Accessors(chain = true)
public class VcsResultDto {

    private Boolean isAllTarget;

    private Set<Path> addedFiles;

}