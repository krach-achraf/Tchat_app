package com.tchat.ms_authentification.service.impl;

import com.tchat.ms_authentification.bean.Role;
import com.tchat.ms_authentification.dao.RoleDao;
import com.tchat.ms_authentification.service.facade.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private RoleDao roleDao;

    @Override
    public Role findByAuthority(String authority) {
        return roleDao.findByAuthority(authority);
    }

    public Role save(Role role) {
        return roleDao.save(role);
    }
}
