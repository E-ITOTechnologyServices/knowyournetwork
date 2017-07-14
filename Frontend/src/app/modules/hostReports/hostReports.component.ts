import { Component } from '@angular/core';

import { DashboardService } from '../../services/index';
import { AppConfig } from '../../app.config';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'hostReports',
    templateUrl: 'hostReports.component.html'
})
export class HostReportsComponent {
    public isCollapsed = true;
    private tableSelector: string = 'hostReport-table';
    private supscription: any;
    public tabs: any[] = [];
    public hostsTab = { active: true };
    private table: any;
    private tables: any[] = [];
    private loadedTabs: any[] = [];
    private deleteTabByID: any[] = [];
    public counter: number;

    constructor(private dashboardService: DashboardService ) {
        // Listen for updates in newAction channel - this part is for search handling
        this.supscription = this.dashboardService.newAction.subscribe(
            (action: string) => {
                if (action === 'hostReportsSearch') {
                    this.reload();
                }else if (action === 'hostReportsLiveSearch') {
                    this.search();
                }
            }
        );
    }

    ngOnInit(): void {
        this.initModule(); // Start with loading data
    }

    ngOnDestroy() {
        this.supscription.unsubscribe();
    }

    // Host Reports module initialization
    initModule(): void {
        // Add wrapper to nav-tabs list and initialize perfect scrollbar
        $('#host-reports .nav-tabs').wrap('<div class="nav-tabs-wrapper"></div>');
        $('#host-reports .nav-tabs-wrapper').perfectScrollbar();

        // Load datatables
        this.loadHostsTable(this.dashboardService.lastSearch);
    }

    // Datatables initialization
    loadHostsTable(query: string): void { // method will be called just in case of reloading (which is triggered by search)
        let __this = this;

        this.table = $(document).find('#' + this.tableSelector).DataTable({
            responsive: true,
            pageLength: 10,
            dom: '<"filter-row"<"search"f><"extra"<"entries"l><"clearfix">><"clearfix">>r<"dataTables_scrollBody"t><"bottom"<"col-xs-12 col-sm-6 col-md-6"p><"col-xs-12 col-sm-6 col-md-6"i><"clearfix">>',
            processing: false,
            serverSide: false,
            bSortClasses: false,
            ajax: (query ? this.dashboardService.dataTableSource('hostReport', null, 'ipAddress', query, null, null) : this.dashboardService.dataTableSource('hostReport', null, null, null, null, null)),
            columns: [
                { 'data': 'host' },
                { 'data': 'severity' },
                { 'data': 'events' },
                { 'data': 'firstDetection' },
                { 'data': 'lastDetection' },
            ],
            order: [[ 1, 'desc' ]],
            createdRow: function (row: any, data: any, index: any) {
                // Add DOM identifier
                $(row).attr('data-id', 'host-' + index);
                $(row).attr('data-ip', data.host);
                // Get ID and IP properties
                $(row).click(function () {
                    let id = $(this).attr('data-id');
                    let ip = $(this).attr('data-ip');

                    __this.viewHostDetails(id, ip);
                });
            },
            initComplete: function(){
                // Set Init search value
                if (__this.dashboardService.query) {
                    __this.search();
                }

                // Add number of results to Host reports's panel title
                let count = this.api().page.info().recordsDisplay;
                __this.updateCounter(count);
            }
        });
    }

    viewHostDetails(id: any, ip: string): void {

        // If delete list has new items do delete action
        if (this.deleteTabByID.length > 0) {

            // First go through all items
            for (let tabID of this.deleteTabByID) {

                // Find out tab index by tab ID
                let index = this.loadedTabs[tabID];

                // Delete tab from list
                this.tabs.splice(index, 1);

                // Updated new indexes - because index is delete if some item has bigger index that needs to be reduced for 1
                let item: any;
                for (item in this.loadedTabs) {
                    if (this.loadedTabs.hasOwnProperty(item)) {
                        if (this.loadedTabs[item] > index) {
                            this.loadedTabs[item]-- ;
                        }
                    }
                }

                // Delete mark in loadedTabs
                delete this.loadedTabs[item];
            }
            // Only when is loop finished, empty delete list
            this.deleteTabByID = [];
        }

        let i: number;

        // Create new tab if it already is not exist
        if (this.loadedTabs[id] === undefined) {

            this.tabs.push({ title: ip, id: id, removable: true });

            // Save ID and order for loaded tabs
            i = this.tabs.length - 1;
            this.loadedTabs[id] = i;

            // Wait until tab is loaded
            setTimeout(() => {
                    // Load data in new table
                    this.tables[id] = $('#' + id + '-table').DataTable({
                        responsive: true,
                        pageLength: 100,
                        dom: '<"dataTables_scrollBody"t>',
                        processing: false,
                        serverSide: false,
                        bSortClasses: false,
                        ajax: this.dashboardService.dataTableSource('hostDetailsReport', id + '-table', 'ipAddress', ip, null, null),
                        columns: [
                            { 'data': 'eventType' },
                            { 'data': 'number' },
                            { 'data': 'firstDetection' },
                            { 'data': 'lastDetection' },
                            { 'data': function(data: any){
                                let result = data.description;
                                if (data.link) {
                                    // Parse date
                                    let date1 = new Date(Date.parse(data.firstDetection + 'Z'));
                                    let date2 = new Date(Date.parse(data.lastDetection + 'Z'));

                                    // Set offset
                                    if (data.dateOffset) {
                                        date1.setMinutes(date1.getMinutes() - parseInt(data.dateOffset, 0));
                                        date2.setMinutes(date2.getMinutes() + parseInt(data.dateOffset, 0));
                                    }

                                    let link = AppConfig.appURL + data.link;
                                    link = link.replace(/\[%IP%\]/g, ip);
                                    link = link.replace(/\[%DATE1%\]/g, date1.toISOString());
                                    link = link.replace(/\[%DATE2%\]/g, date2.toISOString());
                                    result = '<a href="' + link + '" target="_blank">' + result + '</a>';
                                }
                                return result;
                            } }
                        ]});

                    // Update widths because of perfect scrollbar
                    this.reviewTabsWidths();
                },
                100);
        }else {
            // Get index of needed tab
            i = this.loadedTabs[id];
        }

        // Set it active in any case - when is first loaded or selected again
        this.tabs[i].active = true;
    }

    // Actions to do before is tab removed
    removeTabHandler(tab: any): void {
        // Destroy table
        this.tables[tab.id].destroy(); // it is necessary to destroy current instance

        // Remove loaded table
        delete this.tables[tab.id];

        // Push tab ID to delete list - delete action is executed on every new click
        this.deleteTabByID.push(tab.id);

        // Update width again (wait for 200 ms in case when is tab removed)
        setTimeout(() => {
            this.reviewTabsWidths();
        },
        100);
    }

    // Update counter in panel title
    updateCounter(count: number): void {
        if (this.dashboardService.query && this.dashboardService.lastSearch !== undefined) {
            this.counter = count;
            this.dashboardService.counts['hostReports'] = count;
        }else {
            delete this.counter;
            delete this.dashboardService.counts['hostReports']; // Update counter variable which is used in KITE title
        }
    }

    // Update nav tabs list with new width when is new tab added or removed
    reviewTabsWidths(): void {
        let finalWidth = 0;
        $('#host-reports .nav-tabs li').each(function(){
            finalWidth += $(this).width();
        });
        $('#host-reports .nav-tabs').width(finalWidth + 2);

        // Update scrollbar width
        $('#host-reports .nav-tabs-wrapper').perfectScrollbar('update');
    }

    // Reloads data (datatables) for current module
    reload(): void {
        if( this.table ){
            this.table.destroy();
        }
        this.loadHostsTable(this.dashboardService.lastSearch);
    }

    // Starts new search for datatables
    search(): void {
        this.table.search(this.dashboardService.query, 1, 0).draw();
    }
}
