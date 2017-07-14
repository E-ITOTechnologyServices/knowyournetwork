import { Component } from '@angular/core';

import { DashboardService } from '../../services/index';
import { HostInfoService } from './hostInfo.service';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'hostInfo',
    templateUrl: 'hostInfo.component.html'
})
export class HostInfoComponent {
    public isCollapsed = true;
    public tableSelector = 'cmdbReport-table';
    private supscription: any;

    constructor(private dashboardService: DashboardService, private hostInfoService: HostInfoService ) {
        // Listen for updates in newAction channel - this part is for search handling
        this.supscription = this.dashboardService.newAction.subscribe(
            (action: string) => {
                if (action === 'hostInfoSearch') {
                    this.hostInfoService.reload(this.tableSelector);
                }
            }
        );
    }

    ngOnInit(): void {
        this.hostInfoService.loadDataTable(this.tableSelector, this.dashboardService.lastSearch);
    }

    ngOnDestroy () {
        this.supscription.unsubscribe();
    }

    // hostInfo module initialization
    initModule(): void {
        // Load datatables
        this.hostInfoService.loadDataTable(this.tableSelector, this.dashboardService.lastSearch);

        // Append scroll to search when you click on "Search for IP"
        this.dashboardService.initScrollToSearch();
    }
}
