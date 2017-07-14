import { Injectable } from '@angular/core';
import { DashboardService } from '../../services/index';

declare var $: any;

@Injectable()
export class HostInfoService {

    private table: any;
    public counter: number;
    public ip: string;
    public isTableVisible: boolean = false;

    constructor(private dashboardService: DashboardService ) {}

    // datatables initialization
    loadDataTable(tableSelector: string, ip: string): void {
        this.ip = ip;
        let __this = this; // _this is reserved in TypeScript

        if (this.dashboardService.validateIPv4(this.ip)) {

            this.isTableVisible = true;

            setTimeout(() => {
                this.table = $('#' + tableSelector).DataTable({
                    responsive: true,
                    pageLength: 100,
                    dom: '<"dataTables_scrollBody"t>',
                    processing: false,
                    serverSide: false,
                    bSortClasses: false,
                    ajax: this.dashboardService.dataTableSource('cmdbReport', tableSelector, 'ipAddress', this.ip, null, null),
                    columns: [
                        { 'data': function(data: any){
                            return data.ip + '/' + data.maskCidr;
                        }},
                        { 'data': 'name' },
                        { 'data': 'city' },
                        { 'data': 'country' },
                        { 'data': 'source' }
                    ],
                    drawCallback: function(){
                        // Datatables fix
                        $('td.dataTables_empty').attr('colspan', '100%');

                        // Add number of results to Host reports's panel title
                        let count = this.api().page.info().recordsDisplay;
                        if (ip !== undefined) {
                            __this.updateCounter(count);
                        }
                    },
                    initComplete: function(){
                    }
                });
            }, 0);
        }else {
            // Mark table invisible - error message is visible
            this.isTableVisible = false;

            // Reinit scroll to search
            setTimeout( () => {
                this.dashboardService.initScrollToSearch();
            }, 0);

            // Update counter with no results (0)
            this.updateCounter(0);
        }
    }

    // Update counter in panel title
    updateCounter(count: number): void {
        // Update counter for search results
        if (this.dashboardService.validateIPv4(this.dashboardService.query) && this.dashboardService.lastSearch !== undefined) {
            this.counter = count;
            this.dashboardService.counts['hostInfo'] = count;
        }else {
            delete this.counter;
            delete this.dashboardService.counts['hostInfo']; // Update counter variable which is used in KITE title
        }
    }

    // Reloads data (datatables) for current module
    reload(tableSelector: string): void {
        if( this.table ){
            this.table.destroy();
        }
        this.loadDataTable(tableSelector, this.dashboardService.lastSearch);

    }
}
