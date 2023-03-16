package com.gp.chatbot.model.repositories.sm.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.sm.order.TorderDetailEntity;

@EnableJpaRepositories
public interface TorderDetailRepository extends JpaRepository<TorderDetailEntity, String> {
	List<TorderDetailEntity> findByOrdNo(String ordNo);
}
