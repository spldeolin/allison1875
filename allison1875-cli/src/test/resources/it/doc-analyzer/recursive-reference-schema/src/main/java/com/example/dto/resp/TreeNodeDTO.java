package com.example.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 树节点DTO（含自引用字段 children: List&lt;TreeNodeDTO&gt;，形成树形递归结构）
 *
 * @author test-author 2026-04-29
 */
@Data
public class TreeNodeDTO {

    /**
     * 节点ID
     */
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 子节点列表（自引用，递归结构）
     */
    private List<TreeNodeDTO> children;

}
