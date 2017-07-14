import { Component, ViewChild } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from "@angular/http";
import { ModalDirective } from 'ng2-bootstrap/ng2-bootstrap';

import { DashboardService } from '../../services/index';

declare var $: any;
declare  var moment: any;

@Component({
    moduleId: module.id,
    selector: 'reports',
    templateUrl: 'reports.component.html'
})
export class ReportsComponent {

    @ViewChild('modalMessage') public modalMessage:ModalDirective;

    public isCollapsed = true;
    public reports: any;
    // DateTime parameters
    formError: any = { dateFrom: false, dateTo: false };
    model: any = {
        dateTimeRange: { 'dateTimeFrom': null, 'dateTimeTo': null }, // datetime ranges for search
        maxDestSyn: 5,
        maxDestIpPing: 6
    };
    modals: any = { message: {} };
    minDateForTo: any = null;
    maxDateForFrom: any = new Date();
    defaultDate: any = { from: null, to: null };
    dateTimeFormat: string = 'YYYY-MM-DD HH:mm';
    now = new Date();

    constructor(private dashboardService: DashboardService, private http: Http) {
        this.getReports();
    }

    // Load routers from REST
    private getReports(): void {
        this.http.get(this.dashboardService.dataSource('reports/getList'))
            .map((res:Response) => res.json())
            .subscribe(
                data => { this.reports = data.content.data; },
                err => {  }
            );
    }

    // Trigger report generation
    generateReport(): void {

        // First do form validation - errors are shown automatically
        if( this.isGenerateFormValid() ) {

            // HTTP post request which creates report
            $("#overlay").show();
            this.dashboardService.httpPOST(this.dashboardService.dataSource('reports/createReport'), { dateTimeRange: { dateTimeFrom: this.model.dateTimeRange.dateTimeFrom + ' 00:00:00.000', dateTimeTo: this.model.dateTimeRange.dateTimeTo + ' 00:00:00.000' }, maxDestSyn: this.model.maxDestSyn, maxDestIpPing: this.model.maxDestIpPing })
            .map((res:Response) => res.json())
            .subscribe(
                data => {
                    $("#overlay").hide();

                    // Set modal message
                    this.modals.message.body = data.errorMessage;

                    // Status is OK
                    if( !data.status ){
                    // Error in creating
                        this.modals.message.error = false;
                        this.getReports(); // Load new reports
                    }else{
                        this.modals.message.error = true;
                    }

                    this.modalMessage.show();
                },
                err => {  }
            );
        }
    }

    // Validation for search fields
    isGenerateFormValid(): boolean {

        this.formError = { dateFrom: false, dateTo: false };
        // Check if is model.dateTimeRange properly set - if necessary fields are filled
        if(!this.model.dateTimeRange.dateTimeTo || !this.model.dateTimeRange.dateTimeFrom){
            if(!this.model.dateTimeRange.dateTimeFrom) this.formError.dateFrom = true;
            if(!this.model.dateTimeRange.dateTimeTo) this.formError.dateTo = true;
            this.formError.dateFields = true;
            return false;

        // Check for date range condition
        }else if(Date.parse(this.model.dateTimeRange.dateTimeFrom) >= Date.parse(this.model.dateTimeRange.dateTimeTo)){
            this.formError.dateRange = true;
            return false;

        // Check is date format correct
        }else if(this.model.dateTimeRange.dateTimeFrom && !moment(this.model.dateTimeRange.dateTimeFrom, this.dateTimeFormat, true).isValid() ) {
            this.formError.dateFrom = true;
            this.formError.dateFormat = true;
            return false;

        }else if(this.model.dateTimeRange.dateTimeTo && !moment(this.model.dateTimeRange.dateTimeTo, this.dateTimeFormat, true).isValid() ) {
            this.formError.dateTo = true;
            this.formError.dateFormat = true;
            return false;

        // Form is valid
        }else{
            return true;
        }
    }

    onDateFromChange(): void {
        if(this.model.dateTimeRange.dateTimeFrom) {
            this.defaultDate.for = new Date(this.model.dateTimeRange.dateTimeFrom);
            let newValue = new Date(this.model.dateTimeRange.dateTimeFrom);
            newValue.setDate(newValue.getDate()-1);
            this.minDateForTo = newValue;
        }else{
            this.minDateForTo = new Date();
        }

        if(this.model.dateTimeRange.dateTimeTo) {
            this.defaultDate.to = new Date(this.model.dateTimeRange.dateTimeTo);
        }
    }

    onDateToChange(): void {
        if(this.model.dateTimeRange.dateTimeTo) {
            this.defaultDate.to = new Date(this.model.dateTimeRange.dateTimeTo);
            this.maxDateForFrom = new Date(this.model.dateTimeRange.dateTimeTo);
            this.maxDateForFrom.setMinutes(59);
            this.maxDateForFrom.setHours(23);
        }else{
            this.maxDateForFrom = new Date();
        }

        if(this.model.dateTimeRange.dateTimeFrom) {
            this.defaultDate.from = new Date(this.model.dateTimeRange.dateTimeFrom);
        }
    }

}
