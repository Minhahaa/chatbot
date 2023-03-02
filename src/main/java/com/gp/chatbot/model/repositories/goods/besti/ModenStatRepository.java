package com.gp.chatbot.model.repositories.goods.besti;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.order.besti.ModenStatEntity;

@EnableJpaRepositories
public interface ModenStatRepository  extends JpaRepository<ModenStatEntity, String> {
	List<ModenStatEntity> findByOrdNo(String ordNo);
}
