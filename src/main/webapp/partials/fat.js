'use strict';
angular.module('google-chart-sample').controller("FatChartCtrl", function ($scope) {
	
	
    var chartDataColumns = [
        {id: "month", label: "Month", type: "string"},
        {id: "peninfiria-id", label: "BMW", type: "number"},
        {id: "massaratip-id", label: "Massarati", type: "number"},
        {id: "volvo-id", label: "Pagani", type: "number"}
    ];
    $scope.chartDataRows = [];
    $scope.chart1 = {};
    
    $scope.chart1.type = "LineChart";
    $scope.chart1.displayed = false;
    $scope.chart1.data = {"cols": chartDataColumns, "rows": $scope.chartDataRows};

    $scope.chart1.options = {
        "title": "Today's Stock Market",
        "isStacked": "true",
        "fill": 20,
        "displayExactValues": true,
        "vAxis": {
            "title": "Stock Price", "gridlines": {"count": 10}
        },
        "hAxis": {
            "title": "Time"
        }
    };


    var formatCollection = [
        {
            name: "color",
            format: [
                {
                    columnNum: 4,
                    formats: [
                        {
                            from: 0,
                            to: 3,
                            color: "white",
                            bgcolor: "red"
                        },
                        {
                            from: 3,
                            to: 5,
                            color: "white",
                            fromBgColor: "red",
                            toBgColor: "blue"
                        },
                        {
                            from: 6,
                            to: null,
                            color: "black",
                            bgcolor: "green"
                        }
                    ]
                }
            ]
        },
        {
            name: "arrow",
            checked: false,
            format: [
                {
                    columnNum: 1,
                    base: 19
                }
            ]
        },
        {
            name: "date",
            format: [
                {
                    columnNum: 5,
                    formatType: 'long'
                }
            ]
        },
        {
            name: "number",
            format: [
                {
                    columnNum: 4,
                    prefix: '$'
                }
            ]
        },
        {
            name: "bar",
            format: [
                {
                    columnNum: 1,
                    width: 100
                }
            ]
        }
    ]

    $scope.chart1.formatters = {};

    $scope.chart = $scope.chart1;
    $scope.cssStyle = "height:600px; width:100%;";
	$scope.getStockUpdates = function(stockUpdates)
	{
		//console.log("Received data from websocket: ", stockUpdates + " var : " + $scope.chartDataRows);
		console.log("Received data from websocket: ", stockUpdates + " var : " + $scope.chartDataRows);
		//$scope.chart.data = {"cols": chartDataColumns, "rows": JSON.parse(stockUpdates)};
		$scope.chart1.data = {"cols": chartDataColumns, "rows": JSON.parse(stockUpdates)};
		$scope.chart = $scope.chart1;
		
	};
    $scope.chartSelectionChange = function () {
		console.log(" var : " + $scope.chartDataRows);
        if (($scope.chart.type === 'Table' && $scope.chart.data.cols.length === 6 && $scope.chart.options.tooltip.isHtml === true) ||
            ($scope.chart.type != 'Table' && $scope.chart.data.cols.length === 6 && $scope.chart.options.tooltip.isHtml === false)) {
            $scope.chart.data.cols.pop();
            delete $scope.chart.data.rows[0].c[5];
            delete $scope.chart.data.rows[1].c[5];
            delete $scope.chart.data.rows[2].c[5];
        }


        if ($scope.chart.type === 'Table') {

            $scope.chart.options.tooltip.isHtml = false;

            $scope.chart.data.cols.push({id: "data-id", label: "Date", type: "date"});
            $scope.chart.data.rows[0].c[5] = {v: "Date(2013,01,05)"};
            $scope.chart.data.rows[1].c[5] = {v: "Date(2013,02,05)"};
            $scope.chart.data.rows[2].c[5] = {v: "Date(2013,03,05)"};
        }

    }


    $scope.formatCollection = formatCollection;
    $scope.toggleFormat = function (format) {
        $scope.chart.formatters[format.name] = format.format;
    };

    $scope.chartReady = function () {
        fixGoogleChartsBarsBootstrap();
    }

    function fixGoogleChartsBarsBootstrap() {
        // Google charts uses <img height="12px">, which is incompatible with Twitter
        // * bootstrap in responsive mode, which inserts a css rule for: img { height: auto; }.
        // *
        // * The fix is to use inline style width attributes, ie <img style="height: 12px;">.
        // * BUT we can't change the way Google Charts renders its bars. Nor can we change
        // * the Twitter bootstrap CSS and remain future proof.
        // *
        // * Instead, this function can be called after a Google charts render to "fix" the
        // * issue by setting the style attributes dynamically.

        $(".google-visualization-table-table img[width]").each(function (index, img) {
            $(img).css("width", $(img).attr("width")).css("height", $(img).attr("height"));
        });
    };

});



