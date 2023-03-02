package com.gp.chatbot.model.vo.order.besti;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moden_stat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class ModenStatEntity{
	 
    @Id
    @Column(nullable = false, name="ord_no")
    private String ordNo; //
	
    @Column(length = 50, nullable = false, name="unq_no")
    private String unqNo;//
	
    @Column(length = 30, nullable = false, name="goods_cd")
    private String goodsCd;//상품코드
    
    @Column(nullable = true, name="status")
    private String status; //상태
    
    @Column(length = 200, nullable = true, name="msg_manager")
    private String msg_manager; //
    
    @Column(nullable = true, name="msg_customer")
    private String msgCustomer; //부가세 계
    
    @Column(length = 100, nullable = true, name="parceldata")
    private String parcelData; //
    
    @Column(length = 5, nullable = true, name="result_code")
    private String resultCode; //반품수량
    
    @Column(nullable = true, name="add_date")
    private String addDate; //반품금액
    
    public ModenStatEntity(String ord_no) {
    	this.ordNo = ord_no;
    }

}
