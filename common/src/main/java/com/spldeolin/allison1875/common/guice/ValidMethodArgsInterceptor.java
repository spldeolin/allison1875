package com.spldeolin.allison1875.common.guice;

import java.lang.reflect.Method;
import java.util.List;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.ValidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-14
 */
@Slf4j
public class ValidMethodArgsInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();

        for (int i = 0; i < methodInvocation.getArguments().length; i++) {
            Object args = methodInvocation.getArguments()[i];
            if (args == null) {
                continue;
            }
            List<InvalidDTO> invalids = ValidUtils.valid(args);
            if (CollectionUtils.isNotEmpty(invalids)) {
                throw new Allison1875Exception(String.format(
                        "Allison 1875 fail to work cause method '%s.%s' encounter invalid argument\ninvalids=%s",
                        methodInvocation.getThis().getClass().getSimpleName(), method.getName(),
                        JsonUtils.toJsonPrettily(invalids)));
            }
        }

        return methodInvocation.proceed();
    }

}