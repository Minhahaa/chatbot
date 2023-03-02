package com.gp.chatbot.model.repositories.erp.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.erp.order.OrderDetailEntity;

@EnableJpaRepositories
public interface OrderDetailRepository  extends JpaRepository<OrderDetailEntity, String> {
	List<OrderDetailEntity> findByOrdNo(String ordNo);
}
