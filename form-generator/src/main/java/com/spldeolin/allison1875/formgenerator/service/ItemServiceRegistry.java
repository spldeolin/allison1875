package com.spldeolin.allison1875.formgenerator.service;

import java.util.Map;
import java.util.Set;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;

/**
 * ItemService注册中心，用于根据ItemType获取对应的ItemService实现
 *
 * @author Deolin 2026-02-15
 */
@Singleton
public class ItemServiceRegistry {

    private final Map<ItemType, ItemService<? extends ItemDef>> serviceMap = Maps.newHashMap();

    @Inject
    public ItemServiceRegistry(Set<ItemService<? extends ItemDef>> itemServices) {
        for (ItemService<? extends ItemDef> service : itemServices) {
            ItemType itemType = service.supportedItemType();
            if (itemType != null) {
                serviceMap.put(itemType, service);
            }
        }
    }

    /**
     * 根据ItemType获取对应的ItemService实现
     *
     * @param itemType 字段类型
     * @return 对应的ItemService实现，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public <I extends ItemDef> ItemService<I> getService(ItemType itemType) {
        return (ItemService<I>) serviceMap.get(itemType);
    }

    @SuppressWarnings("unchecked")
    public <I extends ItemDef> ItemService<I> getService(ItemDef itemDef) {
        return (ItemService<I>) serviceMap.get(itemDef.getType());
    }

}
