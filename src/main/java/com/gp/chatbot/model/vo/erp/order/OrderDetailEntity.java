package com.gp.chatbot.model.vo.erp.order;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Primary;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sam0211")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public @Data class OrderDetailEntity{
	 
    @Id
    @Column(length = 30, nullable = true, name="goods_cd")
    private String goodsCd; //상품코드
    
    @Column(nullable = false, name="ord_no")
    private String ordNo; //
	
    @Column(length = 10, nullable = false, name="so_day")
    private String soDay;//주문일
	
    @Column(length = 10, nullable = false, name="so_ser")
    private String soSer;//주문번호
    
    @Column(nullable = true, name="qty")
    private String qty; //수량
    
    @Column(length = 50, nullable = true, name="so_amt")
    private String soAmt; //공급가액 계
    
    @Column(nullable = true, name="so_vat")
    private String soVat; //부가세 계
    
    @Column(length = 100, nullable = true, name="so_tot_amt")
    private String soTotAmt; //총판매금액
    
    @Column(length = 100, nullable = true, name="sti_ret_qty")
    private String stiRetQty; //반품수량
    
    @Column(length = 100, nullable = true, name="sti_ret_amt")
    private String stiRetAmt; //반품금액
    
    @Column(length = 50, nullable = true, name="sale_qty")
    private String saleQty; //출고수량
    
    @Column(length = 50, nullable = true, name="sale_amt")
    private String saleAmt; //출고금액
    
    @Column(length = 50, nullable = true, name="sale_vat")
    private String saleVat; //출고부가세
    
    @Column(length = 3, nullable = true, name="sale_ty")
    private String saleTy; //상품매입형태
    
    @Column(length = 3, nullable = true, name="dir_deli_flag")
    private String dirDeliFlag; //협력사별도배송 FLAG : 협력사별도배송이면 'Y' 아니면 'N'
    
    @Column(length = 3, nullable = true, name="dir_po_flag")
    private String dirPoFlag; //협력사별도배송 발주 확인 FLAG 무조건 'Y'
    
    @Column(length = 20, nullable = true, name="ctx_day")
    private String ctxDay; //등록일시
    
    @Column(length = 20, nullable = true, name="utx_day")
    private String utxDay; //수정일시
    
    @Column(length = 20, nullable = true, name="tx_emp_no")
    private String txEmpNo; //작업자
    
    @Column(length = 30, nullable = true, name="deli_day")
    private String deliDay; //협력사별도배송 출고일
    
    @Column(length = 30, nullable = true, name="ex_wfob_amt")
    private String exWfobAmt; //수출 외화 FOB 금액
    
    @Column(length = 30, nullable = true, name="ex_mfob_amt")
    private String exMfobAmt; //수출 미화 FOB 금액
    
    @Column(length = 30, nullable = true, name="ex_wcnf_amt")
    private String exWcnfAmt; //수출 외화 CNF 금액
    
    @Column(length = 30, nullable = true, name="ex_mcnf_amt")
    private String exMcnfAmt; //수출 미화 CNF 금액
    
    @Column(length = 30, nullable = true, name="ex_exp_amt")
    private String exExpAmt; //수출비용
    
    @Column(length = 3, nullable = true, name="vat_yn")
    private String vatYn; //부가세유무
    
    @Column(length = 30, nullable = true, name="sale_unit_amt")
    private String saleUnitAmt; //정상판매단가
    
    @Column(length = 30, nullable = true, name="pch_unit_amt")
    private String pchUnitAmt; //매입단가
    
    @Column(length = 10, nullable = true, name="volume_dc")
    private String volumeDc; //볼륨DC유무
    
    @Column(length = 10, nullable = true, name="sale_type")
    private String saleType; //상품유형(pum0100 의 sale_type)
    
    @Column(length = 30, nullable = true, name="amount")
    private String amount; //WEB표시공급가(MRO(동화상협)때문에 2011.12.16 추가)
    
    @Column(length = 30, nullable = true, name="surtaxe")
    private String surtaxe; //WEB표시부가세(MRO(동화상협)때문에 2011.12.16 추가)
    
    @Column(length = 2, nullable = true, name="trust_yn")
    private String trustYn; //
    
    @Column(length = 2, nullable = true, name="pkg_yn")
    private String pkgYn; //
    
    @Column(length = 30, nullable = true, name="pkg_emp_no")
    private String pkgEmpNo; //
    
    @Column(length = 10, nullable = true, name="pch_type")
    private String pchType; //
    
    @Column(length = 30, nullable = true, name="sub_goods_cd")
    private String subGoodsCd; //

    
    public OrderDetailEntity(String ord_no) {
    	this.ordNo = ord_no;
    }

}
