import { Component } from '@angular/core';
import { Validators, FormGroup, FormArray, FormBuilder } from '@angular/forms';

import { DashboardService } from '../../services/index';

@Component({
    moduleId: module.id,
    templateUrl: 'dashboard.component.html'
})

export class DashboardComponent {
    isKiteCollapsed = true;
    isVisualizationCollapsed = true;
    currentUser: any;

    constructor(public dashboardService: DashboardService){
        this.currentUser = JSON.parse(localStorage.getItem('currentUser'));
    }

    // Sum and return total number of search records in KITE subpanels
    outputKITECounts(): number {
        let sum=0;
        let hostReports=this.dashboardService.counts['hostReports'];
        let hostInfo=this.dashboardService.counts['hostInfo'];
        let syslog=this.dashboardService.counts['syslog'];

        if(hostReports) sum+=hostReports;
        if(hostInfo) sum+=hostInfo;
        if(syslog) sum+=syslog;

        if(this.dashboardService.lastSearch) return sum;
    }
}
