//javascript for populating the graphs in reporting dashboard overview

var monthNamesList = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];	
var overviewData=getOverviewData();

function drawSpsStatsGraph(){
	var chartData;
	var spsChartData = [[ 'SPS', 'Detractors', 'Passives', 'Promoters'],[]];
	google.charts.load('current', {	packages : [ 'corechart', 'bar' ]});
	google.charts.setOnLoadCallback(drawStacked);
	
	function drawEmptyChart(){
		var spsChartData = [
							[ 'SPS', 'Detractors','Passives','Promoters' ],
							[ '', 0, 0, 0 ] ];

					var data = google.visualization
							.arrayToDataTable(spsChartData);

					var options = {
						title : 'SPS Stats',
						legend : {
							position : 'none'
						},
						bar : {
							groupWidth : '40%'
						},
						isStacked : true,
						height : 300,
						vAxis : {
							minValue : 0,
							maxValue : 10,
							gridlines : {
								count : 5
							}
						},
						colors : [ '#E8341F',
								'#999999', '#7ab400' ]
					};

					var chart = new google.visualization.ColumnChart(
							document
									.getElementById('chart_div'));
					chart.draw(data, options);
	}
	
	function drawStacked() {
		$.ajax({
					async : false,
					url : "/fetchreportingspsstats.do",
					type : "GET",
					cache : false,
					dataType : "json",
					success : function(data) {
						if (data.status == 200) {
							$.ajax({
										url : data.url,
										type : "GET",
										cache : false,
										dataType : "json",
										success : function(response) {
											chartData = JSON.parse(response);

											if (chartData.length == 0) {
												drawEmptyChart();
											} else {
												var spsChartData = new Array(
														chartData.length + 1);
												for (var k = 0; k <= chartData.length; k++) {
													spsChartData[k] = new Array(
															4);
												}
												spsChartData[0] = [ 'SPS',
														'Detractors',
														'Passives', 'Promoters' ];

												for (var i = 1; i <= chartData.length; i++) {
													var monthName = monthNamesList[(chartData[i - 1][1]) - 1];
													spsChartData[i][0] = monthName
															+ "/"
															+ chartData[i - 1][0];
													spsChartData[i][1] = chartData[i - 1][2];
													spsChartData[i][2] = chartData[i - 1][3];
													spsChartData[i][3] = chartData[i - 1][4];
												}

												var data = google.visualization
														.arrayToDataTable(spsChartData);

												var options = {
													legend : {
														position : 'none'
													},
													bar : {
														groupWidth : '40%'
													},
													isStacked : true,
													width: 600,
													height: 300,
													chartArea: {width:500,height:200},
													vAxis : {
														gridlines : {
															count : 14
														}
													},
													colors : [ '#E8341F',
															'#999999',
															'#7ab400' ]
												};

												var chart = new google.visualization.ColumnChart(
														document
																.getElementById('chart_div'));
												chart.draw(data, options);
											}
										},
										error : function(e) {

											if (e.status == 504) {
												redirectToLoginPageOnSessionTimeOut(e.status);
												return;
											}
											drawEmptyChart();
										}
									});
						}else{
							drawEmptyChart();
						}
					},
					error : function(e) {
						if (e.status == 504) {
							redirectToLoginPageOnSessionTimeOut(e.status);
							return;
						}
						drawEmptyChart();
					}
				});
	}
}

function drawAvgRatingsGraph(){
	
	google.charts.load("current", {packages:["corechart"]});
	google.charts.setOnLoadCallback(drawChart);
	
	function drawEmptyChart(){
		var spsChartData = [ [ 'X', 'Y' ],
								[ '', 0 ] ];

						var data = google.visualization
								.arrayToDataTable(spsChartData);

						var options = {
							legend : 'none',
							height : 300,
							width : 1000,
							vAxis : {
								title : 'Average Rating',
								minValue : 0,
								maxValue : 6,
								gridlines : {
									count : 7
								}
							},
							colors : [ '009fe0' ],
							pointSize : 5
						};

						var chart = new google.visualization.ColumnChart(
								document
										.getElementById('average_chart_div'));
						chart.draw(data, options);
	}
	
	function drawChart() {
		$.ajax({
					async : false,
					url : "/fetchaveragereportingrating.do",
					type : "GET",
					cache : false,
					dataType : "json",
					success : function(data) {
						if (data.status == 200) {
							$.ajax({
										url : data.url,
										type : "GET",
										cache : false,
										dataType : "json",
										success : function(response) {
											chartData = JSON.parse(response);

											if (chartData.length == 0) {
												drawEmptyChart();
											} else {
												var avgRatingChartData = new Array(
														chartData.length + 1);
												for (var k = 0; k <= chartData.length; k++) {
													avgRatingChartData[k] = new Array(
															2);
												}
												avgRatingChartData[0] = [ 'X',
														'Y' ];

												for (var i = 1; i <= chartData.length; i++) {
													var monthName = monthNamesList[(chartData[i - 1][1]) - 1];
													avgRatingChartData[i][0] = monthName
															+ "/"
															+ chartData[i - 1][0];
													avgRatingChartData[i][1] = chartData[i - 1][2];
												}

												var data = google.visualization
														.arrayToDataTable(avgRatingChartData);

												var options = {
													legend : 'none',
													height : 300,
													width : 1000,
													vAxis : {
														title : 'Average Rating',
														minValue : 0,
														maxValue : 6,
														gridlines : {
															count : 7
														}
													},
													colors : [ '009fe0' ],
													pointSize : 5
												};

												var chart = new google.visualization.LineChart(
														document
																.getElementById('average_chart_div'));
												chart.draw(data, options);
											}
										},
										error : function(e) {
											if (e.status == 504) {
												redirectToLoginPageOnSessionTimeOut(e.status);
												return;
											}
											drawEmptyChart();
										}
									});
						}else{
							drawEmptyChart();
						}
					},
					error : function(e) {
						if (e.status == 504) {
							redirectToLoginPageOnSessionTimeOut(e.status);
							return;
						}
						drawEmptyChart();
					}
				});
	}

}

function drawCompletionRateGraph(){
	google.charts.load('current', {'packages':['corechart']});
	google.charts.setOnLoadCallback(drawChart);

	function drawEmptyChart(){
		var spsChartData = [
							['Month','Completed Transactions ','Incomplete Transactions '],
							[ '', 0, 0 ] ];

					var data = google.visualization
							.arrayToDataTable(spsChartData);

					var options = {
						title : 'Completion Rate',
						height : 300,
						width : 1100,
						chartArea : {
							width : '78%'
						},
						vAxis : {
							minValue : 0,
							maxValue : 10,
							gridlines : {
								count : 6
							}
						},
						legend : {
							position : 'right',
							alignment : 'center',
							maxLines : 2
						},
						pointSize : 5
					};

					var chart = new google.visualization.ColumnChart(
							document
									.getElementById('completion_chart_div'));
					chart.draw(data, options);
	}
	
	function drawChart() {
		$.ajax({
					async : false,
					url : "/fetchreportingcompletionrate.do",
					type : "GET",
					cache : false,
					dataType : "json",
					success : function(data) {
						if (data.status == 200) {
							$.ajax({
										url : data.url,
										type : "GET",
										cache : false,
										dataType : "json",
										success : function(response) {
											chartData = JSON.parse(response);

											if (chartData.length == 0) {
												drawEmptyChart();
											} else {
												var maxTransactionValue = 0;
												var compRateChartData = new Array(
														chartData.length + 1);
												for (var k = 0; k <= chartData.length; k++) {
													compRateChartData[k] = new Array(
															3);
												}
												compRateChartData[0] = [
														'Month',
														'Completed Transactions ',
														'Incomplete Transactions ' ];

												for (var i = 1; i <= chartData.length; i++) {
													var monthName = monthNamesList[(chartData[i - 1][1]) - 1];
													compRateChartData[i][0] = monthName
															+ "/"
															+ chartData[i - 1][0];
													compRateChartData[i][1] = chartData[i - 1][2];
													compRateChartData[i][2] = chartData[i - 1][3];

													if (compRateChartData[i][1] > maxTransactionValue) {
														maxTransactionValue = compRateChartData[i][1];
													}

													if (compRateChartData[i][2] > maxTransactionValue) {
														maxTransactionValue = compRateChartData[i][2];
													}
												}

												var maxVAxisValue = 0;

												if (maxTransactionValue < 10) {
													maxVAxisValue = maxTransactionValue + 5;
												} else {
													maxVAxisValue = maxTransactionValue + 10;
												}

												var data = google.visualization
														.arrayToDataTable(compRateChartData);

												var options = {
													height : 300,
													width : 1100,
													chartArea : {
														width : '85%'
													},
													vAxis : {
														minValue : 0,
														maxValue : maxVAxisValue,
														gridlines : {
															count : 6
														}
													},
													legend : {
														position : 'bottom',
														alignment : 'center'
													},
													pointSize : 5
												};

												var chart = new google.visualization.LineChart(
														document
																.getElementById('completion_chart_div'));

												chart.draw(data, options);
											}
										},
										error : function(e) {

											if (e.status == 504) {
												redirectToLoginPageOnSessionTimeOut(e.status);
												return;
											}
											drawEmptyChart();
										}
									});
						}else{
							drawEmptyChart();
						}
					},
					error : function(e) {
						if (e.status == 504) {
							redirectToLoginPageOnSessionTimeOut(e.status);
							return;
						}
						drawEmptyChart();
					}
				});
	}
}
function drawUnclickedDonutChart(){
	 google.charts.load("current", {packages:["corechart"]});
     google.charts.setOnLoadCallback(drawChart);
     
     var processed = 68993;
     var unprocessed = 9337;
     
     function drawChart() {
    	 
	        var data = google.visualization.arrayToDataTable([
	          ['Transaction', 'Number#'],
	          ['Processed', processed],
	          ['Unprocessed', unprocessed]
	        ]);

	        var options = {
	          pieStartAngle: 130,
	          backgroundColor: '#f9f9fb',
	          pieHole: 0.5,
	          legend: { 
	      	    position : 'none'
	      	  },
	      	  pieSliceText:'none',
	      	  chartArea:{
	      		  width:'100%',
	      		  height:'65%'
	      	  },
	      	slices: [{color : '#0072c2'},{color: '#fa5b00'}],
	      	 legend: {
	             position: 'labeled',
	             labeledValueText: 'none',
	             textStyle: {
	                 color: 'black', 
	                 fontSize: 15,
	                 bold: true
	             }
	      	 },
	      	tooltip: { trigger: 'none' }
	        };

	        var chart = new google.visualization.PieChart(document.getElementById('donutchart'));
	        chart.draw(data, options);
	      }
}

function drawProcessedDonutChart(){
	 google.charts.load("current", {packages:["corechart"]});
     google.charts.setOnLoadCallback(drawChart);
     
     var incomplete = 62080;
     var completed = 6913;
     
     function drawChart() {
    	 
	        var data = google.visualization.arrayToDataTable([
	          ['Transaction', 'Number#'],
	          ['Incomplete', incomplete],
	          ['Completed', completed]
	        ]);

	        var options = {
	          pieStartAngle: 130,
	          backgroundColor: '#f9f9fb',
	          pieHole: 0.5,
	          legend: { 
	      	    position : 'none'
	      	  },
	      	  pieSliceText:'none',
	      	  chartArea:{
	      		  width:'100%',
	      		  height:'65%'
	      	  },
	      	slices: [{color : '#f5c70a'},{color: '#79b600'}],
	      	 legend: {
	             position: 'labeled',
	             labeledValueText: 'none',
	             textStyle: {
	                 color: 'black', 
	                 fontSize: 15,
	                 bold: true
	             }
	      	 },
	      	tooltip: { trigger: 'none' }
	        };

	        var chart = new google.visualization.PieChart(document.getElementById('processedDonutchart'));
	        chart.draw(data, options);
	      }
}

function drawUnprocessedDonutChart(){
	 google.charts.load("current", {packages:["corechart"]});
    google.charts.setOnLoadCallback(drawChart);
    
    var unassigned = 127;
    var duplicate = 7745;
    var corrupted = 1465;
    var other=26;
    
    function drawChart() {
   	 
	        var data = google.visualization.arrayToDataTable([
	          ['Transaction', 'Number#'],
	          ['Unassigned', unassigned],
	          ['duplicate', duplicate],
	          ['corrupted', corrupted],
	          ['other',other]
	        ]);

	        var options = {
	          pieStartAngle: 90,
	          backgroundColor: '#f9f9fb',
	          pieHole: 0.5,
	          legend: { 
	      	    position : 'none'
	      	  },
	      	  pieSliceText:'none',
	      	  chartArea:{
	      		  width:'100%',
	      		  height:'65%'
	      	  },
	      	slices: [{color : '#f5c70a'},{color: '#7e36c2'},{color:'#ea310b'},{color:'#000000'}],
	      	 legend: {
	             position: 'labeled',
	             labeledValueText: 'none',
	             textStyle: {
	                 color: 'black', 
	                 fontSize: 15,
	                 bold: true
	             }
	      	 },
	      	tooltip: { trigger: 'none' }
	        };

	        var chart = new google.visualization.PieChart(document.getElementById('unprocessedDonutchart'));
	        chart.draw(data, options);
	      }
}

function drawDonutChart(){
	
	//var overviewData = getOverviewData();
	
	if(overviewData != null){
	      google.charts.load("current", {packages:["corechart"]});
	      google.charts.setOnLoadCallback(drawChart);
	      
	      var totalIncompleteTransactions = overviewData.TotalIncompleteTransactions;
	      $('#incompleteTransValue').html(totalIncompleteTransactions);
	      
	      var corruptedTrans = overviewData.CorruptedPercentage;
	      var duplicateTrans = overviewData.DuplicatePercentage;
	      var archivedTrans = overviewData.ArchievedPercentage;
	      var mismatchedTrans = overviewData.MismatchedPercentage;
	      
	      var corrupted= (corruptedTrans/100) * totalIncompleteTransactions;
	      var duplicate= (duplicateTrans/100) * totalIncompleteTransactions;
	      var archived= (archivedTrans/100) * totalIncompleteTransactions;
	      var mismatched= (mismatchedTrans/100) * totalIncompleteTransactions;
	      var totalOfCorDupArcMis = corrupted + duplicate + archived + mismatched;
	      var other = totalIncompleteTransactions - totalOfCorDupArcMis;
	      
	      function drawChart() {
	    	 
	        var data = google.visualization.arrayToDataTable([
	          ['Task', 'Hours per Day'],
	          ['Mismatched', mismatched],
	          ['Corrupted', corrupted],
	          ['Duplicate', duplicate],
	          ['Archived', archived],
	          ['Other', other]
	        ]);

	        var options = {
	          pieHole: 0.5,
	          legend: { 
	      	    position : 'none'
	      	  },
	      	  pieSliceText:'none',
	      	  chartArea:{
	      		  width:'95%',
	      		  height:'97%'
	      	  },
	      	slices: [{color : '#009fe0'},{color: '#E8341F'}, {color: '#985698'}, {color: '#7ab400'}, {color: '#f4ad42'}]
	        };

	        var chart = new google.visualization.PieChart(document.getElementById('donutchart'));
	        chart.draw(data, options);
	      }
	}
}

function drawSpsGauge(){
	
	if(overviewData != null){
		var detractorEndAngle;
		var passivesEndAngle;
		var promotersEndAngle;
		var detractorStartAngle;
		var passivesStartAngle;
		var promotersStartAngle;
		var gaugeStartAngle = 250;
		var gaugeEndAngle = 110;

			function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
				var angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;

				return {
					x : centerX + (radius * Math.cos(angleInRadians)),
					y : centerY + (radius * Math.sin(angleInRadians))
				};
			}

			function describeArc(x, y, radius, startAngle, endAngle) {

				var start = polarToCartesian(x, y, radius, endAngle);
				var end = polarToCartesian(x, y, radius, startAngle);

				var largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";

				var d = [ "M", start.x, start.y, "A", radius, radius, 0, largeArcFlag,
						0, end.x, end.y ].join(" ");

				return d;
			}
			
			function getGaugeEndAngles(){
						
				var spsScore = overviewData.SpsScore;
				
				if(overviewData != null){
					
					$('#spsScorebox').html(spsScore);
					var detractors = overviewData.DetractorPercentage;
					var passives =   overviewData.PassivesPercentage;
					var promoters =  overviewData.PromoterPercentage;
					var totalDegree = (360-gaugeStartAngle)+gaugeEndAngle;
					var degRequired = 0;
				
				//Detractor Start And End Angles
				detractorStartAngle = gaugeStartAngle;
				if(detractors == 0){
					detractorEndAngle = detractorStartAngle;
				}else if(detractors == 50){
					detractorEndAngle = 0;
				}else{
					degRequired = (detractors/100)*totalDegree;
					detractorEndAngle = (detractorStartAngle + degRequired)%360;
				}
				
				if(detractors > 50){
					document.getElementById("arc1").setAttribute("d", describeArc(150, 150, 55, detractorStartAngle, 0));
					document.getElementById("arc4").setAttribute("d", describeArc(150, 150, 55, 0, detractorEndAngle));
				}else{
					document.getElementById("arc1").setAttribute("d", describeArc(150, 150, 55, detractorStartAngle, detractorEndAngle));
				}
				
				//Passives Start and End Angles
				if(detractors==0){
					passivesStartAngle = gaugeStartAngle;
				}else{
					passivesStartAngle = detractorEndAngle + 2;
				}
				
				if(passives == 0){
					passivesEndAngle = passivesStartAngle;
				}else{
					degRequired = (passives/100)*totalDegree;
					passivesEndAngle = (passivesStartAngle + degRequired)%360;
				}
				
				if(passivesStartAngle >= gaugeStartAngle){
					if(passivesEndAngle >= gaugeStartAngle){
						document.getElementById("arc2").setAttribute("d", describeArc(150, 150, 55, passivesStartAngle, passivesEndAngle));
					}else{
						document.getElementById("arc2").setAttribute("d", describeArc(150, 150, 55, passivesStartAngle, 0));
						document.getElementById("arc5").setAttribute("d", describeArc(150, 150, 55, 0, passivesEndAngle));
					}
				}else{
					document.getElementById("arc5").setAttribute("d", describeArc(150, 150, 55, passivesStartAngle, passivesEndAngle));
				}
				
				//Promoters Start and End Angles
				promotersEndAngle =  gaugeEndAngle;
				
				if(promoters == 0){
					promotersStartAngle = promotersEndAngle;
				}else if(detractors == 0 && passives == 0){
					promotersStartAngle = gaugeStartAngle;
				}else{
					promotersStartAngle = passivesEndAngle +2;
				}
				
				if(promotersStartAngle >= gaugeStartAngle){
					if(promotersEndAngle >= gaugeStartAngle){
						document.getElementById("arc3").setAttribute("d", describeArc(150, 150, 55, promotersStartAngle, promotersEndAngle));
					}else{
						document.getElementById("arc3").setAttribute("d", describeArc(150, 150, 55, promotersStartAngle, 0));
						document.getElementById("arc6").setAttribute("d", describeArc(150, 150, 55, 0, promotersEndAngle));
					}
				}else{
					document.getElementById("arc6").setAttribute("d", describeArc(150, 150, 55, promotersStartAngle, promotersEndAngle));
				}
			}
		}

			$(document).ready(function() {
				var spsScore = overviewData.SpsScore;
				
				
				var marginLeft = parseInt($("#metre-needle").css("margin-left"));
				var marginTop = parseInt($("#metre-needle").css("margin-top"));
				
				
				var needleDegree;
				var marginNeedle = Math.abs(spsScore - 20)/2;
				
				if(spsScore < 0){
					
						needleDegree = 360-(Math.abs(spsScore)*1.1);
						if(spsScore < -87){
							$("#metre-needle").css("margin-left",marginLeft-marginNeedle+12+'px');
						}else{
							$("#metre-needle").css("margin-left",marginLeft-marginNeedle-5+'px');
						}
						$("#metre-needle").css("margin-top",marginTop+marginNeedle-20+'px');
						
				}else if(spsScore > 15){
					
					needleDegree = Math.abs(spsScore)*1.1;
					if(spsScore > 87){
						$("#metre-needle").css("margin-left",marginLeft+marginNeedle-20+'px');
					}else{
						$("#metre-needle").css("margin-left",marginLeft+marginNeedle+'px');
					}
					$("#metre-needle").css("margin-top",marginTop+marginNeedle+'px');
					
				}else if(spsScore > 7 && spsScore <=15){
					
					needleDegree = Math.abs(spsScore)*1.1;
					$("#metre-needle").css("margin-left",marginLeft-5+'px');
					
				}else if(spsScore == 0 || (spsScore > 0 && spsScore <= 7)){
					needleDegree = Math.abs(spsScore)*1.1;
					$("#metre-needle").css("margin-left",marginLeft-8+'px');
				}
				
				$('#metre-needle').css({'transform':'rotate(' + needleDegree + 'deg)'});
					
				getGaugeEndAngles();
				
				//document.getElementById("arc1").setAttribute("d", describeArc(150, 150, 70, detractorStartAngle, detractorEndAngle));
				//document.getElementById("arc2").setAttribute("d", describeArc(150, 150, 70, passivesStartAngle, passivesEndAngle));
				//document.getElementById("arc3").setAttribute("d", describeArc(150, 150, 70, promotersStartAngle, promotersEndAngle));
			});
		}
}

function drawOverviewPage(){
	
	if(overviewData != null){
		
		var detractors = overviewData.DetractorPercentage;
		var passives =   overviewData.PassivesPercentage;
		var promoters =  overviewData.PromoterPercentage;
		
			//spsGauge checks
			if(detractors == 0 && promoters == 0 && passives == 0){
				$('#spsGaugeSuccess').hide();
				$('#spsGaugeFailure').show();
			}else{
				$('#spsGaugeFailure').hide();
				$('#spsGaugeSuccess').show();
			}
			
			//detractors,passives and promoters bar and values assignment
			$('#detractorsBar').css('width',detractors+'%');
			$('#detractorsValue').html(detractors+'%');
			$('#passivesBar').css('width',passives+'%');
			$('#passivesValue').html(passives+'%');
			$('#promotersBar').css('width',promoters+'%');
			$('#promotersValue').html(promoters+'%');
			
			//donutChart checks
			var totalIncompleteTransactions = overviewData.TotalIncompleteTransactions;
			
			if(totalIncompleteTransactions > 0){
				$('#donutChartSuccess').show();
				$('#donutChartFailure').hide();
			}else{
				$('#donutChartSuccess').hide();
				$('#donutChartFailure').show();
			}
			
			//surveys sent, surveys completed, social posts and zillow reviews values
			var surveysSent = overviewData.TotalSurveySent;
			var surveysCompleted = overviewData.TotalSurveyCompleted;
			var socialPosts = overviewData.TotalSocialPost;
			var zillowReviews = overviewData.TotalZillowReviews;
			
			$('#surveysSentValue').html(surveysSent);
			$('#surveysCompValue').html(surveysCompleted);
			$('#socialPostsValue').html(socialPosts);
			$('#zillowReviewsValue').html(zillowReviews);
					
	}
}

function getOverviewData() {

	// var overviewData;
	$.ajax({
		async : false,
		url : "/fetchreportingoverview.do",
		type : "GET",
		cache : false,
		dataType : "json",
		success : function(data) {
			if (data.status == 200) {
				$.ajax({
					async : false,
					url : data.url,
					type : "GET",
					cache : false,
					dataType : "json",
					success : function(response) {
						if (response.length == 0) {
							overviewData = null;
						} else {
							overviewData = JSON.parse(response);
						}
					},
					error : function(e) {
						if (e.status == 504) {
							redirectToLoginPageOnSessionTimeOut(e.status);
							return;
						}
						overviewData = null;
					}
				});
			}else{
				overviewData = null;
			}
		},
		error : function(e) {
			if (e.status == 504) {
				redirectToLoginPageOnSessionTimeOut(e.status);
				return;
			}
			overviewData = null;
		}
	});
	
	return overviewData;

}

//javascript for reporting_reports page

//Reports Page Generate report button actions
$(document).on('change', '#generate-survey-reports', function() {
	
	var selectedVal = $('#generate-survey-reports').val();
	var key = parseInt(selectedVal);
	if(key == 101 || key == 102 || key == 103){
		$('#date-pickers').hide();
	}else{
		$('#date-pickers').show();
	}
});

$(document).on('click', '#reports-generate-report-btn', function(e) {
	var selectedValue = $('#generate-survey-reports').val();
	var key = parseInt(selectedValue);
	var startDate = $('#dsh-start-date').val();
	var endDate = $("#dsh-end-date").val();
	
	var success = false;
	var messageToDisplay;
	var payload = {
			"startDate" : startDate,
			"endDate" : endDate,
			"reportId" : key
		};
	
	showOverlay();
		$.ajax({
			url : "./savereportingdata.do?startDate="+payload.startDate+"&endDate="+payload.endDate+"&reportId="+payload.reportId,
			type : "POST",
			dataType:"TEXT",
			async:false,
			success : function(data) {
				success=true;
				messageToDisplay = data;
				showInfoForReporting(messageToDisplay);
			},
			complete : function() {	
				hideOverlay();
			},
			error : function(e) {
				if (e.status == 504) {
					redirectToLoginPageOnSessionTimeOut(e.status);
					return;
				}
			}
		});
});

$(document).on('click', '.err-close-rep', function() {
	hideError();
	hideInfo();
	location.reload(true);
});

function getRecentActivityList(startIndex,batchSize){
	var recentActivityList=null;
	var payload={
			"startIndex" : startIndex,
			"batchSize" : batchSize
	}
	$.ajax({
		async : false,
		url : "/fetchrecentactivities.do?startIndex="+payload.startIndex+"&batchSize="+payload.batchSize,
		type : "GET",
		cache : false,
		dataType : "json",
		success : function(data) {
			if (data.status == 200) {
				$.ajax({
					async : false,
					url : data.url,
					type : "GET",
					cache : false,
					dataType : "json",
					success : function(response) {
						recentActivityList = JSON.parse(response);
					},
					error : function(e) {
						if (e.status == 504) {
							redirectToLoginPageOnSessionTimeOut(e.status);
							return;
						}
						recentActivityList = null;
					}
				});
			}else{
				recentActivityList = null;
			}
		},
		error : function(e) {
			if (e.status == 504) {
				redirectToLoginPageOnSessionTimeOut(e.status);
				return;
			}
			recentActivityList = null;
		}
	});	
	return recentActivityList;
}

function getRecentActivityCount(){
	var recentActivityCount=0;
	$.ajax({
		async : false,
		url : "/fetchrecentactivitiescount.do",
		type : "GET",
		cache : false,
		dataType : "json",
		success : function(response) {
			recentActivityCount = parseInt(response);
		},
		error : function(e) {
			if (e.status == 504) {
				redirectToLoginPageOnSessionTimeOut(e.status);
				return;
			}
			recentActivityCount = 0;
		}
	});
	
	return recentActivityCount;
}

function getStatusString(status){
		var statusString;
		switch(status){
		case 1: statusString='Pending';
			break;
		case 0: statusString='Download';
			break;
		case 2: statusString='Failed';
			break;
		default: statusString='Failed'
		}
		return statusString;
	}
var startIndex=0;
var batchSize=10
var tableHeaderData;
var recentActivityList;

function drawRecentActivity(start,batchSize,tableHeader){
	
	tableHeaderData=tableHeader;
	startIndex=start;
	recentActivityList = getRecentActivityList(startIndex,batchSize);
	var tableData=''; 
	for(var i=0;i<recentActivityList.length;i++){
		
		var statusString = getStatusString(recentActivityList[i][6]);
		var startDate = getDateFromDateTime(recentActivityList[i][2]);
		var endDate =getDateFromDateTime(recentActivityList[i][3]);
		
		tableData += "<tr id='recent-activity-row"+i+"' class=\"u-tbl-row user-row \">"
			+"<td class=\"v-tbl-recent-activity fetch-name hide\">"+i+"</td>"
			+"<td class=\"v-tbl-recent-activity fetch-name txt-bold tbl-black-text\">"+recentActivityList[i][0]+"</td>"
			+"<td class=\"v-tbl-recent-activity fetch-email txt-bold tbl-blue-text\">"+recentActivityList[i][1]+"</td>"
			+"<td class=\"v-tbl-recent-activity fetch-email txt-bold tbl-black-text "+(startDate==null?("recent-activity-date-range\">"+" "):("\">"+startDate))+" - "+(endDate==null?" ":endDate)+"</td>"
			+"<td class=\"v-tbl-recent-activity fetch-name txt-bold tbl-black-text\">"+recentActivityList[i][4]+" "+recentActivityList[i][5]+"</td>";
		
		if(recentActivityList[i][6]==0){	
		tableData +="<td class=\"v-tbl-recent-activity fetch-name txt-bold \" style='font-size:13px !important;'><a id=\"downloadLink"+i+"\"class='txt-bold tbl-blue-text downloadLink cursor-pointer'>"+statusString+"</a></td>"
			+"<td class=\"v-tbl-recent-activity fetch-name txt-bold \" ><a id=\"recent-act-delete-row"+i+"\" class='txt-bold recent-act-delete-x cursor-pointer'>X</a></td>"
			+"</tr>";
		}else if(recentActivityList[i][6]==2){
			tableData +="<td class=\"v-tbl-recent-activity fetch-name txt-bold \" style='font-size:13px !important;'>"+statusString+"</td>"
			+"<td class=\"v-tbl-recent-activity fetch-name txt-bold\" ><a id=\"recent-act-delete-row"+i+"\" class='txt-bold recent-act-delete-x cursor-pointer'>X</a></td>"
			+"</tr>";
		}else{
			tableData +="<td class=\"v-tbl-recent-activity fetch-name txt-bold \" style='font-size:13px !important;'>"+statusString+"</td>"
				+"<td class=\"v-tbl-recent-activity fetch-name txt-bold\" >  </td>"
				+"</tr>";
		}
	}
	
	var recentActivityCount = getRecentActivityCount();
	if(recentActivityCount == 0){
		tableData='';
		tableData+="</table><div style='text-align:center; margin:20px auto'><span class='incomplete-trans-span'>There are No Recent Activities</span></div>";
		$('#recent-activity-list-table').html(tableData);
	}else{
		$('#recent-activity-list-table').html(tableHeaderData+tableData+"</table>");
	}
	
}

function getDateFromDateTime(dateTime){
	if(dateTime != null){
	return dateTime.match(/[a-zA-z]{3} \d+, \d{4}/)[0];
	}
	
	return null;
}

function deleteRecentActivity(fileUploadId,idIndex){
	showOverlay();
	$.ajax({
		url : "./deletefromrecentactivities.do?fileUploadId="+fileUploadId,
		type : "POST",
		dataType:"TEXT",
		async:false,
		success : function(data) {
			success=true;
			messageToDisplay = data;
			
		},
		complete : function() {	
			hideOverlay();
			
			var recentActivityCount=getRecentActivityCount();
			$('#recent-activity-row'+idIndex).fadeOut(500)
				.promise()
				.done(function(){
					if(recentActivityCount <= startIndex){
						drawRecentActivity(startIndex-10,batchSize,tableHeaderData);
					}else if(recentActivityCount>=10){
						drawRecentActivity(startIndex,batchSize,tableHeaderData);
					}
					showHidePaginateButtons(startIndex, recentActivityCount);
					
					if(recentActivityCount == 0){
						var tableData='';
						tableData+="</table><div style='text-align:center; margin:20px auto'><span class='incomplete-trans-span'>There are No Recent Activities</span></div>";
						$('#recent-activity-list-table').html(tableData);
					}
				});
			},
		error : function(e) {
			if (e.status == 504) {
				redirectToLoginPageOnSessionTimeOut(e.status);
				return;
			}
			messageToDisplay="Sorry! Failed to delete the activity. Please try again later";
			showError(messageToDisplay);
		}
	});
}

$(document).on('click','.downloadLink',function(e){
	var clickedID = this.id;
	var indexRecentActivity = clickedID.match(/\d+$/)[0];
	var downloadLink=recentActivityList[indexRecentActivity][7];
	window.location=downloadLink;
});

$(document).on('click','.recent-act-delete-x',function(e){
	var clickedID = this.id;
	var indexRecentActivity = clickedID.match(/\d+$/)[0];
	var fileUploadId=recentActivityList[indexRecentActivity][8];
	deleteRecentActivity(fileUploadId, indexRecentActivity);
});

function getStartIndex(){
	return startIndex;
}

function getTableHeader(){
	return tableHeaderData;
}

function showHidePaginateButtons(startIndex,recentActivityCount){

	if(startIndex == 0){
		$('#rec-act-page-previous').hide();
	}else{
		$('#rec-act-page-previous').show();
	}
	
	if((recentActivityCount-startIndex)<=10){
		$('#rec-act-page-next').hide();
	}else{
		$('#rec-act-page-next').show();
	}
	
	if(recentActivityCount == 0){
		$('#rec-act-page-previous').hide();
		$('#rec-act-page-next').hide();
	}
}

function activaTab(tab){
    $('.nav-tabs a[href="#' + tab + '"]').tab('show');
};

