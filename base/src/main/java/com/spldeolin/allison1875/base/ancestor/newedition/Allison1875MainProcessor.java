package com.spldeolin.allison1875.base.ancestor.newedition;

import java.util.Set;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * @author Deolin 2021-06-15
 */
public abstract class Allison1875MainProcessor extends Allison1875Component {

    @Override
    protected void configure() {
        super.configure();
    }

    public abstract void process(AstForest astForest);

    public abstract Set<Class<?>> requiredAllison1875Configs();

}