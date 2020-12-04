package com.spldeolin.allison1875.base;

import java.util.Collection;
import java.util.function.Supplier;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * Allison1875的嵌入式启动类
 *
 * @author Deolin 2020-08-29
 */
public class Allison1875 {

    private Class<?> primaryClass;

    private final Collection<Allison1875MainProcessor<?, ?>> processors = Lists.newArrayList();

    private Allison1875() {
    }

    public static Allison1875 allison1875() {
        return new Allison1875();
    }

    public Allison1875 primaryClass(Class<?> primaryClass) {
        this.primaryClass = primaryClass;
        return this;
    }

    public Allison1875 install(Supplier<? extends Allison1875MainProcessor<?, ?>> installation) {
        processors.add(installation.get());
        return this;
    }

    public Allison1875 launch() {
        if (primaryClass == null) {
            throw new IllegalArgumentException("必须指定primaryClass");
        }
        AstForest astForest = new AstForest(primaryClass);
        for (Allison1875MainProcessor<?, ?> processor : processors) {
            processor.process(astForest);
        }
        return this;
    }

}