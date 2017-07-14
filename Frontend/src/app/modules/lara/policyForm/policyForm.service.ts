import { Component, OnInit } from '@angular/core';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import {DashboardService} from "../../../services/index";

declare var $: any;

@Injectable()
export class PolicyFormService {
    model: any = {
        displayPolicy: null, // Current Policy
        displayRouter: null, // Active Router
        routers: null,
        policies: null,
        generateDateTimeFrom: new Date()
    }

    routers: Array<string>

    constructor( private dashboardService: DashboardService) {
        this.getRouters();
    }

    // Load routers from REST
    private getRouters(): void {
        this.dashboardService.httpPOST(this.dashboardService.dataSource('lara/routers'), {})
            .map((res:Response) => res.json())
            .subscribe(
                data => { this.model.routers = data.content.data },
                err => {  }
            );
    }

    private getPoliciesForRouter(): void {
        // Reset policies before are new policies loaded
        this.model.policies = null;
        this.model.displayPolicy = null;

        // Get policies only if is router selected
        if(this.model.displayRouter.length){
            // Get policy list for specific router
            this.dashboardService.httpPOST(this.dashboardService.dataSource('lara/policyListForRouter'), { routerIpAddress: this.model.displayRouter[0].id})
                .map((res:Response) => res.json())
                .subscribe(
                    data => {
                        this.model.policies = data.content.data;
                    },
                    err => {  }
                );
        }
    }
}