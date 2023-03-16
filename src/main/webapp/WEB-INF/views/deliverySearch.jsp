<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!-- saved from url=(0014)about:internet -->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>GP 배송조회</title>
    <link rel="stylesheet" href="/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/bootstrap-theme.min.css">
    <link rel="stylesheet" href="/resources/css/tracking-cyan.css">
</head>
<body>
<div class="container-fluid">
    <div class="panel-body">
        <div class="col-md-12 header-title">GP 배송조회</div>
        <div class="col-md-12">
            <div class="form-group row">
            	<label for="kind_of_delivery" class="col-xs-4 label-style">조회내용 선택</label>
            	<select id="kind_of_delivery" name="kind_of_delivery" class="col-xs-2 info-style">
            		<option value="none">선택</option>
            		<option value="orderNo">주문번호</option>
            		<option value="deliNo">송장번호</option>
            	</select>
            	<div id="invoice" class="col-xs-6 info-style">
                	<input type="text" id="search_number" onchange="inputOnlyNumberFormat(this);" maxlength="14">
                	<input type="button" id="search" value="조회"/>
                </div>
                
            </div>
            <!-- <div class="form-group row">
                <label for="company" class="col-xs-4 label-style">택배사</label>
                <div id="company" class="col-xs-8 info-style"></div>
            </div> -->
        </div>
        <h6 class="col-md-12">* 송장번호가 상품코드로 나오는 경우 T/R 또는 평택 재고입니다</h6>
    </div>
	
	<div id="delivery_no_kinds">
	    <div class="col-sm-12">
	    	<div class="header-title">택배사 | 송장번호 | 현재상태</div>
	        <table class="table table-striped">
	            <thead>
	            <tr>
	                <th>시간</th>
	                <th>현재 위치</th>
	                <th>배송 상태</th>
	            </tr>
	            </thead>
	            <tbody>
	            <tr>
	                <td>조회 후 표시됩니다</td>
	                <td></td>
	                <td></td>
	            </tr>
	            </tbody>
	        </table>
	    </div>
    </div>

</div>


<script src="/resources/js/jquery.min.js"></script>
<script src="/resources/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/resources/js/sweetalert2.js"></script>
<script>
	const Toast = Swal.mixin({
	    toast: true,
	    position: 'center-center',
	    showConfirmButton: false,
	    timer: 1300,
	    timerProgressBar: true,
	    didOpen: (toast) => {
	    toast.addEventListener('mouseenter', Swal.stopTimer)
	    toast.addEventListener('mouseleave', Swal.resumeTimer)
	    }
	});

	$(document).ready(function(){
		$("#search").on( 'click', function(){

			var obj = $("#kind_of_delivery option:selected").val();
			var numberLength = $('#search_number').val();
			numberLength = onlynumber(numberLength);
			//(numberLength.length < 14 || numberLength.length > 9)
			console.log(obj);
			if(obj == 'none'){
				Toast.fire({
	      	    	icon: 'error',
	      	    	title: '조회를 원하시는 정보를 선택해주세요'
	      		});
				document.getElementById('kind_of_delivery').focus();
				return;
			}else if(obj == 'orderNo'){
				
				if(numberLength.length != 14){
					Toast.fire({
		      	    	icon: 'error',
		      	    	title: '주문번호를 확인해주세요'
		      		});
					document.getElementById('search_number').focus();
					return;
				}else{
					console.log('init');
					var data = {
							"action":{
								"params":{
									"sys_order_service":"IFFICE",
									"sys_order_no":numberLength
									}
								}
							};
					
					$.ajax({
				    	   url: '/api/orderSearch',
				    	   type : 'POST',
				    	   data :  JSON.stringify(data),
				    	   beforeSend: function(){
				    		   loading();  
				    	   },
				    	   success : function(result){
				    		   console.log(result);
				    		   makeOrderList(result);
				    		   closeLoadingWithMask();
				    	   },
				    	   fail: function(e){
				    	   },
				    	   error:function(request, status, error){
				    	   }
				     	});// end ajax
				     	
					
				}
			}else if(obj == 'deliNo'){
				if(numberLength.length < 9 || numberLength.length > 13){
					Toast.fire({
		      	    	icon: 'error',
		      	    	title: '송장번호를 확인해주세요'
		      		});
					document.getElementById('search_number').focus();
					return;
				}else{
					console.log('init');
					var data = {
							"action":{
								"params":{
									"sys_deli_no":numberLength
									}
								}
							};
					
					$.ajax({
				    	   url: '/api/deliNoSearch',
				    	   type : 'POST',
				    	   data :  JSON.stringify(data),
				    	   beforeSend: function(){
				    		   loading();  
				    	   },
				    	   success : function(result){
				    		   console.log(result);
				    		   makeDeliList(result);
				    		   closeLoadingWithMask();
				    	   },
				    	   fail: function(e){
				    	   },
				    	   error:function(request, status, error){
				    	   }
				     	});// end ajax
					
				}
			}
		});
	});
	
	function makeDeliList(result){
		
		console.log('deli info ');
		var apiValue = result.data;
		var allProgress = apiValue.allProgress ==( null || undefined ) ? 'none' : apiValue.allProgress;
		console.log(apiValue);
		console.log(allProgress);
		console.log(result.allProgress);
		
		var carrierId = apiValue.carrierId;
		var companyName = apiValue.company;
		var status = apiValue.status;
		
		carrierId = apiValue.carrierId == '037' ? '직배송' : apiValue.carrierId;
		companyName = apiValue.carrierId == '037' ? '모든오피스' : apiValue.company;
		document.getElementById("delivery_no_kinds").innerHTML = "";
		
		var html = '';
		
		html += '<div class="col-sm-12" style="margin-bottom:30px;">';
		html += '<div>' + companyName + ' |	 송장번호 : ' + carrierId + ' |	 상태 : ' + status + '</div>';
		html += '<hr>';
		html += '<table class="table table-striped">';
		html += '<thead>';
		html += '<tr>';
		html += '<th>시간</th>';
		html += '<th>현재 위치</th>';
		html += '<th>배송 상태</th>';
		html += '</tr>';
		html += '</thead>';
		html += '<tbody>';
		
		if(allProgress != 'none'){
			for(var i = 0 ; i < allProgress.length ; i++){
				var progress = allProgress[i];
				
				html += '<tr>';
				html += '<td>'+ progress.time +'</td>';
				html += '<td>'+ progress.location.name +'</td>';
				html += '<td>'+ progress.status.text +'</td>';
				html += '</tr>';
			}
		}else{
			
			html += '<tr>';
			html += '<td>'+ '미정' +'</td>';
			html += '<td>'+ '미정' +'</td>';
			html += '<td>'+ '미정' +'</td>';
			html += '</tr>';
		}

		html += '</tbody>';
		html += '</table>';
		html += '</div>';
		html += '<br><br><br><br><br><br><hr>';
		$('#delivery_no_kinds').append(html);
	}
	
	function makeOrderList(result){
		
		var data = result.data;
		
		var apiValues = data.apiValues;
		var deliStatus = data.deliveryStatus;
		var goodsStatus = data.goodsStatus;
		var modenDeliveryMan = data.modenDelivery == null ? "" : data.modenDelivery.modenDelivery;
		
		console.log(apiValues);
		console.log(deliStatus);
		console.log(goodsStatus);
		console.log(modenDeliveryMan);
		
		document.getElementById("delivery_no_kinds").innerHTML = "";
		var deliKeys = Object.keys(deliStatus);//키를 가져옵니다. 이때, keys 는 반복가능한 객체가 됩니다.
		
		for(var i = 0 ; i < deliKeys.length ; i++){
			var deliKey = deliKeys[i];
			var status = deliStatus[deliKey];
			
			console.log(deliKey);
			console.log(deliKey);
			
			var html = '';
			for(var j = 0 ; j < apiValues.length ; j++){
				
				console.log('init point');
				
				var carrierId = apiValues[j].carrierId;
				var companyName = apiValues[j].company;
				console.log(deliKey + " , " + carrierId);
				
				//moden office 직배송이 아닌경우에 표현
				if(deliKey == carrierId){
					
					carrierId = apiValues[j].carrierId == '037' ? '직배송' : apiValues[j].carrierId;
					companyName = apiValues[j].carrierId == '037' ? '모든오피스' : apiValues[j].company;
					
					if(status == 'CLAIM 처리'){
						var goodKeys = Object.keys(goodsStatus);
						var listGoods = ' (';
						for(var k = 0 ; k < goodKeys.length ; k++){
							var goodKey = goodKeys[k];
							if(goodsStatus[goodKey] == 'CLAIM 처리'){
								if(k == 0){
									listGoods += goodKey;
								}else{
									listGoods += ','+ goodKey;
								}
							}
						}
						listGoods += ')';
						
						status = status + listGoods;
					}
					
					html += '<div class="col-sm-12" style="margin-bottom:30px;">';
					html += '<div>' + companyName + ' |	 송장번호 : ' + carrierId + ' |	 상태 : ' + status + '</div>';
					html += '<hr>';
					html += '<table class="table table-striped">';
					html += '<thead>';
					html += '<tr>';
					html += '<th>시간</th>';
					html += '<th>현재 위치</th>';
					html += '<th>배송 상태</th>';
					html += '</tr>';
					html += '</thead>';
					html += '<tbody>';
					console.log(apiValues[j]);
					var allProgress = apiValues[j].allProgress ==( null || undefined ) ? 'none' : apiValues[j].allProgress;
					console.log(allProgress);
					if(allProgress != 'none'){
						
						for(var k = 0 ; k < allProgress.length ; k++){
							var progress = allProgress[k];
							
							html += '<tr>';
							html += '<td>'+ progress.time +'</td>';
							html += '<td>'+ progress.location.name +'</td>';
							html += '<td>'+ progress.status.text +'</td>';
							html += '</tr>';
						}
						
					}else{
						
						html += '<tr>';
						html += '<td>'+ '미정' +'</td>';
						html += '<td>'+ '미정' +'</td>';
						html += '<td>'+ '미정' +'</td>';
						html += '</tr>';
					}
					
					html += '</tbody>';
					html += '</table>';
					html += '</div>';
					html += '<br><br><br><br><br><br><hr>';
					$('#delivery_no_kinds').append(html);
					
				}
			}
		}
	}
	
	function loading(){
		
		var maskHeight = $(document).height();
	    var maskWidth  = window.document.body.clientWidth;
	    
	  	//화면에 출력할 마스크를 설정해줍니다.
	    var mask       = "<div id='mask' style='position:absolute; z-index:9000; background-color:#000000; display:none; left:0; top:0;'></div>";
	    var loadingImg = "<div id='loadingImg' style=''>";
	    loadingImg += " <img src='/resources/images/Spinner.gif' style='position: absolute; display: block; top: 40%;left: 40%;'/>";
	    loadingImg += "</div>";
	    //화면에 레이어 추가
	    $('body')
	        .append(mask)
	 
	    //마스크의 높이와 너비를 화면 것으로 만들어 전체 화면을 채웁니다.
	    $('#mask').css({
	            'width' : maskWidth,
	            'height': maskHeight,
	            'opacity' : '0.3'
	    }); 
	  
	    //마스크 표시
	    $('#mask').show();
	  
	    //로딩중 이미지 표시
	    $('body').append(loadingImg);
	    $('#loadingImg').show();
	    
	}
	
	function closeLoadingWithMask() {
	    $('#mask, #loadingImg').hide();
	    $('#mask, #loadingImg').empty();  
	}
	
	function comma(str) {
        str = String(str);
        return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
    }

    function uncomma(str) {
        str = String(str);
        return str.replace(/[^\d]+/g, '');
    } 
    
    function inputNumberFormat(obj) {
        obj.value = comma(uncomma(obj.value));
    }
    
    function inputOnlyNumberFormat(obj) {
        obj.value = onlynumber(uncomma(obj.value));
    }
    
    function onlynumber(str) {
	    str = String(str);
	    return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g,'$1');
	}
    

</script>


</body></html>