package com.gp.chatbot.model.vo.sm.order;

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
@Table(name = "torderdtl")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class TorderDetailEntity{
	 
    @Id
    @Column(nullable = false, name="goods_nm")
    private String goodsNm;//
    
    @Column(nullable = false, name="ord_no")
    private String ordNo; //
	
    @Column(length = 10, nullable = false, name="goods_unt")
    private String goodsUnt;//
    
    @Column(nullable = true, name="real_sale_pr")
    private String realSalePr; //
    
    @Column(length = 50, nullable = true, name="goods_cnt")
    private String goodsCnt; //
    
    @Column(nullable = true, name="goods_cd")
    private String goodsCd; //
    
    @Column(length = 5, nullable = true, name="trust_yn")
    private String trustYn; //
    
    public TorderDetailEntity(String ord_no) {
    	this.ordNo = ord_no;
    }

}
