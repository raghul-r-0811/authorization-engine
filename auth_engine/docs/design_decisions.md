# Major Design Changes

## 15/07/2026 - Self-registration Removed

### Context

Initially, users were allowed to self-register. This approach was later reconsidered as the application evolved toward a multi-tenant authorization engine.

### Problem

Allowing self-registration created issues with the tenant-aware data model. A newly self-registered user would not naturally belong to a valid tenant boundary, which conflicted with database constraints and with the intended multi-tenant design.

It also did not align well with the real-world operational model being targeted, where tenant-scoped user creation is usually controlled by an authorized tenant administrator.

### Decision

Self-registration was removed.

Instead, a `tenant_admin` is responsible for creating users with the correct `tenant_id` and related details. After that, the user can complete account setup by setting a password and then logging in.

### Alternatives Considered

#### 1. Separate self-registration table

Self-registration could have been allowed by storing users in a separate table first. Later, a `tenant_admin` could review and add those users into the main tenant-aware user table with the appropriate `tenant_id`.

#### 2. Same table with default tenant assignment

Self-registration could have been allowed in the same user table by assigning a default `tenant_id`, with the expectation that an admin would later update the tenant or organization mapping.

### Why These Alternatives Were Rejected

Both alternatives introduced unnecessary complexity and weakened tenant isolation.

They also created design discomfort around tenant visibility and ownership rules, such as ensuring that one tenant admin could not improperly interact with users outside their tenant scope.

---

### Login Flow Reason

The first login request is handled directly by the controller for simplicity.

This flow could later be moved into a custom authentication filter that extracts the user email and password from the request and performs the same authentication flow earlier in the security chain.

After the initial login, all protected requests are intercepted by `JwtAuthFilter`.

---

## 25/07/2026 - Roles and Resource Permissions Decoupled

### Context

The earlier authorization design was intentionally simple: one user had one role, and access decisions were mostly role-driven.

### Problem

This model became too restrictive when resource-level access requirements became more specific.

For example, if access to a particular resource must be granted only to selected users within `tenantId = 10`, and those users belong to different roles, the existing role-only model cannot represent that cleanly.

The old design assumed that if a role had a permission, all users with that role would receive it. If a user needed access to a specific resource outside the normal role definition, there was no clear mechanism for granting it.

### Decision

The authorization model now needs to decouple roles from permissions on specific resources.

Roles should still define general access patterns, but resource-specific permissions should be assignable independently when required.

### Why This Change Was Needed

A role-centric model works for broad access control, but it does not scale well when new resources are introduced and access must be granted selectively across users within the same tenant.

Decoupling role assignment from resource-level authorization gives the design more flexibility and makes the system better suited for real multi-tenant use cases.

### ReDesign Intention

The redesign should separate the following concerns:

- user-role assignment
- permission definition
- action-to-resource authorization
- tenant boundary enforcement

This allows the system to support both general RBAC behavior and selective resource-level access where needed.



### TODO

- [X] Register a user with SUPER_ADMIN role with null tenant, Register users with other roles with non null tenant.
- [X] Role validation (is this user_role can be created or not)
- [ ] Permission Enrollment