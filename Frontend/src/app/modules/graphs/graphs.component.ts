import { Component } from '@angular/core';

import { DashboardService } from '../../services/index';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'graphs',
    templateUrl: 'graphs.component.html'
})
export class GraphsComponent {
    public isCollapsed = true;
    public isIP = false;

    constructor(private dashboardService: DashboardService) {}

    ngOnInit(): void {

        this.initModule();

        // Listen for updates in newAction channel - this part is for search handling
        this.dashboardService.newAction.subscribe(
            (action: string) => {
                if (action === 'graphsSearch') {
                    this.reload();
                }
            }
        );
    }

    // When is module loaded
    initModule(): void {
        this.setIP();
    }

    // Check search query and set IP variable
    setIP(): void {
        if (this.dashboardService.validateIPv4(this.dashboardService.lastSearch)) {
            // And now set Graphs visible
            this.isIP = true;
        }else {
            // When Graphs aren't visible message is shown
            this.isIP = false;
        }
    }

    // Reloads data when is search executed
    reload(): void {
        // Trigger resets reload
        this.dashboardService.sendNewAction('resetsReload');

        this.setIP();
    }
}
