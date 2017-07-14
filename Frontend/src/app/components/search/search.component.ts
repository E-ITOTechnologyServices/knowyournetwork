import { Component } from '@angular/core';
import { Location, LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Validators, FormGroup, FormArray, FormBuilder } from '@angular/forms';

import { DashboardService, NewsTickerService } from '../../services/index';

declare var $: any;
declare  var moment: any;

@Component({
    moduleId: module.id,
    selector: 'search',
    templateUrl: 'search.component.html',
    providers: [ Location, { provide: LocationStrategy, useClass: HashLocationStrategy }]
})
export class SearchComponent{
    private location: Location;
    private supscription: any;
    searchError: any = { dateFrom: false, dateTo: false };
    minDateForTo: any = null;
    maxDateForFrom: any = new Date();
    defaultDate: any = { from: null, to: null };
    dateTimeFormat: string = 'YYYY-MM-DD HH:mm';
    now = new Date();

    constructor(public dashboardService: DashboardService, public newsTickerService: NewsTickerService, location: Location){
        this.location = location;

        // Do hashAction when is app loaded
        this.hashAction();

        // Listen for hash changes
        this.supscription = this.location.subscribe((e) => {
            if(e.type == 'hashchange'){
                this.hashAction();
            }
        })
    }

    ngOnDestroy () {
        this.supscription.unsubscribe();
    }

    // Main search
    search(): void {
        this.dashboardService.lastSearch = this.dashboardService.query;

        // Do search if is form valid
        if( this.isSearchFormValid() ) {
            // Reload newsTicker data
            this.newsTickerService.reload();

            // Update default date
            this.defaultDate.for = this.dashboardService.dateTimeRange.dateTimeFrom;
            this.defaultDate.to = this.dashboardService.dateTimeRange.dateTimeTo;

            // Initialize Search trigger for all modules
            let modules = ['reports', 'hostReports', 'hostInfo', 'syslog', 'kibana', 'graphs', 'ruleset'];
            for(let module of modules){
                this.dashboardService.sendNewAction(module + 'Search');
            }

            // Clear URL value in the address bar
            this.location.go('');
        }
    }

    // Validation for search fields
    isSearchFormValid(): boolean {

        // Check if is dateTimeRange properly set
        this.searchError = { dateFrom: false, dateTo: false };
        if(this.dashboardService.dateTimeRange.dateTimeTo && !this.dashboardService.dateTimeRange.dateTimeFrom){
            this.searchError.dateFrom = true;
            this.searchError.dateFields = true;
            return false;
        }else if(!this.dashboardService.dateTimeRange.dateTimeTo && this.dashboardService.dateTimeRange.dateTimeFrom){
            this.searchError.dateTo = true;
            this.searchError.dateFields = true;
            return false;

            // Check for date range condition
        }else if(Date.parse(this.dashboardService.dateTimeRange.dateTimeFrom) >= Date.parse(this.dashboardService.dateTimeRange.dateTimeTo)){
            this.searchError.dateRange = true;
            return false;

            // Check is date format correct
        }else if(this.dashboardService.dateTimeRange.dateTimeFrom && !moment(this.dashboardService.dateTimeRange.dateTimeFrom, this.dateTimeFormat, true).isValid() ) {
            this.searchError.dateFrom = true;
            this.searchError.dateFormat = true;
            return false;
        }else if(this.dashboardService.dateTimeRange.dateTimeTo && !moment(this.dashboardService.dateTimeRange.dateTimeTo, this.dateTimeFormat, true).isValid() ){
            this.searchError.dateTo = true;
            this.searchError.dateFormat = true;
            return false;

            // Form is valid
        }else{
            return true;
        }
    }

    // Live search actions
    liveSearch(): void {
        this.newsTickerService.search();
        this.dashboardService.sendNewAction('hostReportsLiveSearch'); // send action to reports listener
        this.dashboardService.sendNewAction('AQLrulesLiveSearch');
        this.dashboardService.sendNewAction('rulesetLiveSearch');
    }

    onDateFromChange(): void {
        if(this.dashboardService.dateTimeRange.dateTimeFrom) {
            this.defaultDate.for = new Date(this.dashboardService.dateTimeRange.dateTimeFrom);
            let newValue = new Date(this.dashboardService.dateTimeRange.dateTimeFrom);
            newValue.setDate(newValue.getDate()-1);
            this.minDateForTo = newValue;
        }else{
            this.minDateForTo = new Date();
        }

        if(this.dashboardService.dateTimeRange.dateTimeTo) {
            this.defaultDate.to = new Date(this.dashboardService.dateTimeRange.dateTimeTo);
        }
    }

    onDateToChange(): void {
        if(this.dashboardService.dateTimeRange.dateTimeTo) {
            this.defaultDate.to = new Date(this.dashboardService.dateTimeRange.dateTimeTo);
            this.maxDateForFrom = new Date(this.dashboardService.dateTimeRange.dateTimeTo);
            this.maxDateForFrom.setMinutes(59);
            this.maxDateForFrom.setHours(23);
        }else{
            this.maxDateForFrom = new Date();
        }

        if(this.dashboardService.dateTimeRange.dateTimeFrom) {
            this.defaultDate.from = new Date(this.dashboardService.dateTimeRange.dateTimeFrom);
        }
    }

    // Deletes the value of the fields in search form
    clearFields(): void {
        this.dashboardService.dateTimeRange.dateTimeFrom = '';
        this.dashboardService.dateTimeRange.dateTimeTo = '';
        this.dashboardService.query = '';

        // Focus query field
        $("#search-query").focus();

        // Do live search again - with empty fields
        this.liveSearch();
    }

    // Starts search action if is search hash provided
    hashAction(): void {
        // Get search param
        let item = this.location.path().split("=");
        let name = item[0]
        let value = item[1];

        // Check for search param and trigger search
        if(name == 'query' && value){
            setTimeout(() => {
                this.dashboardService.query = value;
                this.search();
                this.liveSearch();
            }, 0);
        }
    }
}
