package com.gp.chatbot.model.repositories.sm.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.sm.common.TcodeDtlEntity;

@EnableJpaRepositories
public interface TcodeDtlRepository  extends JpaRepository<TcodeDtlEntity, String> {
	List<TcodeDtlEntity> findByCdNo(String cdNo);
}
