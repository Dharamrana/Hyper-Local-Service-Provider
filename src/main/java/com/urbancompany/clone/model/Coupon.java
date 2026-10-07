package com.urbancompany.clone.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity @Table(name="coupons") @Data @NoArgsConstructor @AllArgsConstructor
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true) private String code; // FIRST100, PREMNAGAR50
    private Double percentOff; private Double maxDiscount; private Double minOrder;
    private LocalDate validTill; private Boolean active = true;
}
