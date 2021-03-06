package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Delayed;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository,TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public ResponseEntity buy(Trade trade, int id) {
//      此排名上的热搜没有人购买
    RsEventDto rsEventDto = rsEventRepository.findByRank(trade.getRank());
    if (rsEventDto == null) {
//      更新rsEvent的rank
      return updateRsEventAndSaveTrade(trade, id);
    }
    if (trade.getAmount()>rsEventDto.getTradeDto().getAmount()){
//      删除原rank上的热搜
      rsEventRepository.delete(rsEventDto);

//      购买成功
      return updateRsEventAndSaveTrade(trade, id);
    }
    return ResponseEntity.status(400).build();
  }

  private ResponseEntity updateRsEventAndSaveTrade(Trade trade, int id) {
    TradeDto buildTradeDto = TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).build();
    tradeRepository.save(buildTradeDto);
    RsEventDto rsEventDto = rsEventRepository.findById(id).get();
    rsEventDto.setRank(trade.getRank());
    rsEventDto.setTradeDto(buildTradeDto);
    rsEventRepository.save(rsEventDto);
    return ResponseEntity.created(null).build();
  }
}
