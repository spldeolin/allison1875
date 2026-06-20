package com.example.roletest.service;

import com.example.roletest.design.*;
import com.example.roletest.entity.*;
import java.util.*;
import java.time.*;
import java.math.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import com.example.roletest.dto.resp.SaveRoleResp;
import com.example.roletest.dto.req.SaveRoleReq;
import com.example.roletest.dto.resp.PageResult;
import com.example.roletest.dto.resp.ListRolesResp;
import com.example.roletest.dto.req.ListRolesReq;
import com.example.roletest.dto.resp.GetRoleDetailResp;
import com.example.roletest.dto.req.GetRoleDetailReq;
import com.example.roletest.dto.req.DeleteRoleReq;

/**
 * @author Deolin
 */
public interface RoleService {

    SaveRoleResp saveRole(SaveRoleReq req);

    PageResult<ListRolesResp> listRoles(ListRolesReq req);

    GetRoleDetailResp getRoleDetail(GetRoleDetailReq req);

    void deleteRole(DeleteRoleReq req);
}
