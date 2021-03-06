angular.module('socialSurvey.dashBoard',['ui.router'])

.config(['$stateProvider',function($stateProvider){

  $stateProvider.state('dashBoard',{
    url: '/dashBoard',
    templateUrl: 'dashBoard/dashBoard.html',
    controller: 'dashBoardCtrl'
  });

}])

.controller('dashBoardCtrl',['$scope',function($scope){

  FusionCharts.ready(function () {
      var revenueChart = new FusionCharts({
          type: 'stackedcolumn3dlinedy',
          renderAt: 'reviewRatingChart',
          width: '100%',
          height: '350',
          dataFormat: 'jsonUrl',
          dataSource: 'resources/json/averageRating.json'
      });
      revenueChart.render();
  });

  FusionCharts.ready(function () {
    var topStores = new FusionCharts({
        type: 'bar2d',
        renderAt: 'experienceChart',
        width: '70%',
        height: '300',
        dataFormat: 'jsonUrl',
        dataSource: "resources/json/ratingsJson.json"
    });
    topStores.render();
});

FusionCharts.ready(function () {
    var revenueChart = new FusionCharts({
        type: 'column2d',
        renderAt: 'mazdaCharts',
        width: '100%',
        height: '150',
        dataFormat: 'jsonUrl',
        dataSource: 'resources/json/mazdaRatings.json'
    });
    revenueChart.render();
});

FusionCharts.ready(function () {
  var sSRatingChart = new FusionCharts({
      type: 'bar2d',
      renderAt: 'sSRatingChart',
      width: '90%',
      height: '150px',
      dataFormat: 'jsonUrl',
      dataSource: "resources/json/sSRatingsJson.json"
  });
  sSRatingChart.render();
});

FusionCharts.ready(function(){
    var fusioncharts = new FusionCharts({
    type: 'angulargauge',
    renderAt: 'gaugeChartContainer',
    id: 'cs-angular-gauge',
    width: '100%',
    height: '170',
    dataFormat: 'jsonUrl',
    dataSource: "resources/json/gaugeChart.json"
}
);
    fusioncharts.render();
});
}])
