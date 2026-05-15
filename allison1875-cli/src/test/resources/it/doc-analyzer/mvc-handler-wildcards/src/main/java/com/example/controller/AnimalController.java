package com.example.controller;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateAnimalReq;
import com.example.dto.resp.AnimalResp;

/**
 * 动物管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    /**
     * 查询动物列表
     */
    @GetMapping
    public List<AnimalResp> listAnimals() {
        return null;
    }

    /**
     * 根据ID查询动物详情
     */
    @GetMapping("/{id}")
    public AnimalResp getById(@PathVariable Long id) {
        return null;
    }

    /**
     * 创建动物
     */
    @PostMapping
    public AnimalResp createAnimal(@RequestBody CreateAnimalReq req) {
        return null;
    }

    /**
     * 删除动物
     */
    @DeleteMapping("/{id}")
    public void deleteAnimal(@PathVariable Long id) {
    }

}
