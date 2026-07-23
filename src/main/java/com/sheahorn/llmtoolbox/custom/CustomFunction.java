package com.sheahorn.llmtoolbox.custom;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "custom_functions")
public class CustomFunction extends PanacheEntityBase {

    @Id
    @Column(length = 36)
    public String id;

    @Column(unique = true, nullable = false, length = 256)
    public String operationId;

    @Column(length = 1024)
    public String description;

    @Column(nullable = false, length = 4096)
    public String shellCommand;

    public static CustomFunction create(String operationId, String description, String shellCommand) {
        CustomFunction f = new CustomFunction();
        f.id = UUID.randomUUID().toString();
        f.operationId = operationId;
        f.description = description;
        f.shellCommand = shellCommand;
        return f;
    }
}
