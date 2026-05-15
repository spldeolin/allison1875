package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.BookResp;

/**
 * 图书管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    /**
     * 通过SingleMember别名查询图书
     *
     * @param bookId 图书ID
     */
    @GetMapping("/{bookId}")
    public BookResp getByPathVarSingle(@PathVariable("bookId") Long id) {
        return null;
    }

    /**
     * 通过name属性别名查询图书
     *
     * @param isbn ISBN编号
     */
    @GetMapping("/by-isbn/{isbn}")
    public BookResp getByPathVarName(@PathVariable(name = "isbn") String isbnCode) {
        return null;
    }

    /**
     * 搜索图书
     *
     * @param keyword 搜索关键字
     * @param pageNo 页码
     * @param active 是否启用
     */
    @GetMapping("/search")
    public BookResp search(
            @RequestParam("keyword") String kw,
            @RequestParam(value = "page_no", required = false) int pageNo,
            @RequestParam(required = false) boolean active) {
        return null;
    }

}
