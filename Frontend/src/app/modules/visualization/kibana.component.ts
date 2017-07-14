import { Component } from '@angular/core';

import { DashboardService } from '../../services/index';
import { AppConfig } from '../../app.config';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'kibana',
    templateUrl: 'kibana.component.html'
})
export class KibanaComponent {
    isCollapsed = true;
    isKibanaVisible = false;
    ip: string;
    url: string;
    iHeight: number;

    constructor(private dashboardService: DashboardService) {}

    ngOnInit(): void {
        this.initModule();

        // Listen for updates in newAction channel - this part is for search handling
        this.dashboardService.newAction.subscribe(
            (action: string) => {
                if (action === 'kibanaSearch') {
                    this.reload();
                }
            }
        );
    }

    initModule(): void {
        // Load Kibana - in case when is search triggered before is module loaded
        this.loadKibana();

        // Append scroll to search when you click on "Search for IP"
        this.dashboardService.initScrollToSearch();
    }

    reload(): void {
        // iFrame needs to be toggled in order to update src
        this.isKibanaVisible = false;

        // And this timeout is necessary
        setTimeout(() => {
            this.loadKibana();
        }, 0);
    }

    loadKibana(): void {
        if (this.dashboardService.validateIPv4(this.dashboardService.lastSearch)) {
            this.ip = this.dashboardService.lastSearch;

            // Define exact query
            let query = '%27SourceAddress:%22' + this.ip + '%22%20OR%20DestinationAddress:%22' + this.ip + '%22%27';

            // Dashboard version depends on window resolution
            if ( $(document).width() > 991 ) {
                this.url = AppConfig.appURL + 'app/kibana#/dashboard/DesktopDashboard?embed=true&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-1y,mode:quick,to:now))&_a=(filters:!(),options:(darkTheme:!f),panels:!((col:1,id:Source-destination-view,panelIndex:1,row:1,size_x:6,size_y:5,type:visualization),(col:7,id:%2724h-Conversations%27,panelIndex:2,row:1,size_x:6,size_y:5,type:visualization)),query:(query_string:(analyze_wildcard:!t,query:' + query + ')),title:DesktopDashboard,uiState:(P-2:(vis:(params:(sort:(columnIndex:!n,direction:!n))))))';
                this.iHeight = 510;
            }else {
                this.url = AppConfig.appURL + 'app/kibana#/dashboard/MobileDashboard?embed=true&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-1y,mode:quick,to:now))&_a=(filters:!(),options:(darkTheme:!f),panels:!((col:1,id:Source-destination-view,panelIndex:1,row:1,size_x:12,size_y:5,type:visualization),(col:1,id:%2724h-Conversations%27,panelIndex:2,row:6,size_x:12,size_y:5,type:visualization)),query:(query_string:(analyze_wildcard:!t,query:' + query + ')),title:MobileDashboard,uiState:(P-1:(spy:(mode:(fill:!f,name:!n)),vis:(legendOpen:!f)),P-2:(spy:(mode:(fill:!f,name:table)),vis:(params:(sort:(columnIndex:!n,direction:!n))))))';
                this.iHeight = 1010;
            }

            // And now set Kibana visible
            this.isKibanaVisible = true;
        }else {
            delete this.ip;

            // When Kibana isn't visible message is shown
            this.isKibanaVisible = false;
        }
    }
}
