package com.spldeolin.allison1875.mojo;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.enums.ToolEnum;

/**
 * Form Generator Mojo
 *
 * @author Deolin 2024-06-14
 */
@Mojo(name = "form-generator", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
public class FormGeneratorMojo extends Allison1875Mojo {

    @Override
    protected ToolEnum getTool() {
        return ToolEnum.FORM_GENERATOR;
    }

    @Override
    protected void resolveBasedirPaths(Config config) {
        if (config.getDslPath() != null) {
            config.setDslPath(getCanonicalFileRelativeToBasedir(config.getDslPath()));
        }
    }

}
