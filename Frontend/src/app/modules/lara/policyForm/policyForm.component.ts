import { Component, OnInit } from '@angular/core';

import { DashboardService } from '../../../services/index';
import { PolicyFormService } from "./policyForm.service";

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'policyForm',
    templateUrl: 'policyForm.component.html'
})
export class PolicyFormComponent {
    now = new Date();
    isCollapsed = true;

    constructor( private dashboardService: DashboardService, private policyFormService: PolicyFormService ) {}

    ngOnInit(): void {  }

    onDisplayFormSubmit(form: any) {
        if(form.valid){
            this.dashboardService.sendNewAction('displayPolicies');
        }
    }

    onGenerateFormSubmit(form: any) {
        if(form.valid){
        }
    }

    // Min date for search field from - needs to be one day before
    minDateForTo(): Date {
        if(this.policyFormService.model.generateDateTimeFrom){
            let minDate=this.policyFormService.model.generateDateTimeFrom;
            return minDate;
        }
        return null;
    }

}
