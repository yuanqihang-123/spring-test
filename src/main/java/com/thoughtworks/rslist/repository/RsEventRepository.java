package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RsEventRepository extends CrudRepository<RsEventDto, Integer> {


    List<RsEventDto> findByRankNot(int notNumber);
    List<RsEventDto> findByRankIs(int number);
    List<RsEventDto> findAll();

    RsEventDto findByRank(int rank);

    long deleteByRank(int rank);

    @Transactional
    void deleteAllByUserId(int userId);
}
