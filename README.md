# Authorization Engine

A Spring Boot based authorization engine focused on **fine-grained access control** for backend applications.

This project explores and implements a hybrid authorization model using:

- **RBAC** for role-based permissions
- **ABAC** for attribute-driven checks
- **Policy-based authorization** for flexible access rules
- **Custom decision voters** for extensible authorization decisions
- **Audit logging** for traceability
- **Performance-aware permission caching** to reduce repeated authorization cost

At the current stage, the project skeleton and core structure have been set up, and the repository is being evolved feature by feature.

## What this project is about

In many applications, simple role checks such as `ADMIN` or `USER` are not enough. Real systems often need decisions based on multiple factors such as:

- who the user is
- what role they have
- which tenant or organization they belong to
- what resource they are trying to access
- what action they are trying to perform
- contextual attributes such as ownership, state, or environment

This project aims to move beyond basic role checks and support more realistic backend authorization scenarios.

## Goals

- build a reusable authorization architecture in Spring Boot
- support both role-based and attribute-based access control
- model permissions in a way that is extensible and maintainable
- provide a clear decision flow for allow/deny evaluation
- keep the design practical for real backend systems
- make the project a learning-focused but production-minded backend codebase

## Current status

### Completed so far

- Initial Spring Boot project structure created
- Security configuration added
- User registration flow currently enabled
- Foundation prepared for hybrid RBAC + ABAC authorization flow
-  Authorization engine skeleton established

### Important current behavior

The authorization layer is still under active development, so most endpoints are not fully protected yet.

At the current stage, parts of the application are intentionally configured in a permissive way to make development and feature-by-feature integration easier. This behavior is currently controlled through `SecurityConfig.java`, including the `filterChain` configuration.

This means the project structure for authentication and authorization is being built first, while stricter endpoint-level protection will be added gradually as the authorization flow becomes complete.

## Planned architecture

- authentication layer
- authorization evaluation layer
- role and permission management
- policy evaluation flow
- custom access decision components
- audit trail for authorization outcomes
- caching layer for permission resolution
- tenant-aware or domain-aware authorization support

## Use cases this project is targeting

- admin vs normal user permission boundaries
- resource ownership checks
- tenant-scoped access decisions
- feature/module-level permissions
- action-level authorization such as read, write, update, delete, approve
- context-sensitive authorization rules

## Tech stack

- Java
- Spring Boot
- Spring Security
- Gradle
- Backend authorization design patterns

## Why this project exists

This repository is part of a deeper learning effort around backend engineering, security, and access control design. The goal is not just to make something work, but to understand how permissions should be modeled, where authorization logic should live, and how to keep it flexible as business rules grow.

## Roadmap

- define roles, permissions, and policies
- add authorization evaluation flow
- introduce custom voters or decision components
- add audit logging
- add caching for repeated permission checks
- add tests for authorization scenarios
- improve API documentation and example flows

## Notes

This project is under active development. Some areas currently represent structure and intent more than fully completed business behavior. This README will be updated as more features become concrete.
