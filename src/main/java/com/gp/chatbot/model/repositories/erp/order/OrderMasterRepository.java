package com.gp.chatbot.model.repositories.erp.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.gp.chatbot.model.vo.erp.order.OrderMasterEntity;

@EnableJpaRepositories
public interface OrderMasterRepository  extends JpaRepository<OrderMasterEntity, String> {
	List<OrderMasterEntity> findByOrdNo(String ordNo);
}
