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
@Table(name = "wmm0700")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class WisDriverEntity{
	
    @Id
    @Column(length = 30, nullable = false, name="driver_no")
    private String driverNo;//회원번호
    
    @Column(nullable = false, name="driver_name")
    private String driverName; //
	
    @Column(length = 30, nullable = false, name="tel_no")
    private String telNo;//
    
    public WisDriverEntity(String driverNo) {
    	this.driverNo = driverNo;
    }

}
