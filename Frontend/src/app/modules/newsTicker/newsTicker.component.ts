import { Component } from '@angular/core';
import { DashboardService } from '../../services/index';
import { NewsTickerService } from './newsTicker.service';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'newsTicker',
    templateUrl: 'newsTicker.component.html'
})
export class NewsTickerComponent {
    public isCollapsed = true;

    constructor(private dashboardService: DashboardService, private newsTickerService: NewsTickerService) { }

    ngOnInit(): void {
        this.newsTickerService.initModule();
    }
}
