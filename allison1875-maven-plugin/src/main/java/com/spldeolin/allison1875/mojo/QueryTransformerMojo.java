package com.spldeolin.allison1875.mojo;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.enums.ToolEnum;

/**
 * @author Deolin 2024-06-14
 */
@Mojo(name = "query-transformer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
public class QueryTransformerMojo extends Allison1875Mojo {

    @Override
    protected ToolEnum getTool() {
        return ToolEnum.QUERY_TRANSFORMER;
    }

}
