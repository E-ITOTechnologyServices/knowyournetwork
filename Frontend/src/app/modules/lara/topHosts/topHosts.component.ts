import { Component, OnInit, ViewChild } from '@angular/core';

import { DashboardService } from '../../../services/index';
import { PolicyFormService } from "../policyForm/policyForm.service";
import { AppConfig } from "../../../app.config";
declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'topHosts',
    templateUrl: 'topHosts.component.html'
})
export class TopHostsComponent {

    private supscription: any;
    isCollapsed = true;
    isDynamicCollapsed = true;
    isStaticCollapsed = true;
    tables = new Array();
    visibility = false;

    constructor(private dashboardService: DashboardService, private policyFormService: PolicyFormService) {
        this.supscription = this.dashboardService.newAction.subscribe(
                (action:string) => {
                    switch (action) {
                        case 'displayPolicies':
                            this.displayPolicies();
                            break;
                    }
                }
            );
    }

    ngOnDestroy () {
        this.supscription.unsubscribe();
    }

    displayPolicies(): void {
        this.loadTopSourcesDynamic();
        this.loadTopDestinationsDynamic();
        this.loadTopSourcesStatic();
        this.loadTopDestinationsStatic();
        this.visibility = true;
    }

    // Top sources dynamic
    loadTopSourcesDynamic(): void {
        if(this.tables['top-sources-dynamic']) {
            this.tables['top-sources-dynamic'].destroy();
        }

        this.tables['top-sources-dynamic'] = $('#table-top-sources-dynamic').DataTable({
            responsive: true,
            pageLength: 10,
            dom:'<"filter-row"<"col-md-6 col-sm-6 col-xs-12 search"f><"col-md-6 col-sm-6 col-xs-12 top"<"top"l>><"clearfix">>rt<"bottom halfBottom"pi<"clearfix">>',
            ajax: this.dashboardService.dataTableSource('laraRules/topSrcDynamic', 'table-top-sources-dynamic', 'policyId', this.policyFormService.model.displayPolicy[0].id, null, null),
            columns: [
                { 'data': 'no' },
                { 'data': 'ip' },
                { 'data': 'count' },
                { 'data': 'size' }
            ],
            createdRow: function (row: any, data: any, index: number) {
                let ip=$('td:nth-child(2)', row).html();
                $('td:nth-child(2)', row).html('<a target="_blank" href="https://ipadmin.ip.testing/ipadmin/index.html?ip='+ip+'">'+ip+'</a>');
            },
            drawCallback: function(){
                $('td.dataTables_empty').attr('colspan','100%');
            }
        });
    }

    // Top destinations dynamic
    loadTopDestinationsDynamic(): void {
        if(this.tables['top-destinations-dynamic']) {
            this.tables['top-destinations-dynamic'].destroy();
        }

        this.tables['top-destinations-dynamic'] = $('#table-top-destinations-dynamic').DataTable({
            responsive: true,
            pageLength: 10,
            dom:'<"filter-row"<"col-md-6 col-sm-6 col-xs-12 search"f><"col-md-6 col-sm-6 col-xs-12 top"<"top"l>><"clearfix">>rt<"bottom halfBottom"pi<"clearfix">>',
            ajax: this.dashboardService.dataTableSource('laraRules/topDstDynamic', 'table-top-destinations-dynamic', 'policyId', this.policyFormService.model.displayPolicy[0].id, null, null),
            columns: [
                { 'data': 'no' },
                { 'data': 'ip' },
                { 'data': 'count' },
                { 'data': 'size' }
            ],
            createdRow: function (row: any, data: any, index: number) {
                let ip=$('td:nth-child(2)', row).html();
                $('td:nth-child(2)', row).html('<a target="_blank" href="https://ipadmin.ip.testing/ipadmin/index.html?ip='+ip+'">'+ip+'</a>');
            },
            drawCallback: function(){
                $('td.dataTables_empty').attr('colspan','100%');
            }
        });
    }

    // Top sources static
    loadTopSourcesStatic(): void {
        if(this.tables['top-sources-static']) {
            this.tables['top-sources-static'].destroy();
        }

        this.tables['top-sources-static'] = $('#table-top-sources-static').DataTable({
            responsive: true,
            pageLength: 10,
            dom:'<"filter-row"<"col-md-6 col-sm-6 col-xs-12 search"f><"col-md-6 col-sm-6 col-xs-12 top"<"top"l>><"clearfix">>rt<"bottom halfBottom"pi<"clearfix">>',
            ajax: this.dashboardService.dataTableSource('laraRules/topSrcStatic', 'table-top-sources-static', 'policyId', this.policyFormService.model.displayPolicy[0].id, null, null),
            columns: [
                { 'data': 'no' },
                { 'data': 'ip' },
                { 'data': 'count'},
                { 'data': 'size' }
            ],
            createdRow: function (row: any, data: any, index: number) {
                let ip=$('td:nth-child(2)', row).html();
                $('td:nth-child(2)', row).html('<a target="_blank" href="https://ipadmin.ip.testing/ipadmin/index.html?ip='+ip+'">'+ip+'</a>');
            },
            drawCallback: function(){
                $('td.dataTables_empty').attr('colspan','100%');
            }
        });
    }

    // Top destinations static
    loadTopDestinationsStatic (): void {
        if(this.tables['top-destinations-static']) {
            this.tables['top-destinations-static'].destroy();
        }

        this.tables['top-destinations-static'] = $('#table-top-destinations-static').DataTable({
            responsive: true,
            pageLength: 10,
            dom:'<"filter-row"<"col-md-6 col-sm-6 col-xs-12 search"f><"col-md-6 col-sm-6 col-xs-12 top"<"top"l>><"clearfix">>rt<"bottom halfBottom"pi<"clearfix">>',
            ajax: this.dashboardService.dataTableSource('laraRules/topDstStatic', 'table-top-destinations-static', 'policyId', this.policyFormService.model.displayPolicy[0].id, null, null),
            columns: [
                { 'data': 'no' },
                { 'data': 'ip' },
                { 'data': 'count' },
                { 'data': 'size' }
            ],
            createdRow: function (row: any, data: any, index: number) {
                let ip=$('td:nth-child(2)', row).html();
                $('td:nth-child(2)', row).html('<a target="_blank" href="https://ipadmin.ip.testing/ipadmin/index.html?ip='+ip+'">'+ip+'</a>');
            },
            drawCallback: function(){
                $('td.dataTables_empty').attr('colspan','100%');
            }
        });
    }
}
