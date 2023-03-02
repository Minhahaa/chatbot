package com.gp.chatbot.model.vo.sm.common;

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
@Table(name = "tcodedtl")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class TcodeDtlEntity{
	 

    @Id
    @Column(length = 10, nullable = false, name="cd_dtl_no")
    private String cdDtlNo;//
    
    @Column(nullable = false, name="cd_no")
    private String cdNo; //
	
    @Column(length = 30, nullable = false, name="cd_dtl_nm")
    private String cdDtlNm;//
    
    @Column(nullable = true, name="code_url")
    private String codeUrl; //상태
    
    @Column(length = 5, nullable = true, name="cd_dtl_exp")
    private String cdDtlExp; //송장 번호
    
    @Column(nullable = true, name="prir_seq")
    private String prirSeq; //
    
    @Column(length = 30, nullable = true, name="reg_dm")
    private String regDm; //
    
    @Column(length = 4, nullable = true, name="use_yn")
    private String useYn; //
    
    @Column(length = 10, nullable = true, name="user_id")
    private String userId; //
    
    public TcodeDtlEntity(String cd_no) {
    	this.cdNo = cd_no;
    }

}
