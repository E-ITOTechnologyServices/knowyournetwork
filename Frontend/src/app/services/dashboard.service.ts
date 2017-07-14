import { Injectable, Output, EventEmitter } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from "@angular/http";
import { Cookie } from 'ng2-cookies/ng2-cookies';
import { AppConfig } from '../app.config';

declare var $: any;

@Injectable()
export class DashboardService {
    dateTimeRange: any = { 'dateTimeFrom': null, 'dateTimeTo': null }; // datetime ranges for search
    query: string = '';
    report: string = '';
    counts = new Array();
    lastSearch: any = undefined; // Saves last triggered search
    settings = new Array();

    constructor(private http: Http) {}

    // For detecting changes
    @Output() newAction: any = new EventEmitter();

    public sendNewAction(action: string): void {
        this.newAction.emit(action);
    }

    // Provide needed data file regarding on source type
    public dataSource(identifier: string): string {
        return AppConfig.appURL + 'rest/' + identifier;
    }

    // Return configuration for datatables.net ajax option
    public dataTableSource(identifier: string, tableID: any, parameter0: string, value0: any, parameter1: string, value1: any): any {
        let tableSelector: string;
        tableID ? tableSelector = '#' + tableID : tableSelector = '#' + identifier + '-table';

        let url = this.dataSource(identifier);

        let JSON = new Object();
        // Append seconds and milliseconds for Middleware
        let range = Object.assign(new Object(), this.dateTimeRange); // Copy dateTimeRange and extend it
        if (range.dateTimeFrom) {
            range.dateTimeFrom += ':00.000';
        }
        if (range.dateTimeTo) {
            range.dateTimeTo += ':00.000';
        }
        if (range.dateTimeFrom && range.dateTimeTo) {
            JSON['dateTimeRange'] = range;
        }

        // Usual parameters
        if (parameter0) {
            JSON[parameter0] = value0 ;
        }
        if (parameter1) {
            JSON[parameter1] = value1;
        };

        return {
            'url': url,
            'type': 'POST',
            'data': JSON,
            'dataSrc': function(response: any) {
                // Handle data if status=1
                if (!response.status) {
                    return response.content.data;
                // Handle error if status=0
                }else {
                    // Instead of empty option show error message
                    let errorMessage = response.errorMessage;
                    if (typeof errorMessage === 'undefined') {
                        errorMessage = '[Dashboard] There is an error in JSON structure.';
                    }
                    $(tableSelector).dataTable().fnSettings().oLanguage.sEmptyTable = '<span class="error-message">' + errorMessage + '</span>';
                    return false;
                }
            },
            'error': function(){
                $(tableSelector + ' .dataTables_empty').html('<span class="error-message">[Dashboard] Error loading file: ' + url + '.</span>');
                return false;
            }
        };
    }

    // Standard method for HTTP post request to Node.js MW
    httpPOST(source: string, params:any): any {
        // Do http.post and return object
        return this.http.post(
            source,
            this.httpPOSTbody('', params),
            new RequestOptions({ headers: new Headers ({ 'Content-Type': 'application/x-www-form-urlencoded' }), method: "post" })
        );
    }

    httpPOSTbody(inObject: string, params: any): string {
        let body = '';
        let i = 0; // Marker for charachter &

        // Iterate through all params and do recursive magic
        for(let param in params){
            if(typeof params[param] === 'object'){
                body += this.httpPOSTbody(param, params[param]);
            }else{
                body += ( i ? '&' : '' ) + ( inObject? inObject + '[' + param + ']' : param ) + '=' + params[param];
            }
            i++;
        }

        // return result :)
        return body;
    }

    // This method validates IPv4 format
    validateIPv4(ipaddress: string): boolean {
        if (/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ipaddress)) {
            return true;
        }
        return false;
    }

    // Init scroll to search after click on "Search for IP"
    initScrollToSearch(): void {
        $('.search-for-ip').click(function(e: any){
            e.preventDefault();
            $('html, body').animate({ scrollTop: 0 }, 300);
            $('#query').focus();
        });
    }

    // Addon for panels - check if class can be added with ngIf
    onCollapse(module: string, isCollapsed: boolean): void {
        !isCollapsed ? $('#' + module + '-title').addClass('collapsed') : $('#' + module + '-title').removeClass('collapsed');
    }
}
