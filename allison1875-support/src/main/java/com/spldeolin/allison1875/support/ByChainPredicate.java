package com.spldeolin.allison1875.support;

import java.util.Collection;

/**
 * by()调用链的谓语
 *
 * 适配Allison 1875 query-transformer
 *
 * @author Deolin 2020-01-11
 */
public interface ByChainPredicate<E, C> {

    /**
     * EQual
     */
    E eq(C value);

    /**
     * Not Equal
     */
    E ne(C value);

    /**
     * IN
     */
    E in(Collection<C> values);

    /**
     * Not IN
     */
    E nin(Collection<C> values);

    /**
     * Greater Than
     */
    E gt(C value);

    /**
     * Greater or Equals
     */
    E ge(C value);

    /**
     * Lesser Than
     */
    E lt(C value);

    /**
     * Lesser or Equal
     */
    E le(C value);

    /**
     * NOT NULL
     */
    E notnull();

    /**
     * IS NULL
     */
    E isnull();

    /**
     * LIKE
     */
    E like(C value);

}