package com.sheahorn.llmtoolbox.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "llm_providers")
public class Provider extends PanacheEntityBase {

    @Id
    @Column(length = 36)
    public String id;

    @Column(unique = true, nullable = false, length = 256)
    public String name;

    @Column(nullable = false, length = 1024)
    public String baseUrl;

    @Column(length = 4096)
    public String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public ApiType apiType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public ApiMode apiMode = ApiMode.chat;

    public static Provider create(String name, String baseUrl, String apiKey, ApiType apiType, ApiMode apiMode) {
        Provider p = new Provider();
        p.id = UUID.randomUUID().toString();
        p.name = name;
        p.baseUrl = baseUrl;
        p.apiKey = apiKey;
        p.apiType = apiType;
        p.apiMode = apiMode != null ? apiMode : ApiMode.chat;
        return p;
    }
}
