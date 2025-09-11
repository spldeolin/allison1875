package com.spldeolin.allison1875.common.guice;

import java.util.List;
import com.google.inject.spi.ProvisionListener;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.ValidUtils;

/**
 * @author Deolin 2025-09-11
 */
public class ValidSingletonListener implements ProvisionListener {

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        // 先让Guice创建实例
        Object instance = provision.provision();

        // 对实例执行校验
        List<InvalidDTO> invalids = ValidUtils.valid(instance);
        if (!invalids.isEmpty()) {
            throw new Allison1875Exception(
                    "Allison 1875 fail to work cause invalid config\ninvalids=" + JsonUtils.toJsonPrettily(invalids));
        }
    }

}
