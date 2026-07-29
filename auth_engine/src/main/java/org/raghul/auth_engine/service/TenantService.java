package org.raghul.auth_engine.service;

import org.raghul.auth_engine.dto.RegisterTenantRequest;
import org.raghul.auth_engine.entity.TenantEntity;
import org.raghul.auth_engine.repository.TenantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantService {


    TenantRepo tenantRepo;

    @Autowired
    public TenantService(TenantRepo tenantRepo){this.tenantRepo = tenantRepo;}




}
