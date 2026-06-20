package com.example.roletest.dto.req;

import com.example.roletest.design.*;
import com.example.roletest.entity.*;
import java.util.*;
import java.time.*;
import java.math.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Deolin 2026-06-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SaveRoleReq {

    /**
     * 角色的业务ID
     */
    String roleCode;

    /**
     * 角色名称
     */
    @NotBlank
    @Size(max = 32)
    String roleName;

    /**
     * 角色描述
     */
    @Size(max = 128)
    String description;
}
