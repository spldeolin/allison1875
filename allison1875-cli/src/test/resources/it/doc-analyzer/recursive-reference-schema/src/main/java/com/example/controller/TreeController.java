package com.example.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.TreeNodeDTO;

/**
 * 树形结构管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/trees")
public class TreeController {

    /**
     * 获取整棵树（自引用DTO：children: List&lt;TreeNodeDTO&gt;）
     */
    @GetMapping
    public List<TreeNodeDTO> getTree() {
        return null;
    }

}
