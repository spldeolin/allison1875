package com.spldeolin.allison1875.inspector.service;

import java.nio.file.Path;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.inspector.javabean.VcsResultDto;
import com.spldeolin.allison1875.inspector.service.impl.VcsServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(VcsServiceImpl.class)
public interface VcsService {

    VcsResultDto process(Path projectPath);

}