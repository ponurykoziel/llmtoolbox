package com.sheahorn.llmtoolbox.basics.notes;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "notes")
public class Note extends PanacheEntity {

    @Column(nullable = false, length = 1024)
    public String title;

    @Column(nullable = false, length = 65536)
    public String content;
}
