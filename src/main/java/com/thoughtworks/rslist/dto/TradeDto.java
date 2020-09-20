package com.thoughtworks.rslist.dto;

import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;

@Entity
@Table(name = "trade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDto {
    @Id
    @GeneratedValue
    private Integer id;
    private int amount;
    private int rank;

}
