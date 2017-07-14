import { Component } from '@angular/core';

import { DashboardService } from '../../services/index';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'syslog',
    templateUrl: 'syslog.component.html'
})

export class SyslogComponent {
    private pageLength = 10;
    private tables = new Array();
    private supscription: any;
    public isCollapsed = true;
    private supscription: any;
    public tableSettings: any;
    public counter: number;

    constructor(private dashboardService: DashboardService ) {

        // Define all syslog tables and their fields
        this.tableSettings = {
            syslogIPS: {
                title: 'IPS logs',
                fields: [
                    {'data': '@timestamp'},
                    {'data': 'Source'},
                    {'data': 'Destination'},
                    {'data': 'Destinationport'},
                    {'data': 'log'},
                    {'data': 'IPS_System'}
                ]
            },
            syslogDHCP: {
                title: 'DHCP logs',
                fields: [
                    {'data': '@timestamp'},
                    {'data': 'message'},
                    {'data': 'AD-Server'}
                ]
            },
            syslogASA: {
                title: 'ASA logs',
                fields: [
                    {'data': '@timestamp'},
                    {'data': 'message'},
                    {'data': 'asa_fw'}
                ]
            },
            syslogRouter: {
                title: 'Router logs',
                fields: [
                    {'data': '@timestamp'},
                    {'data': 'cisco_message'},
                    {'data': 'router'}
                ]
            },
            syslogVoice: {
                title: 'Voice logs',
                fields: [
                    {'data': '@timestamp'},
                    {'data': 'shortmessage'},
                    {'data': 'voicegw'}
                ]
            },
            syslogProxy: {
                title: 'Proxy logs',
                fields: [
                    {'data': '@timestamp'},
                    {'data': 'c_ip'},
                    {'data': 'destinationIP'},
                    {'data': 'cs_uri_port'},
                    {'data': 'cs_host'},
                    {'data': 'cs_uri_path'}
                ]
            }
        };

        // Listen for updates in newAction channel - this part is for search handling
        this.supscription = this.dashboardService.newAction.subscribe(
            (action: string) => {
                if (action === 'syslogSearch') {
                    this.reload();
                }
            }
        );

    }

    ngOnInit(): void {
        this.initModule();
    }

    ngOnDestroy () {
        this.supscription.unsubscribe();
    }

    // Syslog module initialization
    initModule(): void {
        // Init all datatables
        this.loadDataTables(this.dashboardService.lastSearch, false);

        // Add perfect scrollbar wrapper to .nav-tabs
        $('#syslog .nav-tabs').wrap('<div class="nav-tabs-wrapper"></div>');
        $('#syslog .nav-tabs-wrapper').perfectScrollbar();
    }

    // Executes methods for loading each defined datatable
    loadDataTables(query: string, withDestroy: boolean) {
        // Set counter to 0 for ever new load when is query entered
        if (query) {
            this.counter = 0;
        }

        // Go through all defined properties/tables
        for (let table in this.tableSettings) {
            if (this.tableSettings.hasOwnProperty(table)) {
                // Destroy old table if table exists and that is needed (in reload case)
                if (this.tables[table] && withDestroy) {
                    this.tables[table].destroy();
                }

                // Heading HTML is stored to new variable because attribute title is used for creating new heading with number
                this.tableSettings[table].heading = this.tableSettings[table].title;

                // Load it
                this.loadDataTable(table, query, this.tableSettings[table].fields);
            }
        }
    }

    // Generic method for loading data in syslog tables
    loadDataTable(identifier: string, query: string, fields: any): void {
        let __this = this;

        this.tables[identifier] = $('#' + identifier + '-table').DataTable({
            responsive: true,
            pageLength: this.pageLength,
            dom: '<"dataTables_scrollBody"t><"bottom"<"col-xs-12 col-sm-6 col-md-6"p><"col-xs-12 col-sm-6 col-md-6"i><"clearfix">>',
            processing: false,
            serverSide: false,
            bSortClasses: false,
            ajax: (query ? this.dashboardService.dataTableSource(identifier, null, 'query', query, null, null) : this.dashboardService.dataTableSource(identifier, null, null, null, null, null)),
            columns: fields,
            columnDefs: [
                {targets: 'no-sort', orderable: false}
            ],
            initComplete: function () {
                $('td.dataTables_empty').attr('colspan', '100%');
                // Get number of results
                let count = this.api().page.info().recordsDisplay;

                // Update table counter
                __this.tableSettings[identifier].heading = __this.tableSettings[identifier].title + ' (' + count + ')';

                // Update Syslog and KITE counters
                __this.updateCounter(count);

                // Update widths for perfect scrollbar wrapper - hard coded identifier for case when is last table loaded
                if (identifier === 'syslogProxy') {
                    setTimeout(() => {
                        __this.reviewTabsWidths();
                    }, 0);
                }
            }
        });
    }

    // Updates each Syslog counter and KITE counter
    updateCounter(count: number) {
        if (this.dashboardService.query && this.dashboardService.lastSearch !== undefined) {
            // Update Syslog (modul) counter
            this.counter += count;

            // Update KITE counter
            this.dashboardService.counts['syslog'] = this.counter;
        }else {
            delete this.counter;
            delete this.dashboardService.counts['syslog'];
        }
    }

    // Update nav tabs list width when all counts are loaded
    reviewTabsWidths(): void {
        let finalWidth = 0;
        $('#syslog .nav-tabs li').each(function(){
            finalWidth += $(this).width();
        });
        $('#syslog .nav-tabs').width(finalWidth + 2);

        // Update scrollbar width
        $('#syslog .nav-tabs-wrapper').perfectScrollbar('update');
    }

    // Reloads data (datatables) for current module
    reload(): void {
        // Load tables with new query
        this.loadDataTables(this.dashboardService.lastSearch, true);
    }
}
