package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import org.springframework.data.repository.CrudRepository;

public interface TradeRepository extends CrudRepository<TradeDto, Integer> {
    TradeDto findByAmountAndRank(int amount,int rank);
    TradeDto findByRsEventDto(RsEventDto rsEventDto);
    TradeDto findByRank(int rank);
}
