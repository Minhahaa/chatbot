package com.gp.chatbot.model.repositories.goods.erp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.order.erp.OrderDetailEntity;

@EnableJpaRepositories
public interface OrderDetailRepository  extends JpaRepository<OrderDetailEntity, String> {
	List<OrderDetailEntity> findByOrdNo(String ordNo);
}
