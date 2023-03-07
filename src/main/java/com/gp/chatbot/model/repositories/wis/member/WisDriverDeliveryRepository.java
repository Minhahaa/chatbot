package com.gp.chatbot.model.repositories.wis.member;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.wis.member.WisDriverDeliveryEntity;
import com.gp.chatbot.model.vo.wis.member.WisDriverEntity;
import com.gp.chatbot.model.vo.wis.order.TwDirectDeliEntity;

@EnableJpaRepositories
public interface WisDriverDeliveryRepository  extends JpaRepository<WisDriverDeliveryEntity, String> {
	List<WisDriverDeliveryEntity> findByTransSabon(String transSabon);
	List<WisDriverDeliveryEntity> findByOrdNo(String ordNo);
}
