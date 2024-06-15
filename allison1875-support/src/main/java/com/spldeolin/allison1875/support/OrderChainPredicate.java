package com.spldeolin.allison1875.support;

/**
 * order()调用链的谓语
 *
 * 适配Allison 1875 query-transformer
 *
 * @author Deolin 2021-05-12
 */
public interface OrderChainPredicate<E> {

    E asc();

    E desc();

}