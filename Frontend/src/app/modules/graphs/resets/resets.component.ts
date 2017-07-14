import { Component, OnInit } from '@angular/core';

import { DashboardService } from '../../../services/index';
import { AppConfig } from "../../../app.config";
import * as d3 from 'd3';
import * as _ from 'underscore';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'resets',
    templateUrl: 'resets.component.html'
})
export class ResetsComponent {

    constructor(private dashboardService:DashboardService) {}

    public haveResults: boolean = undefined;
    public errorMessage: string;

    data: any = null;
    xScale: any = null;
    yScale: any = null;
    svgLine: any = null;
    colorScale: any = null;
    perspectiveOffsetX: number = 10;
    perspectiveOffsetY: number = 6;
    chartHeight: number = 180;
    lineWidth: number = 580;
    lineHeight: number = 150;
    xUnits: number = 7;
    yAxis: any = null;
    chartName: string = "resets-chart"; // ID

    menu = [
        {'label': 'week', 'sortBy': 'week'},
        {'label': 'Maximum', 'sortBy': 'max'},
        {'label': 'Average', 'sortBy': 'mean'}
    ];

    uiState: any = {
        selectedIndex: 0,
        selectedDatum: null, // Automatically updated
        sortBy: 'week',
        sorting: false
    };

    sortFunction: any = {};
    openingTimer: any = null;

    ngOnInit():void {
        let that = this;

        this.loadData();

        // This part is for reloading graph after new search
        this.dashboardService.newAction.subscribe(
            (action:string) => {
                if (action == 'resetsReload') {
                    // Reset HTML and jQuery listeners
                    $('#'+that.chartName+' svg, #'+that.chartName+' .menu, #'+that.chartName+' .info').html('');
                    $('#'+that.chartName).off();

                    // Load new data
                    that.loadData();
                }
            }
        );
    }

    // Loads chart data and initializes it if is data available
    loadData(): void {
        let that = this;
        let file = this.dashboardService.dataSource('resets');

        let dateTimeRange = '';
        if(this.dashboardService.dateTimeRange.dateTimeFrom && this.dashboardService.dateTimeRange.dateTimeTo) {
            dateTimeRange = '&dateTimeRange[dateTimeFrom]=' + this.dashboardService.dateTimeRange.dateTimeFrom + ':00.000' + '&dateTimeRange[dateTimeTo]=' + this.dashboardService.dateTimeRange.dateTimeTo + ':00.000';
        }

        d3.json(file, function(error: any, data: any) {
            if(error){
                that.errorMessage = '[Dashboard] Error loading file: '+file+'.';
            }else{
                if(!data.status && data.content){
                    delete that.errorMessage;

                    if(!data.content.info.size){
                        that.haveResults = false;
                    }else{
                        that.haveResults = true;
                        setTimeout( () => {
                            that.data=data.content;
                            that.init();
                            that.updateVisibleWeeks();
                        }, 0);
                    }
                }else{
                    // Handle some more error types
                    var errorMessage=data.errorMessage;
                    if(typeof errorMessage === 'undefined') errorMessage='[Dashboard] There is an error in JSON structure.';

                    that.errorMessage=errorMessage;
                }
            }
        }).header("Content-Type","application/x-www-form-urlencoded").send("POST","ipAddress=" + this.dashboardService.lastSearch + dateTimeRange);
    }

    translate(x: any, y: any): string {return 'translate('+x+','+y+')';};

    init(): void {
        //var data: any = {};
        var xUnits=this.xUnits;

        this.yAxis=this.data.info.maximum;

        var	dataset = _.map(this.data.counts, function(data: any, k: any) {
            var weekAve = _.reduce(data, function(m: any, v: any) {return m + v;}, 0) / xUnits;
            var weekMax = _.max(data);
            return {week: k, data: data, mean: weekAve, max: weekMax};
        });
        this.data.counts = {data: dataset.reverse() , extent: [0, this.yAxis]};

        this.sortFunction.week = function(a: any, b: any) {return d3.descending(a.week, b.week);}
        this.sortFunction.mean = function(a: any, b: any) {return d3.descending(a.mean, b.mean);}
        this.sortFunction.max = function(a: any, b: any) {return d3.descending(a.max, b.max);}

        this.initChart();
        this.initEvents();
        this.initMenu();
    };

    initMenu(): void {
        var that = this;
        d3.select('#'+that.chartName+' .menu')
            .text('Sort by: ')
            .selectAll('span')
            .data(this.menu)
            .enter()
            .append('span')
            .html(function(d: any, i: any) {
                var html = '<span class="button">' + d.label + '</span>';
                if(i < that.menu.length - 1) html += ' / ';
                return html;
            });

        d3.select('#'+that.chartName+' .menu')
            .selectAll('span.button')
            .classed('selected', function(d: any, i: any) {return i===0;})
            .on('click', function() {
                let d: any = d3.select(this.parentNode).datum();
                d3.selectAll('#'+that.chartName+' .menu span.button')
                    .classed('selected', false);

                d3.select(this)
                    .classed('selected', true);

                that.updateSort(d.sortBy);
            });
    };

    updateVisibleWeeks(): void {
        var that = this; // Better way to do this?

        let index = that.uiState.selectedIndex;
        let weeks = d3.selectAll('#'+that.chartName+' .weeks g.week');

        weeks.classed('hover', false);

        weeks
            .filter(function(d: any, i: any) {return i === index;})
            .classed('hover', true);

        d3.selectAll('.axes')
            .attr('transform', that.translate(50 + index * that.perspectiveOffsetX, that.chartHeight + that.yScale(0) + -index * that.perspectiveOffsetY));

        weeks
            .style('opacity', function(d: any, i: any) {
                if(i < index) return 0;
                return that.colorScale(i);
            });

        var datum = weeks.filter(function(d: any, i: any) { return i === index;}).datum();
        that.uiState.selectedDatum = datum;

        that.updateInfo();
    };

    updateInfo(): void {
        var that = this;
        var d = that.uiState.selectedDatum;
        var html = '<h2>' + d.week + ' - Number of resets</h2>';
        html += _.has(that.data.info, d.week) ? that.data.info[d.week].text : '';
        html += '<p>Largest amount: ' + d.max + '</p>';
        html += '<p>Week average: ' + d.mean.toFixed(1) + '</p>';

        d3.select('#'+that.chartName+' .info')
            .html(html);
    };

    initEvents(): void {
        var that=this;
        $("#"+that.chartName).on('scroll touchmove mousewheel DOMMouseScroll', function(evt: any){
            // Stop default scroll
            evt.preventDefault();
            evt.stopPropagation();

            if(that.uiState.sorting) return;

            if (!evt) evt = event;
            var direction = (evt.originalEvent.deltaY<0 || evt.originalEvent.detail > 0) ? 1 : -1;

            if( !(that.uiState.selectedIndex==(that.data.info.size-1) && direction==1) && !(that.uiState.selectedIndex==0 && direction==-1) ){
                that.uiState.selectedIndex +=direction;
                that.updateVisibleWeeks();
            }
        });

        // Keydown handler - use arrows up and down to change direction
        var keydownHandler = function(e: any){
            e.preventDefault();

            switch(e.keyCode){
                case 38:
                    if( !(that.uiState.selectedIndex==(that.data.info.size-1))){
                        that.uiState.selectedIndex +=1;
                        that.updateVisibleWeeks();
                    }
                    break;

                case 40:
                    if(!(that.uiState.selectedIndex==0)){
                        that.uiState.selectedIndex -=1;
                        that.updateVisibleWeeks();
                    }
                    break;
            }
        }

        $('#'+this.chartName+'-wrapper')
            .hover(
                function() {
                    $(document).bind('keydown', keydownHandler);

                },
                function() {
                    $(document).unbind('keydown', keydownHandler);
                }
            );
    };

    initChart(): void {
        var that = this;

        this.xScale = d3.scale.linear()
            .domain([0, (this.xUnits-1)])
            .range([0, this.lineWidth]);

        this.yScale = d3.scale.linear()
            .domain(this.data.counts.extent)
            .range([this.lineHeight, 0]);

        this.colorScale = d3.scale.linear()
            .domain([0, this.data.info.size-1])
            .range([1, 0.5]);

        this.svgLine = d3.svg.line()
            .interpolate('linear') // basis
            .x(function(d: any, i: any) {return that.xScale(i);})
            .y(function(d: any) {return that.yScale(d);});

        // week LINES
        var weeks = d3.select('#'+that.chartName+' svg')
            .append('g')
            .classed('weeks', true)
            .attr('transform', this.translate(50, this.chartHeight))
            .selectAll('g.week')
            .data(this.data.counts.data)
            .enter()
            .append('g')
            .attr('class', function(d: any, i: any) {return 'week-' + d.week;})
            .classed('week', true)
            .sort(this.sortFunction[this.uiState.sortBy])
            .attr('transform', function(d: any, i: any) {
                return that.translate(i * that.perspectiveOffsetX, -i * that.perspectiveOffsetY);
            })
            .style('opacity', function(d: any, i: any) {
                return that.colorScale(i);
            });

        // Add paths
        weeks
            .append('path')
            .attr('d', function(d: any, i: any) {
                return that.svgLine(d.data);
            });

        // Base and end lines
        weeks
            .append('line')
            .classed('base', true)
            .attr('x1', 0)
            .attr('y1', this.yScale(0))
            .attr('x2', this.xScale(this.xUnits-1))
            .attr('y2', this.yScale(0));

        weeks
            .append('line')
            .classed('start', true)
            .attr('x1', 0)
            .attr('y1', this.yScale(0))
            .attr('x2', 0)
            .attr('y2', function(d: any) {return that.yScale(d.data[0]);});

        weeks
            .append('line')
            .classed('end', true)
            .attr('x1', this.xScale(this.xUnits-1))
            .attr('y1', this.yScale(0))
            .attr('x2', this.xScale(this.xUnits-1))
            .attr('y2', function(d: any) { return that.yScale(d.data[(that.xUnits-1)]); });


        d3.select('#'+that.chartName+' svg')
            .append('g')
            .classed('axes', true)
            .attr('transform', this.translate(50, this.chartHeight));

        this.renderAxes();
    };

    renderAxes(): void {

        var hours=['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];

        var monthScale = d3.scale.ordinal()
            .domain(hours)
            .rangePoints([0, this.lineWidth]);

        // Split maximum value to 5 intervals
        var interval=this.yAxis/5;
        var tickValues: any[] = [];
        for(let i=0; i<=this.yAxis; i+=interval){
            tickValues.push(i);
        }

        var yAxis = d3.svg.axis()
            .scale(this.yScale)
            .orient('left')
            .tickValues(tickValues);

        d3.select('#'+this.chartName+' .axes')
            .append('g')
            .classed('axis y', true)
            .attr('transform', this.translate(0, -this.yScale(0)))
            .call(yAxis);

        var xAxis = d3.svg.axis()
            .scale(monthScale)
            .orient('bottom');

        d3.select('#'+this.chartName+' .axes')
            .append('g')
            .classed('axis x', true)
            .call(xAxis);
    };

    updateSort(sortBy: any): void {
        let that = this;
        this.uiState.sortBy = sortBy;
        let index: number;

        // Do the sort
        var weeks = d3.select('#'+that.chartName+' .weeks')
            .selectAll('g.week')
            .sort(this.sortFunction[this.uiState.sortBy]);

        // Persist the chosen week: get the index of the chosen week
        d3.selectAll('#'+that.chartName+' .weeks g.week')
            .each(function(d: any, i: any) {
                if(d.week === that.uiState.selectedDatum.week) index = i;
            });
        that.uiState.selectedIndex = index;

        // Transform the axes
        d3.selectAll('.axes')
            .transition()
            .duration(2000)
            .attr('transform', that.translate(50 + index * that.perspectiveOffsetX, that.chartHeight + that.yScale(0) + -index * that.perspectiveOffsetY))
            .each('end', function() {
                that.uiState.sorting = false;
            });

        // Transform the week paths
        d3.selectAll('#'+that.chartName+' .weeks .week')
            .transition()
            .duration(2000)
            .attr('transform', function(d: any, i: any) {
                return that.translate(i * that.perspectiveOffsetX, -i * that.perspectiveOffsetY);
            })
            .style('opacity', function(d: any, i: any) {
                if(i < index) return 0;
                return that.colorScale(i);
            });

        this.uiState.sorting = true;
    };
}