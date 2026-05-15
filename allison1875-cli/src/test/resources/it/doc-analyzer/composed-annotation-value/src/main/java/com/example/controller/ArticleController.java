package com.example.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateArticleReq;
import com.example.dto.req.UpdateArticleReq;
import com.example.dto.resp.ArticleResp;

/**
 * 文章管理
 *
 * @author test-author 2026-05-13
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    /**
     * 获取文章详情
     */
    @GetMapping("/detail")
    public ArticleResp getDetail() {
        return null;
    }

    /**
     * 创建文章
     */
    @PostMapping("/create")
    public ArticleResp createArticle(@RequestBody CreateArticleReq req) {
        return null;
    }

    /**
     * 更新文章
     */
    @PutMapping("/update")
    public ArticleResp updateArticle(@RequestBody UpdateArticleReq req) {
        return null;
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/delete")
    public void deleteArticle() {
    }

    /**
     * 局部更新文章
     */
    @PatchMapping("/patch")
    public ArticleResp patchArticle(@RequestBody UpdateArticleReq req) {
        return null;
    }

}