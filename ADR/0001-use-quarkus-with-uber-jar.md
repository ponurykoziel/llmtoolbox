# ADR-0001: Use Quarkus with Uber-Jar Packaging

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox needs a JVM-based HTTP server that exposes REST endpoints for LLM tool integration, serves a browser UI, persists data, and shells out to system commands. The application must be easy to deploy on bare-metal Linux hosts with minimal dependencies.

## Decision

Use **Quarkus 3.17.6** (jakarta namespace) with **uber-jar** packaging (`quarkus.package.jar.type=uber-jar`).

Key dependencies:
- `quarkus-rest-jackson` — REST endpoints with JSON serialization
- `quarkus-hibernate-orm-panache` — Active Record style persistence
- `quarkus-jdbc-h2` — In-memory H2 database
- `quarkus-smallrye-openapi` — Auto-generated OpenAPI 3.1 specs
- `quarkus-elytron-security-jdbc` — Form-based authentication
- Thymeleaf 3.1.2 — Server-side HTML templating

Java 17 is the compile target (`maven.compiler.release=17`).

## Rationale

- **Uber-jar** produces a single self-contained artifact (`llmtoolbox-1.3.0-runner.jar`), simplifying deployment to `java -jar`.
- **Quarkus** provides fast startup, low memory footprint, and native integration with the tools' shell-out execution model.
- **Hibernate Panache** reduces boilerplate for simple CRUD entities (User, ApiKey, Preset, Note, MemoryEntry).
- **SmallRye OpenAPI** auto-generates the OpenAPI spec from JAX-RS annotations, which is then subsetted for LLM tool integration — a core feature.
- **Thymeleaf** provides server-side rendering for the admin UI without requiring a separate frontend build step.
- **Java 17** is the minimum version required by Quarkus 3.x and is an LTS release with broad ecosystem support. It is widely available in Linux package repositories (`apt`, `dnf`, `pacman`), pre-installed on many LTS distributions, and avoids the still-evolving adoption curve of Java 21+. The application can run on any JRE ≥17 (the Docker helper script uses `eclipse-temurin:21-jre`, demonstrating forward compatibility). No features from Java 18+ are used, maximizing the range of deployable hosts without requiring operators to install a newer JDK.

## Consequences

- The application is tied to the Quarkus ecosystem; migration to another framework would require significant rework.
- Uber-jar means the entire application (including all dependencies) is repackaged on every build.
- H2 in-memory means all data is volatile — acceptable for a tool-oriented application where persistence is secondary.
