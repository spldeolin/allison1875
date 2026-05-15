package com.example.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class BookResp {

    /**
     * ID
     */
    private Long id;

    /**
     * 书名
     */
    private String title;

    /**
     * ISBN编号
     */
    private String isbn;

}
