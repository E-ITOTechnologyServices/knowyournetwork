import { Component, OnInit, ViewChild } from '@angular/core';
import { ModalDirective } from 'ng2-bootstrap/ng2-bootstrap';

import { DashboardService } from '../../../services/index';
import { HostInfoService } from "../../hostInfo/hostInfo.service";
import { PolicyFormService } from "../policyForm/policyForm.service";
import { AppConfig } from "../../../app.config";
declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'ruleset',
    templateUrl: 'ruleset.component.html'
})
export class RulesetComponent {

    @ViewChild('modalConversations') public modalConversations:ModalDirective;
    @ViewChild('modalHostInfo') public modalHostInfo:ModalDirective;

    private supscription: any;
    isCollapsed = true;
    visibility = false;
    tables = new Array();
    counter: number;
    modals: any = { conversations: {}, hostInfo: {} };
    tableSettings: any = { limitForMultiple: 5 };
    multipleCounter = 0;
    rules: string;
    errorMessage: string;
    isCopied: boolean = false;

    constructor(private dashboardService: DashboardService, private hostInfoService: HostInfoService, private policyFormService: PolicyFormService) {
        // Listen for updates in newAction channel
        this.supscription = this.dashboardService.newAction.subscribe(
                (action:string) => {
                    switch (action) {
                        case 'rulesetLiveSearch':
                        case 'rulesetSearch':
                            this.search();
                            break;

                        case 'displayPolicies':
                            this.displayPolicies();
                            break;
                    }
                }
            );
    }

    ngOnDestroy () {
        this.supscription.unsubscribe();
    }

    displayPolicies(): void {
        // Load Ruleset table
        this.loadRulesetTable();

        // Load list of ACL rules
        this.loadACLRules();

        // Add placeholder to search fields
        $(".dataTables_filter .form-control").each(function(){
            $(this).attr("placeholder","Search");
        });
    }

    loadRulesetTable(): void {
        let __this=this;

        // Destroy datatable if already exists
        if(this.tables['lara-ruleset']) {
            this.tables['lara-ruleset'].destroy();
        }

        // Initialize new table
        this.tables['lara-ruleset'] = $('#lara-ruleset-table').DataTable( {
            'bAutoWidth': false,
            'pageLength': 10,
            'dom':'<"filter-row"<"search"f><"extra"<"add-any"><"entries"l><"clearfix">><"clearfix">>r<"dataTables_scrollBody"t><"bottom"<"col-xs-12 col-sm-6 col-md-6"p><"col-xs-12 col-sm-6 col-md-6"i><"clearfix">>',
            'processing': false,
            'serverSide': false,
            'bSortClasses': false,
            'ajax': this.dashboardService.dataTableSource('laraRules', 'lara-ruleset-table', 'policyId', this.policyFormService.model.displayPolicy[0].id, null, null),
            'columns': [
                // Number
                { 'data': function(row: any, type: any, _set: any, meta: any){
                    return '<div><span>'+(meta.row+1)+'</div></span>';
                } },
                // Source
                { 'data': function(row: any){
                   return __this.srcAndDstColumn('source', row);
                }},
                // Destination
                { 'data': function(row: any){
                    return __this.srcAndDstColumn('destination', row);
                }},
                // Services
                { 'data': function(row: any){
                    let numOfServices=row.flow.service.members.length;

                    // Multiple services
                    if(numOfServices>1){
                        let label: string;

                        // Set label
                        if(row.flow.service.name){
                            label='<i class="service-icon service-group"></i>'+row.flow.service.name;
                        }else{
                            label='... Multiple services';
                        }

                        let html='';

                        // Append extra wrapper - dropdown menu
                        if(numOfServices>__this.tableSettings.limitForMultiple){
                            html+='<div class="dropdown-network services"><a class="collapsed" data-toggle="collapse" data-target="#multiple'+__this.multipleCounter+'"><span>'+label+'</span><i class="fa expandible"></i></a><div id="multiple'+__this.multipleCounter+'" class="dropdown-box"><span  class="arrow-up"></span><div class="check-scrollbar">';
                        }

                        html+='<ul class="multiple-services '+(numOfServices>__this.tableSettings.limitForMultiple?'':'view-n')+'">';
                        for(let item of row.flow.service.members){
                            if(item.protocol || item.port)
                                html+='<li><i class="service-icon '+item.protocol+'-service"></i> '+item.port+'</li>';
                        }
                        html+='</ul>';

                        // Finish wrapping
                        if(numOfServices>__this.tableSettings.limitForMultiple){
                            html+='</div></div></div>';
                        }

                        // For assigning unique ID to element
                        __this.multipleCounter++;

                        return html;

                    // Single service
                    }else{
                        let protocol=row.flow.service.members[0].protocol;
                        let port=row.flow.service.members[0].port;
                        return '<div class="single"><i class="service-icon '+protocol+'-service"></i>'+port+'</div>';
                    }
                }},
                // Action
                { 'data': function(row: any){
                    if(row.action=='permit'){
                        return '<i class="service-icon permit-icon"></i> permit';
                    }else{
                        return '<i class="service-icon drop-icon"></i> deny';
                    }
                  }},
                // Comment
                { 'data': function(row: any){
                    return row.hits+' Hits &nbsp; '+row.bytes;
                }}
            ],
            'columnDefs': [
                { className: "permit", "targets": [ 4 ] },
                { className: "no", "targets": [ 0 ] },
                { className: "services", targets: [3] }
            ],
            'createdRow': function(row: any, data: any, index: number){
                // Append "Options dropdown"
                $('td',row).find(".lara-options-dropdown").each(function(){
                    var ip=$(this).attr('data-ip');

                    // Host dropdown
                    $(this).append('<ul class="dropdown-menu">' +
                        '<li role="separator" class="label">Investigate</li>' +
                        '<li><a href="#" id="get-host-info"><i class="fa fa-map-marker"></i>  Get Host Info</a></li>' +
                        '<li><a href="#query='+ip+'"><i class="fa fa-search-plus"></i> Search for IP in kyn</a></li>' +
                        '<li><a href="#" id="show-24h-conversations"><i class="fa fa-exchange"></i> Get 24h Conversations</a></li>' +
                        '<li><a href="#" id="show-30d-conversations"><i class="fa fa-exchange"></i> Get 30d Conversations</a></li>' +
                        '</ul>');

                    // Set Conversations modals
                     $(this).find("#show-24h-conversations").click(function(e: any){
                        e.preventDefault();
                        __this.modals.conversations.title = ip;
                        __this.modals.conversations.source = AppConfig.appURL+'app/kibana#/visualize/edit/24h-Conversations?embed=true&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-24h,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:\'SourceAddress:%22'+ip+'%22%20OR%20DestinationAddress:%22'+ip+'%22\')),uiState:(),vis:(aggs:!((id:\'1\',params:(),schema:metric,type:count),(id:\'2\',params:(field:SourceAddress,customLabel:Source,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms),(id:\'3\',params:(field:DestinationAddress,customLabel:Destination,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms),(id:\'4\',params:(field:Destinationport,customLabel:port,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms)),listeners:(),params:(perPage:20,showMeticsAtAllLevels:!f,showPartialRows:!f),title:\'24h%20Conversations\',type:table))';
                        __this.showModalConversations();
                    });
                    $(this).find("#show-30d-conversations").click(function(e: any){
                        e.preventDefault();
                        __this.modals.conversations.title = ip;
                        __this.modals.conversations.source = AppConfig.appURL+'app/kibana#/visualize/edit/30d-Conversations?embed=true&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-30d,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:\'SourceAddress:%22'+ip+'%22%20OR%20DestinationAddress:%22'+ip+'%22\')),uiState:(),vis:(aggs:!((id:\'1\',params:(),schema:metric,type:count),(id:\'2\',params:(field:SourceAddress,customLabel:Source,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms),(id:\'3\',params:(field:DestinationAddress,customLabel:Destination,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms),(id:\'4\',params:(field:Destinationport,customLabel:port,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms)),listeners:(),params:(perPage:20,showMeticsAtAllLevels:!f,showPartialRows:!f),title:\'24h%20Conversations\',type:table))';
                        __this.showModalConversations();
                    });

                    // Set Host info modal
                    $(this).find("#get-host-info").click(function(e: any){
                        e.preventDefault();
                        __this.modals.hostInfo.title = ip;
                        __this.hostInfoService.loadDataTable('lara-cmdbReport-table', ip); // Load data table from Host Info module
                        __this.showModalHostInfo();
                    });

                    // Close LARA extra menus on collapse box click if they don't consist these dropdown
                    $(this).click(function(){
                        var dropdownTopPosition=$(this).index()*50;
                        $(this).parent().parent().scrollTop(dropdownTopPosition);

                        var parent=$(this).parent();
                        if( !parent.hasClass("multiple-destinations") ){
                            $(".dropdown-box").parent().find("a").addClass("collapsed");
                            $(".dropdown-box").removeClass("in");
                        }
                    });
                });

                // Append smaller menu (for static servers)
                $('td',row).find(".lara-smaller-options-dropdown").each(function(){
                    let ip=$(this).attr('data-ip');
                    ip=ip.split('/');
                    ip=ip[0];
                    $(this).append('<ul class="dropdown-menu">' +
                        '<li role="separator" class="label">Investigate</li>' +
                        '<li><a href="#" data-toggle="modal" data-target="#lara-get-host-info" data-ip="'+ip+'"><i class="fa fa-map-marker"></i>  Get Host Info</a></li>' +
                        '<li><a href="#query='+ip+'"><i class="fa fa-search-plus"></i> Search for IP in kyn</a></li>' +
                        '<li><a href="#" data-toggle="modal" data-target="#lara-conversations" data-ip="'+ip+'" data-title="24h Conversations" data-source="'+AppConfig.appURL+'app/kibana#/visualize/edit/24h-Conversations?embed=true&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-24h,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:\'SourceAddress:%22'+ip+'%22%20OR%20DestinationAddress:%22'+ip+'%22\')),uiState:(),vis:(aggs:!((id:\'1\',params:(),schema:metric,type:count),(id:\'2\',params:(field:SourceAddress,customLabel:Source,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms),(id:\'3\',params:(field:DestinationAddress,customLabel:Destination,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms),(id:\'4\',params:(field:Destinationport,customLabel:port,order:desc,orderBy:\'1\',size:2000),schema:bucket,type:terms)),listeners:(),params:(perPage:20,showMeticsAtAllLevels:!f,showPartialRows:!f),title:\'24h%20Conversations\',type:table))"><i class="fa fa-exchange"></i> Get 24h Conversations</a></li>' +
                        '</ul>');
                });

                // Add perfect scrollbar where are lists to big
                $('td', row).find(".check-scrollbar").each(function(){
                    if($(this).find('ul.multiple-destinations>li, ul.multiple-services>li').length>6){
                        $(this).addClass("perfect-scrollbar");
                    }
                });

                // Update scrollbar - this show scrollbar when is box loaded - instead just on hover
                $('td.multiple',row).find(".dropdown-network>a").click(function(){
                    // Wait until scrollbar is shown
                    setTimeout(function() {
                        $(".perfect-scrollbar").perfectScrollbar('update');
                    }, 300);
                });
            },
            'drawCallback': function(){
                // Datatables fix
                $('td.dataTables_empty').attr('colspan','100%');

                // Update counter with new status
                var count = this.api().page.info().recordsDisplay;
                __this.updateCounter(count);
            },
            'initComplete': function(){
                // Do initial search
                __this.tables['lara-ruleset'].search(__this.dashboardService.query,1,0).draw();

                // Append "Add ANY" option to datatables template
                $(".add-any").append('<label><input type="checkbox" id="includeOptionAny"> Add ANY to search </label>');

                // Load perfect scrollbar
                $('.perfect-scrollbar').perfectScrollbar();

                // Include option ANY to search
                $("#includeOptionAny").change(function(){
                    let search='';
                    __this.dashboardService.settings['includeOptionAny']=$(this).is(":checked");

                    $("#lara-ruleset-table_filter input").val()?search=$("#lara-ruleset-table_filter input").val():search=(__this.dashboardService.query?__this.dashboardService.query:'');

                    if(__this.dashboardService.settings['includeOptionAny']){
                        search+='|ANY';
                    }else{
                        search=search.replace('|ANY','');
                    }

                    __this.tables['lara-ruleset'].search(search,1,0).draw();
                });

                // Call complete action
                __this.visibility = true;
            }
        } );
    }

    // For display columns Source and Destination
    srcAndDstColumn(type: string, row: any): string{
        let html='';
        let numOfItems=row.flow[type].members.length;

        // Multiple sources
        if(numOfItems>1){

            // Append extra wrapper - dropdown menu
            if(numOfItems>this.tableSettings.limitForMultiple){
                html+='<div class="dropdown-network"><a class="collapsed" data-toggle="collapse" data-target="#multiple'+this.multipleCounter+'"><span>... Multiple sources</span><i class="fa expandible"></i></a><div id="multiple'+this.multipleCounter+'" class="dropdown-box"><span  class="arrow-up"></span><div class="check-scrollbar">';
            }

            html+='<ul class="multiple-destinations '+(numOfItems>this.tableSettings.limitForMultiple?'':'view-n')+'" >';

            for(let item of row.flow[type].members){
                html+='<li class="dropdown-options lara-options-dropdown lara-'+item.address_type+'-dropdown" data-type="destination" data-ip="'+item.ip_address+'"><a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"> <i class="service-icon '+item.address_type+'-icon-light"></i> <span>'+item.ip_address+'</span><i class="fa fa-chevron-down"></i></a></li>';
            }
            html+='</ul>';

            // Finish wrapping
            if(numOfItems>this.tableSettings.limitForMultiple){
                html+='</div></div></div>';
            }

            // Single source
        }else{
            // Development
            let addressType=row.flow[type].members[0].address_type;
            let address=row.flow[type].members[0].ip_address;

            // Label for 0.0.0.0 is ANY
            if(address=='0.0.0.0'){
                html+='<i class="service-icon any-icon"></i>ANY';
            }else{
                html+='<div class="dropdown-options lara-options-dropdown lara-'+addressType+'-dropdown" data-type="source" data-ip="'+address+'"><a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"> <i class="service-icon '+addressType+'-icon-light"></i> <span>'+address+'</span> <i class="fa fa-chevron-down"></i></a></div>';
            }
        }

        // For assigning unique ID to element
        this.multipleCounter++;

        return html;
    }

    // Starts new search for ruleset datatable
    search(): void {
        if(this.visibility) {
            this.tables['lara-ruleset'].search(this.dashboardService.query+(this.dashboardService.settings['includeOptionAny']?'|ANY':''), 1, 0).draw();
        }
    }

    // Show and Hide Coversations Modal
    showModalConversations(): void {
        this.modalConversations.show();
    }

    hideModalConversations(): void {
        this.modalConversations.hide();
    }

    // Show and Hide Host Info Modal
    showModalHostInfo(): void {
        this.modalHostInfo.show();
    }

    hideModalHostInfo(): void {
        this.modalHostInfo.hide();
    }

    // Updates LARA Ruleset counter on search or liveSearch action
    updateCounter(count: number){
        if(this.dashboardService.query){
            this.counter=count;
            this.dashboardService.counts['ruleset']=count;
        }else{
            delete this.counter;
            delete this.dashboardService.counts['ruleset'];
        }
    }

    // Loads and shows ACL rules
    loadACLRules(): void {
        // Get data
        this.dashboardService.httpPOST(this.dashboardService.dataSource('laraCisco'), { policyId: this.policyFormService.model.displayPolicy[0].id })
            .map((res:Response) => res.json())
            .subscribe(
                data => {
                    if(!data.status){
                        this.rules=data.content.data[0].acl;
                    }else{
                        this.errorMessage=data.errorMessage;
                    }
                },
                err => { this.errorMessage = '[Dashboard] Error loading file.' }
            );
    }
}