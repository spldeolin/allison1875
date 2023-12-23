package com.spldeolin.allison1875.inspector.javabean;

import java.nio.file.Path;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-07
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VcsResultDto {

     Boolean isAllTarget;

     Set<Path> addedFiles;

}