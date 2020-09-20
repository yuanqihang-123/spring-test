package com.thoughtworks.rslist.dto;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "trade")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDto {
    @Id
    @GeneratedValue
    private Integer id;
    private int amount;
    private int rank;
    @OneToOne
    @JoinColumn(name = "rsEventDto_id",referencedColumnName = "id")
    private RsEventDto rsEventDto;
}
