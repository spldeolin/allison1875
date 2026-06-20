package com.example.roletest.controller;

import com.example.roletest.design.*;
import com.example.roletest.entity.*;
import java.util.*;
import java.time.*;
import java.math.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import com.example.roletest.service.RoleService;
import com.example.roletest.common.RequestResult;
import com.example.roletest.dto.resp.SaveRoleResp;
import com.example.roletest.dto.req.SaveRoleReq;
import com.example.roletest.dto.resp.PageResult;
import com.example.roletest.dto.resp.ListRolesResp;
import com.example.roletest.dto.req.ListRolesReq;
import com.example.roletest.dto.resp.GetRoleDetailResp;
import com.example.roletest.dto.req.GetRoleDetailReq;
import com.example.roletest.dto.req.DeleteRoleReq;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.validation.Valid;

/**
 * 角色
 *
 * @author Deolin
 */
@RestController
@RequestMapping("/api/v1/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping("saveRole")
    public RequestResult<SaveRoleResp> saveRole(@RequestBody @Valid SaveRoleReq req) {
        return RequestResult.success(roleService.saveRole(req));
    }

    /**
     * 角色列表
     */
    @PostMapping("listRoles")
    public RequestResult<PageResult<ListRolesResp>> listRoles(@RequestBody @Valid ListRolesReq req) {
        return RequestResult.success(roleService.listRoles(req));
    }

    /**
     * 角色详情
     */
    @PostMapping("getRoleDetail")
    public RequestResult<GetRoleDetailResp> getRoleDetail(@RequestBody @Valid GetRoleDetailReq req) {
        return RequestResult.success(roleService.getRoleDetail(req));
    }

    /**
     * 删除角色
     */
    @PostMapping("deleteRole")
    public RequestResult<Void> deleteRole(@RequestBody @Valid DeleteRoleReq req) {
        roleService.deleteRole(req);
        return RequestResult.success();
    }
}
