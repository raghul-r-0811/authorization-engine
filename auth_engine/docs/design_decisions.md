Date:15/07/2026

Before this point, the users can self-register, but that is being stopped from now.

Why?
    The idea is to build the auth-engine which can work for a multi-tenant environment.
So having self registration breaks the DB table constraint, and it is not the standard practice it seems in real world scenario.

Decision Implemented : No self-registeration, tenant_admin can add users with their tenant_id, and other details  later user can set password for login.

Alternative ideas considered:
    
1. Self-registration allowed but stored in different table and Tenant_admin will add that users with added tenant_id.
2. Self-registration allowed but stored in the same table with default tenant_id and admins can change the org_id/tenant_id later.

The ideads are scratched because it felt too eaaahn and broke some other DB table restrictions, like one admin cant view the users of other tenant id something like that.