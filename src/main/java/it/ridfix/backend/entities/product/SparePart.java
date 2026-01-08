package it.ridfix.backend.entities.product;

import it.ridfix.backend.entities.Brand;
import it.ridfix.backend.entities.Category;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "spare_parts")
@DiscriminatorValue("SPARE_PART")
public class SparePart extends Product {

    @Column(length = 60)
    private String oemCode;

    @Column(length = 250)
    private String compatibility;

    public SparePart() {}

    public SparePart(String name, String description, BigDecimal price, int stockQty, Category category, Brand brand,
                     String oemCode, String compatibility) {
        super(name, description, price, stockQty, category, brand);
        this.oemCode = oemCode;
        this.compatibility = compatibility;
    }

    public String getOemCode() { return oemCode; }
    public void setOemCode(String oemCode) { this.oemCode = oemCode; }
    public String getCompatibility() { return compatibility; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }
}
