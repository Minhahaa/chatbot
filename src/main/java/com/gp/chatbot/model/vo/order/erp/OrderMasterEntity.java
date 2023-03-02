package com.gp.chatbot.model.vo.order.erp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.springframework.context.annotation.Primary;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sam0210")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class OrderMasterEntity {
	 
    @Id
    @Column(nullable = false, name="ord_no") 
    private String ordNo; //주문번호
	
	@Column(length = 10, nullable = false, name="so_day")
    private String soDay;//주문일자
	
    @Column(length = 10, nullable = false, name="so_ser")
    private String soSer;//주문순번
    
    @Column(length = 3, nullable = false, name="inout_gubun") 
    private String inoutGubun; //
    
    @Column(length = 3, nullable = true, name="so_rcpt_ty")
    private String soRcptTy; //
    
    @Column(length = 3, nullable = true, name="so_ty")
    private String soTy; //
    
    @Column(length = 10, nullable = true, name="cust")
    private String cust; //협력사 코드
    
    @Column(length = 10, nullable = true, name="mem_no")
    private String memNo; //회원번호
    
    @Column(length = 8, nullable = true, name="post_no")
    private String postNo; //우편번호
    
    @Column(length = 5, nullable = true, name="wh_code")
    private String whCode; //물류센터 코드
    
    @Column(length = 30, nullable = true, name="rcpt_man")
    private String rcptMan; //수취인
    
    @Column(length = 20, nullable = true, name="rcpt_tel")
    private String rcptTel; //수취인 번호
    
    @Column(length = 100, nullable = true, name="rcpt_addr")
    private String rcptAddr; //수취인 주소
    
    @Column(length = 10, nullable = true, name="deli_day")
    private String deliDay; //배송 예정일
    
    @Column(length = 3, nullable = true, name="pay_cond")
    private String payCond; //결제 수단
    
    @Column(length = 3, nullable = true, name="pay_state")
    private String payState; //결제 수단
    
    @Column(length = 10, nullable = true, name="curr_unit")
    private String currUnit; //결제 수단
    
    @Column(length = 3, nullable = true, name="tax_yn")
    private String taxYn; //부가세 여부
    
    @Column(length = 3, nullable = true, name="mileage")
    private String mileage; //결제 포인트
    
    @Column(length = 3, nullable = true, name="prepay")
    private String prepay; //2% DC 여부
    
    @Column(length = 100, nullable = true, name="cash_amt")
    private String cashAmt; //결제금액
    
    @Column(length = 100, nullable = true, name="deli_amt")
    private String deliAmt; //배송비
    
    @Column(length = 100, nullable = true, name="set_amt")
    private String setAmt; //주문금액
    
    @Column(length = 30, nullable = true, name="inpt_bank")
    private String inptBank; //CMS 입금은행
    
    @Column(length = 20, nullable = true, name="inpt_day")
    private String inptDay; //CMS 입금일자
    
    @Column(length = 10, nullable = true, name="inpt_amt")
    private String inptAmt; //CMS 입금금액
    
    @Column(length = 20, nullable = true, name="inpt_acc_no")
    private String inptAccNo; //CMS입금계좌
    
    @Column(length = 10, nullable = true, name="end_yn")
    private String endYn; //종결여부
    
    @Column(length = 10, nullable = true, name="status_code")
    private String statusCode; //상태코드
    
    @Column(length = 20, nullable = true, name="esti_day")
    private String estiDay; //견적서일자
    
    @Column(length = 20, nullable = true, name="esti_ser")
    private String estiSer; //견적서순번
    
    @Column(length = 20, nullable = true, name="claim_day")
    private String claimDay; //클레임 일자
    
    @Column(length = 20, nullable = true, name="claim_ser")
    private String claimSer; //클레임 순번
    
    @Column(length = 20, nullable = true, name="tax_no")
    private String taxNo; //견적서일자
    
    @Column(length = 20, nullable = true, name="tax_no2")
    private String taxNo2; //견적서일자
    
    @Column(length = 20, nullable = true, name="sale_day")
    private String saleDay; //츨고일자
    
    @Column(length = 20, nullable = true, name="slip_no")
    private String slipNo; //
    
    @Column(length = 20, nullable = true, name="remark")
    private String remark; //
    
    @Column(length = 20, nullable = true, name="old_so_no")
    private String oldSoNo; //
    
    @Column(length = 30, nullable = true, name="ctx_day")
    private String ctxDay; //
    
    @Column(length = 30, nullable = true, name="utx_day")
    private String utxDay; //
    
    @Column(length = 20, nullable = true, name="tx_emp_no")
    private String txEmpNo; //
    
    @Column(length = 20, nullable = true, name="slip_no_sale")
    private String silpNoSale; //
    
    @Column(length = 20, nullable = true, name="slip_no_sale2")
    private String silpNoSale2; //
    
    @Column(length = 10, nullable = true, name="card_gubun")
    private String cardGubun; //카드사 구분
    
    @Column(length = 20, nullable = true, name="ex_ord_no")
    private String exOrdNo; //
    
    @Column(length = 20, nullable = true, name="ex_agent")
    private String exAgent; //
    
    @Column(length = 20, nullable = true, name="ex_ord_date")
    private String exOrdDate; //
    
    @Column(length = 20, nullable = true, name="ex_lc_no")
    private String exLcNo; //
    
    @Column(length = 20, nullable = true, name="ex_pass_date")
    private String exPassDate; //
    
    @Column(length = 20, nullable = true, name="ex_place")
    private String exPlace; //
    
    @Column(length = 20, nullable = true, name="claim_date")
    private String claimDate; //
    
    @Column(length = 20, nullable = true, name="ex_nego_date")
    private String exNegoDate; //
    
    @Column(length = 20, nullable = true, name="end_date")
    private String endDate; //
    
    @Column(length = 20, nullable = true, name="slip_no_sale_ser")
    private String slipNoSaleSer; //
    
    @Column(length = 20, nullable = true, name="slip_no_sale2_ser")
    private String slipNoSale2Ser; //
    
    @Column(length = 3, nullable = true, name="ord_iquick")
    private String ordIquick; //
    
    @Column(length = 3, nullable = true, name="point_yn")
    private String pointYn; //
    
    @Column(length = 20, nullable = true, name="slip_no_point")
    private String slipNoPoint; //
    
    @Column(length = 20, nullable = true, name="slip_no_point_ser")
    private String slipNoPointSer; //
    
    @Column(length = 20, nullable = true, name="coupon_amt")
    private String couponAmt; //
    
    @Column(length = 20, nullable = true, name="class_gubun")
    private String classGubun; //
    
    @Column(length = 20, nullable = true, name="am_cust_yn")
    private String amCustYn; //
    
    @Column(length = 20, nullable = true, name="pre_amt")
    private String preAmt; //
    
    @Column(length = 40, nullable = true, name="tax_email")
    private String taxEmail; //
    
    @Column(length = 20, nullable = true, name="tax_sms")
    private String taxSms; //
    
    @Column(length = 20, nullable = true, name="tax_nm")
    private String taxNm; //
    
    @Column(length = 20, nullable = true, name="claim_no")
    private String claimNo; //
    
    @Column(length = 20, nullable = true, name="logistics_loc_code")
    private String logisticsLocCode; //
    
    @Column(length = 20, nullable = true, name="actual_recv")
    private String actualRecv; //
    
    @Column(length = 10, nullable = true, name="company_type")
    private String companyType; //
    
    public OrderMasterEntity(String ordNo) {
    	this.ordNo = ordNo;
    }

}
