package com.example.roletest.dto.resp;

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

/**
 * @author Deolin 2026-06-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SaveRoleResp {

    /**
     * 角色的业务ID
     */
    String roleCode;
}
