package com.gp.chatbot.model.vo.wis.member;

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
@Table(name = "wom0100")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class WisDriverDeliveryEntity{
	
    @Id
    @Column(nullable = false, name="ord_no")
    private String ordNo;//회원번호
    
    @Column(nullable = false, name="trans_sabon")
    private String transSabon; //
	
    public WisDriverDeliveryEntity(String transSabon) {
    	this.transSabon = transSabon;
    }

}
