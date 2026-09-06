package com.sheahorn.llmtoolbox.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "llm_models")
public class Model extends PanacheEntityBase {

    @Id
    @Column(length = 36)
    public String id;

    @Column(nullable = false, length = 36)
    public String providerId;

    @Column(unique = true, nullable = false, length = 256)
    public String niceName;

    @Column(nullable = false, length = 256)
    public String providerName;

    public static Model create(String providerId, String niceName, String providerName) {
        Model m = new Model();
        m.id = UUID.randomUUID().toString();
        m.providerId = providerId;
        m.niceName = niceName;
        m.providerName = providerName;
        return m;
    }
}
