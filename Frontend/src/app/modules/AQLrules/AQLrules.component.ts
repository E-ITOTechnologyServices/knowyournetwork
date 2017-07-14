import { Component, ViewChild } from '@angular/core';
import { ModalDirective } from 'ng2-bootstrap/ng2-bootstrap';

import { DashboardService } from '../../services/index';

declare var $: any;
declare  var moment: any;

@Component({
    moduleId: module.id,
    selector: 'AQLrules',
    templateUrl: 'AQLrules.component.html'
})
export class AQLrulesComponent {

    @ViewChild('modalRule') public modalRule:ModalDirective;

    // Private
    private table: any;

    // Public
    isCollapsed = true;
    modals: any = { rule: {} }; // Content which is displayed
    model: any = { }; // Data which will be changed

    constructor(private dashboardService: DashboardService) {}

    // Init method
    ngOnInit(): void {

        // Listen for updates in newAction channel - this part is for search handling
        this.dashboardService.newAction.subscribe(
            (action: string) => {
                if (action === 'AQLrulesLiveSearch') {
                    this.search();
                }
            }
        );

        // Load datatables
        this.loadDataTable();
    }

    // Shows modal and form for new rule
    showAddRule(): void {
        this.modals.rule.action = 'add';
        this.modals.rule.title = 'Add new rule';
        this.modals.rule.showForm = true;

        this.resetFormSettings();

        this.model = {};

        this.modalRule.show();
    }

    // Shows modal and form for edit
    showEditRule(ruleID: number, rule: string, description: string): void {
        this.modals.rule.action = 'edit';
        this.modals.rule.title = 'Edit rule';
        this.modals.rule.showForm = true;

        this.resetFormSettings();

        // Set existing data
        this.model.id = ruleID;
        this.model.rule = rule;
        this.model.description = description;

        this.modalRule.show();
    }

    // Shows modal for delete
    showDeleteRule(ruleID: number){
        this.modals.rule.action = 'delete';
        this.modals.rule.title = 'Delete rule';
        this.modals.rule.body = 'Are you sure you want to delete rule with ID ' + ruleID + '?';
        this.modals.rule.showForm = false;

        this.resetFormSettings();

        this.model.id = ruleID;

        this.modalRule.show();
    }

    // Executes adding
    onRuleFormSubmit(form: any): void {
        this.modals.rule.submitted = true;
        if( this.isRuleFormValid(form) ){
            this.modalRule.hide();
            $('#overlay').show();

            // Execute action on MW
            this.dashboardService.httpPOST(this.dashboardService.dataSource('aql/rules/' + this.modals.rule.action), this.model)// Possible actions: add, edit and delete
                .map((res:Response) => res.json())
                .subscribe(
                    data => {
                        if( data.status == 0 ){
                            this.table.ajax.reload(null, false);
                            $('#overlay').hide();
                        }else{
                            this.showError(this.modals.rule.action, data.errorMessage);
                        }
                    },
                    err => {
                        this.showError(this.modals.rule.action, err);
                    }
                );
        }
    }

    showError(action, errorMessage): void {
        this.modals.rule.title = 'AQL rule editor status';
        this.modals.rule.body = '<span class="error">Error in ' + action + ' action:<br>' + errorMessage + ' </span>';
        this.modals.rule.showForm = false;
        this.modals.rule.showError = true;

        $('#overlay').hide();
        this.modalRule.show();
    }

    isRuleFormValid(form): boolean {
        if( this.modals.rule.submitted ){
            if( form.valid ){
                return true;
            }else{
                // Field Rule
                if( !this.model.rule ) {
                    this.modals.rule.ruleError = true;
                }else{
                    this.modals.rule.ruleError = false;
                }

                // Field Description
                if( !this.model.description ) {
                    this.modals.rule.descriptionError = true;
                }else{
                    this.modals.rule.descriptionError = false;
                }
                return false;
            }
        }
    }

    resetFormSettings(): void {
        // Reset mark for submitted
        this.modals.rule.submitted = false;

        // Remove mark for displaying errors
        this.modals.rule.showError = false;

        // Reset validation settings
        this.modals.rule.ruleError = false;
        this.modals.rule.descriptionError = false;
    }

    loadDataTable(): void {
        let _this = this;

        this.table = $('#AQLrules-table').DataTable({
            bAutoWidth: false,
            bSort: false,
            dom: '<"filter-row"<"search"f><"extra"<"entries"l><"clearfix">><"clearfix">>r<"dataTables_scrollBody"t><"bottom"<"col-xs-12 col-sm-6 col-md-6"p><"col-xs-12 col-sm-6 col-md-6"i><"clearfix">>',
            ajax: this.dashboardService.dataTableSource('aql/rules/list', null, null, null, null, null),
            columns: [
                { 'data': 'id' },
                { 'data': 'rule' },
                { 'data': 'description' },
                { 'data': function(data: any){
                    return '<button data-id="' + data.id + '" data-action="edit" class="btn-rule-action btn-rule-edit"><i class="fa fa-pencil-square-o" aria-hidden="true"></i></button><button data-id="' + data.id + '" data-action="delete" class="btn-rule-action btn-rule-delete"><i class="fa fa-remove" aria-hidden="true"></i></button>';
                }}
            ],
            initComplete: function(){
                // Set Init search value
                if (_this.dashboardService.query) {
                    _this.search();
                }
            },
            drawCallback: function(data){
                // Adds listeners for Edit and Delete buttons
                $('.btn-rule-action').click(function(){
                    let ruleID =  $(this).attr('data-id');
                    switch ( $(this).attr('data-action') ){
                        case "edit":
                            // Find out rule and description from table
                            let rule = $(this).parent('td').parent('tr').find('td:nth-child(2)').text();
                            let description = $(this).parent('td').parent('tr').find('td:nth-child(3)').text();

                            _this.showEditRule(ruleID, rule, description);
                            break;

                        case "delete":
                            _this.showDeleteRule(ruleID);
                            break;
                    }
                });
            }
        });
    }

    // Starts new search for current datatables
    search(): void {
        this.table.search(this.dashboardService.query, 1, 0).draw();
    }

    // Delete AQL rule with provided ID
    deleteRule(ruleID): void {
        console.log(ruleID);
    }
}