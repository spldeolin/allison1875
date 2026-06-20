package com.example.roletest.service.impl;

import com.example.roletest.design.*;
import com.example.roletest.entity.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import com.example.roletest.service.RoleService;
import com.example.roletest.mapper.RoleMapper;
import com.example.roletest.dto.req.SaveRoleReq;
import com.example.roletest.dto.resp.SaveRoleResp;
import com.example.roletest.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import com.example.roletest.dto.req.ListRolesReq;
import com.example.roletest.dto.resp.PageResult;
import com.example.roletest.dto.resp.ListRolesResp;
import com.example.roletest.dto.req.GetRoleDetailReq;
import com.example.roletest.dto.resp.GetRoleDetailResp;
import java.time.*;
import java.math.*;
import java.util.*;
import com.example.roletest.dto.req.DeleteRoleReq;
import com.example.roletest.dto.param.QueryRoleParam;

/**
 * @author Deolin
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    private final Long queryRoleTotal = 0L;

    @Transactional
    @Override
    public SaveRoleResp saveRole(SaveRoleReq req) {
        boolean toCreate = req.getRoleCode() == null;
        RoleEntity role;
        if (toCreate) {
            role = new RoleEntity();
            role.setRoleCode(UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
            role.setCreatedAt(LocalDateTime.now());
        } else {
            role = roleMapper.queryByRoleCode(req.getRoleCode());
            if (role == null) {
                throw new BizException("角色不存在或是已被删除");
            }
        }
        RoleEntity existRoleForRoleName = roleMapper.queryRole(req.getRoleCode(), req.getRoleName());
        if (existRoleForRoleName != null) {
            throw new BizException("角色名称已存在");
        }
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setUpdatedAt(LocalDateTime.now());
        if (toCreate) {
            roleMapper.insert(role);
        } else {
            roleMapper.updateById(role);
        }
        return new SaveRoleResp().setRoleCode(role.getRoleCode());
    }

    @Override
    public PageResult<ListRolesResp> listRoles(ListRolesReq req) {
    final QueryRoleParam queryRoleParam = new QueryRoleParam();
    queryRoleParam.setRoleCode(req.getRoleCode());
    queryRoleParam.setRoleName(req.getRoleName());
    queryRoleParam.setDescription(req.getDescription());
    queryRoleParam.setCreatedAt(req.getCreatedAtStart() == null ? null : req.getCreatedAtStart());
    queryRoleParam.setCreatedAtEx(req.getCreatedAtEnd() == null ? null : req.getCreatedAtEnd());
    queryRoleParam.setOffset((req.getPageNum() - 1) * req.getPageSize());
    queryRoleParam.setLimit(req.getPageSize());
    long queryRoleExTotal = roleMapper.countRole(queryRoleParam);
    List<RoleEntity> roles = roleMapper.queryRoleEx(queryRoleParam);
        if (roles.isEmpty()) {
            return PageResult.empty();
        }
        List<ListRolesResp> dtos = new ArrayList<>();
        for (RoleEntity role : roles) {
            ListRolesResp dto = new ListRolesResp();
            dto.setRoleCode(role.getRoleCode());
            dto.setRoleName(role.getRoleName());
            dto.setDescription(role.getDescription());
            dto.setCreatedAt(role.getCreatedAt());
            dto.setUpdatedAt(role.getUpdatedAt());
            dtos.add(dto);
        }
        return PageResult.of(queryRoleTotal, dtos);
    }

    @Override
    public GetRoleDetailResp getRoleDetail(GetRoleDetailReq req) {
        //查询角色
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleCode());
        if (role == null) {
            throw new RuntimeException("角色不存在或是已被删除");
        }
        //构建返回值
        GetRoleDetailResp result = new GetRoleDetailResp();
        result.setRoleCode(role.getRoleCode());
        result.setRoleName(role.getRoleName());
        result.setDescription(role.getDescription());
        result.setCreatedAt(role.getCreatedAt());
        result.setUpdatedAt(role.getUpdatedAt());
        return result;
    }

    @Transactional
    @Override
    public void deleteRole(DeleteRoleReq req) {
    int deleteRoleCount = roleMapper.deleteRole(req.getRoleCodes());
    }
}
