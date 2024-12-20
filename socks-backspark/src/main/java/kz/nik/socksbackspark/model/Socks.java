package kz.nik.socksbackspark.model;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "socks")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Socks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "color")
    private String color;
    @Column(name = "cottonPercentage")
    private int cottonPercentage;
    @Column(name = "quantity")
    private int quantity;
}
