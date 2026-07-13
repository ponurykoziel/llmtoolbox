package com.sheahorn.llmtoolbox.basics.presets;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "presets")
public class Preset extends PanacheEntityBase {

    @Id
    @Column(nullable = false, unique = true, length = 128)
    public String name;

    @Column(nullable = false, length = 4096)
    public String prefixes;

    public static Optional<Preset> byName(String name) {
        return find("name", name).firstResultOptional();
    }

    public static List<Preset> all() {
        return listAll();
    }

    public List<String> prefixList() {
        if (prefixes == null || prefixes.isBlank()) {
            return List.of();
        }
        return List.of(prefixes.split(",")).stream()
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
