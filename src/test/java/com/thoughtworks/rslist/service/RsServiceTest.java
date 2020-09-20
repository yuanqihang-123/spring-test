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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
    RsService rsService;

    @Mock
    RsEventRepository rsEventRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    VoteRepository voteRepository;
    @Mock
    TradeRepository tradeRepository;
    LocalDateTime localDateTime;
    Vote vote;
    Trade trade;

    @BeforeEach
    void setUp() {
        initMocks(this);
        rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
        localDateTime = LocalDateTime.now();
        vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
        trade = Trade.builder().amount(10).rank(1).build();
    }

    @Test
    void shouldVoteSuccessWhenTheRankHasNoPersonBuy() {
        // given

        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
        // when
        rsService.vote(vote, 1);
        // then
        verify(voteRepository)
                .save(
                        VoteDto.builder()
                                .num(2)
                                .localDateTime(localDateTime)
                                .user(userDto)
                                .rsEvent(rsEventDto)
                                .build());
        verify(userRepository).save(userDto);
        verify(rsEventRepository).save(rsEventDto);
    }

    @Test
    void shouldThrowExceptionWhenUserNotExist() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        //when&then
        assertThrows(
                RuntimeException.class,
                () -> {
                    rsService.vote(vote, 1);
                });
    }

    @Test
    void shouldBuySuccessWhenTheRankNotBuy() {
        // given

        UserDto userDto =
                UserDto.builder()
                        .voteNum(10)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        when(rsEventRepository.findByRank(anyInt())).thenReturn(null);
        when(rsEventRepository.findById(rsEventDto.getId())).thenReturn(Optional.of(rsEventDto));
        // when
        rsService.buy(trade, rsEventDto.getId());
        // then
        TradeDto tradeDto = TradeDto.builder()
                .amount(trade.getAmount())
                .rank(trade.getRank())
                .build();
//        要想比较的参数是相等的，必须重写equals方法，如果不重写，那么直接比较对象引用，肯定是不相等的
//        刚开始踩了坑，tradeDto没有重写equals方法，导致verify一直出错。
        verify(tradeRepository).save(tradeDto);
        verify(rsEventRepository).save(
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .rank(1)
                        .tradeDto(tradeDto)
                        .user(userDto)
                        .build());
    }

    @Test
    void shouldBuyRsWithMoneyMoreThanCurrentMoneySuccess() {
        // given
        TradeDto tradeDto = TradeDto.builder().amount(5).rank(1).id(1).build();
        UserDto userDto =
                UserDto.builder()
                        .voteNum(10)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(3)
                        .keyword("keyword")
                        .voteNum(2)
                        .rank(1)
                        .tradeDto(tradeDto)
                        .user(userDto)
                        .build();
        RsEventDto rsEventDto1 =
                RsEventDto.builder()
                        .eventName("猪肉涨价了")
                        .id(4)
                        .keyword("经济")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        // when
        when(rsEventRepository.findByRank(anyInt())).thenReturn(rsEventDto);
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto1));
        //先给when，再调用，不然when不起作用
        rsService.buy(trade, rsEventDto1.getId());
        // then

        verify(rsEventRepository).delete(rsEventDto);
        TradeDto tradeDto1 = TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).build();
        verify(tradeRepository).save(tradeDto1);
        verify(rsEventRepository).save(RsEventDto.builder()
                .eventName("猪肉涨价了")
                .id(4)
                .keyword("经济")
                .voteNum(2)
                .user(userDto)
                .rank(1)
                .tradeDto(tradeDto1)
                .build());

    }

}
