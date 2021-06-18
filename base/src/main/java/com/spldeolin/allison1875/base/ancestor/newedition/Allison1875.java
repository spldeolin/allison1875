package com.spldeolin.allison1875.base.ancestor.newedition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.base.Preconditions;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.GuiceUtils;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-15
 */
@Log4j2
public class Allison1875 {

    public static void allison1875(Class<?> primaryClass, Allison1875Component... allison1875Components) {
        Preconditions.checkNotNull(primaryClass, "required 'primaryClass' Parameter cannot be null");
        Preconditions.checkArgument(allison1875Components.length == 0,
                "requried 'allison1875Components' Parameter cannot be empty");

        Set<Class<?>> componentTypes = listTypes(allison1875Components);
        List<Allison1875Config> configs = listConfigs(allison1875Components);
        List<Allison1875MainProcessor> mainProcessors = listMainProcessors(allison1875Components);

        for (Allison1875MainProcessor mainProcessor : mainProcessors) {
            for (Class<?> requiredConfig : mainProcessor.requiredAllison1875Configs()) {
                if (!componentTypes.contains(requiredConfig)) {
                    throw new IllegalArgumentException(
                            String.format("required %s config is not present in arguments", componentTypes));
                }
            }
        }

        GuiceUtils.createInjector(allison1875Components);
        AstForest astForest = new AstForest(primaryClass, false);

        ValidateUtils.ensureValid(configs);

        for (Allison1875MainProcessor mainProcessor : mainProcessors) {
            log.info("launch [{}]", mainProcessor.getClass().getSimpleName());
            mainProcessor.process(astForest.reset());
        }

    }

    private static Set<Class<?>> listTypes(Allison1875Component[] components) {
        return Arrays.stream(components).map(Allison1875Component::getClass).collect(Collectors.toSet());
    }

    private static List<Allison1875Config> listConfigs(Allison1875Component[] components) {
        return Arrays.stream(components).filter(c -> c instanceof Allison1875Config).map(c -> (Allison1875Config) c)
                .collect(Collectors.toList());
    }

    private static List<Allison1875MainProcessor> listMainProcessors(Allison1875Component[] components) {
        return Arrays.stream(components).filter(c -> c instanceof Allison1875MainProcessor)
                .map(c -> (Allison1875MainProcessor) c).collect(Collectors.toList());
    }

}