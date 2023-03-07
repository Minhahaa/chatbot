package com.gp.chatbot.model.repositories.wis.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.wis.order.TwDirectDeliEntity;

@EnableJpaRepositories
public interface TwDirectDeliRepository  extends JpaRepository<TwDirectDeliEntity, String> {
	List<TwDirectDeliEntity> findBySoDateAndSoSer(String soDate, String soSer);

	TwDirectDeliEntity findBySoDateAndSoSerAndGoodsCd(String soDt, String soSr, String goodsCd);

	List<TwDirectDeliEntity> findByDeliNo(String deliNo);
}
