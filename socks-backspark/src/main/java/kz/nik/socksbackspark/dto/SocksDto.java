package kz.nik.socksbackspark.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocksDto {
    private Long id;
    private String color;
    private int cottonPercentage;
    private int quantity;
}
