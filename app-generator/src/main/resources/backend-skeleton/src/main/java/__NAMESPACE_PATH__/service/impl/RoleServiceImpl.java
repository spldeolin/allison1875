package __NAMESPACE__.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.common.CurrentUser;
import __NAMESPACE__.dto.param.QueryRoleParam;
import __NAMESPACE__.dto.req.CreateRoleReq;
import __NAMESPACE__.dto.req.DeleteRoleReq;
import __NAMESPACE__.dto.req.GetRoleDetailReq;
import __NAMESPACE__.dto.req.ListRolesReq;
import __NAMESPACE__.dto.req.UpdateRoleReq;
import __NAMESPACE__.dto.resp.CreateRoleResp;
import __NAMESPACE__.dto.resp.GetRoleDetailResp;
import __NAMESPACE__.dto.resp.ListRolesResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.entity.*;
import __NAMESPACE__.enums.AuditOperationTypeEnum;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.service.AuditLogFacade;
import __NAMESPACE__.service.RoleService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private AuditLogFacade auditLogFacade;

    @Transactional
    @Override
    public CreateRoleResp createRole(CreateRoleReq req) {
        RoleEntity role = new RoleEntity();
        role.setRoleCode(UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
        RoleEntity existRoleForRoleName = roleMapper.queryRole(null, req.getRoleName());
        if (existRoleForRoleName != null) {
            throw new BizException("角色名称已存在");
        }
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setCreatedAt(LocalDateTime.now());
        role.setCreatedBy(CurrentUser.getUsername());
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(CurrentUser.getUsername());
        roleMapper.insert(role);
        Map<String, Object> auditContent = new LinkedHashMap<>();
        auditContent.put("角色ID", role.getRoleCode());
        auditContent.put("角色名称", req.getRoleName());
        auditContent.put("角色描述", req.getDescription());
        auditLogFacade.logSuccess(AuditOperationTypeEnum.CREATE_ROLE, auditContent);
        return new CreateRoleResp().setRoleCode(role.getRoleCode());
    }

    @Transactional
    @Override
    public void updateRole(UpdateRoleReq req) {
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleCode());
        if (role == null) {
            throw new BizException("角色不存在或是已被删除");
        }
        RoleEntity existRoleForRoleName = roleMapper.queryRole(req.getRoleCode(), req.getRoleName());
        if (existRoleForRoleName != null) {
            throw new BizException("角色名称已存在");
        }
        Map<String, Object> oldValues = new LinkedHashMap<>();
        oldValues.put("角色ID", role.getRoleCode());
        oldValues.put("角色名称", role.getRoleName());
        oldValues.put("角色描述", role.getDescription());
        Map<String, Object> newValues = new LinkedHashMap<>();
        newValues.put("角色ID", role.getRoleCode());
        newValues.put("角色名称", req.getRoleName());
        newValues.put("角色描述", req.getDescription());
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(CurrentUser.getUsername());
        roleMapper.updateById(role);
        auditLogFacade.logUpdateSuccess(AuditOperationTypeEnum.UPDATE_ROLE, oldValues, newValues);
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
        queryRoleParam.setSortBy(req.getSortBy() != null ? req.getSortBy().getCode() : null);
        queryRoleParam.setIsAsc(req.getIsAsc());
        long queryRoleTotal = roleMapper.countRole(queryRoleParam);
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
            dto.setCreatedBy(role.getCreatedBy());
            dto.setUpdatedAt(role.getUpdatedAt());
            dto.setUpdatedBy(role.getUpdatedBy());
            dtos.add(dto);
        }
        return PageResult.of(queryRoleTotal, dtos);
    }

    @Override
    public GetRoleDetailResp getRoleDetail(GetRoleDetailReq req) {
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleCode());
        if (role == null) {
            throw new RuntimeException("角色不存在或是已被删除");
        }
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
        List<RoleEntity> roles = roleMapper.queryByRoleCodes(req.getRoleCodes());
        Map<String, Object> auditContent = new LinkedHashMap<>();
        List<Map<String, String>> deletedRoles = new ArrayList<>();
        for (RoleEntity role : roles) {
            Map<String, String> roleInfo = new LinkedHashMap<>();
            roleInfo.put("角色名称", role.getRoleName());
            roleInfo.put("角色ID", role.getRoleCode());
            deletedRoles.add(roleInfo);
        }
        auditContent.put("删除的角色", deletedRoles);
        int deleteRoleCount = roleMapper.deleteRole(req.getRoleCodes());
        auditLogFacade.logSuccess(AuditOperationTypeEnum.DELETE_ROLE, auditContent);
    }
}
