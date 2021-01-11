package com.spldeolin.allison1875.support;

import java.util.Collection;

/**
 * 适配Allison 1875 query-transformer
 *
 * @author Deolin 2020-01-11
 */
public interface QueryPredicate<E, C> {

    E eq(C value);

    E ne(C value);

    E in(Collection<C> values);

    E nin(Collection<C> values);

    E gt(C value);

    E ge(C value);

    E lt(C value);

    E le(C value);

    E notnull();

    E isnull();

    E like(C value);

}