package com.gp.chatbot.model.vo.wis.order;

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
@Table(name = "tw_directdeli")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class TwDirectDeliEntity{
	
    @Id
    @Column(length = 30, nullable = false, name="goods_cd")
    private String goodsCd;//상품코드
    
    @Column(nullable = false, name="so_date")
    private String soDate; //
	
    @Column(length = 10, nullable = false, name="so_ser")
    private String soSer;//
    
    @Column(nullable = true, name="deli_house")
    private String deliHouse; //상태
    
    @Column(length = 30, nullable = true, name="deli_no")
    private String deliNo; //송장 번호
    
    @Column(nullable = true, name="ctx_dt")
    private String ctxDt; //
    
    @Column(length = 100, nullable = true, name="tx_emp_no")
    private String txEmpNo; //
    
    public TwDirectDeliEntity(String ord_no) {
    	String soDt = ord_no.substring(0,7);
    	String soSr = ord_no.substring(8,13);
    	this.soDate = soDt;
    	this.soSer = soSr;
    }

}
