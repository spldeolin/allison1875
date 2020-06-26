package com.spldeolin.allison1875.handlergenerator.util;

/**
 * @author Deolin 2020-06-27
 */
@FunctionalInterface
public interface TriConsumer<X, Y, Z> {

    void accept(X x, Y y, Z z);

}
