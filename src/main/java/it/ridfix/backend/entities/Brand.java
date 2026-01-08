package it.ridfix.backend.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "brands",
        uniqueConstraints = @UniqueConstraint(name = "uk_brands_name", columnNames = "name"))
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String name;

    public Brand() {}

    public Brand(String name) { this.name = name; }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
