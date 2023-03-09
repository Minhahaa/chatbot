package com.gp.chatbot.model.repositories.besti.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.besti.order.ModenStatEntity;

@EnableJpaRepositories
public interface ModenStatRepository  extends JpaRepository<ModenStatEntity, String> {
	List<ModenStatEntity> findByOrdNo(String ordNo);

	List<ModenStatEntity> findByOrdNoOrderByAddDateAsc(String ordNo);
	
	List<ModenStatEntity> findByUnqNoLikeOrderByStatusDesc(String string);

	List<ModenStatEntity> findByParcelDataLike(String string);
}
