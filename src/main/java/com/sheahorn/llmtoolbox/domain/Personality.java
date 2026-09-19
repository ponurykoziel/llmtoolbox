package com.sheahorn.llmtoolbox.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "llm_personalities")
public class Personality extends PanacheEntityBase {

    @Id
    @Column(length = 36)
    public String id;

    @Column(unique = true, nullable = false, length = 256)
    public String niceName;

    @Column(length = 8192)
    public String systemPrompt;

    @Column(nullable = false)
    public Double temperature = -1.0;

    @Column(nullable = false)
    public Double minP = -1.0;

    @Column(nullable = false)
    public Double topP = -1.0;

    @Column(nullable = false)
    public Double topK = -1.0;

    @Column(nullable = false)
    public Double frequencyPenalty = -1.0;

    @Column(nullable = false)
    public Double presencePenalty = -1.0;

    @Column(length = 64)
    public String reasoningEffort;

    public static Personality create(String niceName) {
        Personality p = new Personality();
        p.id = UUID.randomUUID().toString();
        p.niceName = niceName;
        return p;
    }

    /** Rounds to 0.01, clamps to -1.0 if negative. */
    public static Double sanitizeParam(Double value) {
        if (value == null || value < 0.0) return -1.0;
        return Math.round(value * 100.0) / 100.0;
    }
}
