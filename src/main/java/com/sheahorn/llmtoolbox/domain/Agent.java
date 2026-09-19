package com.sheahorn.llmtoolbox.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "llm_agents")
public class Agent extends PanacheEntityBase {

    @Id
    @Column(length = 36)
    public String id;

    @Column(unique = true, nullable = false, length = 256)
    public String niceName;

    @Column(nullable = false, length = 36)
    public String providerId;

    @Column(nullable = false, length = 36)
    public String modelId;

    @Column(nullable = false, length = 36)
    public String personalityId;

    @Column(length = 256)
    public String toolPreset;

    public static Agent create(String niceName, String providerId, String modelId, String personalityId, String toolPreset) {
        Agent a = new Agent();
        a.id = UUID.randomUUID().toString();
        a.niceName = niceName;
        a.providerId = providerId;
        a.modelId = modelId;
        a.personalityId = personalityId;
        a.toolPreset = toolPreset;
        return a;
    }
}
