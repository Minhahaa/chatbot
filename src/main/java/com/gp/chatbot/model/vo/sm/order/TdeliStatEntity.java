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
@Table(name = "tdelistat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class TdeliStatEntity{
	 
    @Id
    @Column(length = 10, nullable = false, name="delv_seq")
    private String delvSeq;//
    
    @Column(nullable = false, name="ord_no")
    private String ordNo; //
	
    @Column(length = 30, nullable = false, name="delv_itm_cd")
    private String delvItmCd;//
    
    @Column(nullable = true, name="delv_stat")
    private String delvStat; //
    
    @Column(length = 50, nullable = true, name="delv_dt")
    private String delvDt; //
    
    @Column(nullable = true, name="add_exp")
    private String addExp; //
    
    @Column(length = 30, nullable = true, name="reg_dm")
    private String regDm; //
    
    @Column(length = 30, nullable = true, name="upd_dm")
    private String updDm; //
    
    @Column(length = 10, nullable = true, name="user_id")
    private String userId; //
    
    @Column(length = 100, nullable = true, name="screen_id")
    private String screenId; //
    
    public TdeliStatEntity(String ord_no) {
    	this.ordNo = ord_no;
    }

}
