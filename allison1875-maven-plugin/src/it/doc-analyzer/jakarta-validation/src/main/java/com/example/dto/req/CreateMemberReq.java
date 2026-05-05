package com.example.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class CreateMemberReq {

    /**
     * 会员名称
     */
    @NotBlank
    @Size(min = 2, max = 50)
    private String memberName;

    /**
     * 会员等级
     */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer level;

    /**
     * 出生日期
     */
    @Past
    private Date birthday;

}
