import { Component } from '@angular/core';

import { DashboardService } from '../../services/index';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'lara',
    templateUrl: 'lara.component.html'
})
export class LaraComponent {

    isCollapsed = true;
    isOverviewCollapsed = true;
    isPolicyBuilderCollapsed = true;
    counter: number;

    constructor(private dashboardService: DashboardService) {}

    // Returns Ruleset output number
    outputCounts(): number {
        let ruleset = this.dashboardService.counts['ruleset'];

        if (ruleset !== undefined) {
            return ruleset;
        }
    }
}

