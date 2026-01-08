package it.ridfix.backend.entities.product;

import it.ridfix.backend.entities.Brand;
import it.ridfix.backend.entities.Category;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "accessories")
@DiscriminatorValue("ACCESSORY")
public class Accessory extends Product {

    @Column(length = 60)
    private String material;

    @Column(length = 40)
    private String color;

    public Accessory() {}

    public Accessory(String name, String description, BigDecimal price, int stockQty, Category category, Brand brand,
                     String material, String color) {
        super(name, description, price, stockQty, category, brand);
        this.material = material;
        this.color = color;
    }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
