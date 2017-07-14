import { Injectable } from '@angular/core';
import { Cookie } from 'ng2-cookies/ng2-cookies';

import { AppConfig } from '../../app.config';
import { DashboardService } from '../../services/index';

declare var $: any;

@Injectable()
export class NewsTickerService {
    private style = 'type';
    private table: any;
    public counter: number;

    constructor(public dashboardService: DashboardService ) {}

    // newsTicker module initialization
    initModule(): void {
        let _this = this;

        // Load datatables
        this.loadDataTable(null);

        /*
         * Style changer
         */
        // Check if is cookie set

        if (Cookie.get('newsTickerStyle')) {
            this.style = Cookie.get('newsTickerStyle');
        }

        // Set initial style
        $('#style-' + this.style).addClass('active');

        // Add change function
        $('#newsTicker .change-style span').click(function(){
            // Set another class active
            $('#newsTicker .change-style span').removeClass();
            $(this).addClass('active');
            Cookie.set('newsTickerStyle', $(this).attr('data-style'));
            _this.style = $(this).attr('data-style');

            // Redraw table
            let oldPage = _this.table.page.info().page;
            _this.table.draw();
            _this.table.page(oldPage).draw( 'page' );
        });
    }

    // datatables initialization
    loadDataTable(query: string): void {
        let _this = this;

        this.table = $('#newsTicker-table').DataTable({
            'bAutoWidth': false,
            'bSort': false,
            'pageLength': AppConfig.newsTicker.loadAmount,
            'dom': 'r<"dataTables_scrollBody"t><"bottom"<"col-xs-6 col-sm-4 col-md-4"p><"col-xs-6 col-sm-4 col-md-4 load-more"><"col-xs-12 col-sm-4 col-md-4"l><"clearfix">>',
            'processing': false,
            'serverSide': false,
            'bSortClasses': false,
            'ajax': (query ? this.dashboardService.dataTableSource('newsTicker', null, 'size', AppConfig.newsTicker.size, 'query', query) : this.dashboardService.dataTableSource('newsTicker', null, 'size', AppConfig.newsTicker.size, '', '')),
            'columns': [
                { 'data': '@timestamp' },
                { 'data':
                    function(data: any){
                        let text: string = data.text;
                        if (data.host !== '0.0.0.0.') {
                            text = '<a href="#query=' + data.host + '">' + data.text + '</a>';
                        }
                        return text;
                    }
                },
                { 'data': 'host' }
            ],
            'rowCallback': function (row: any, data: any, index: any) {
                // Style by priority or type
                $(row).removeClass().addClass('c-' + AppConfig.newsTicker.colors[_this.style][data[_this.style]]);
            },
            'fnDrawCallback': function() {
                $('#newsTicker-table thead').remove();
                // Append number of results
                _this.dashboardService.query ? _this.counter = this.api().page.info().recordsDisplay : _this.counter = undefined;
            },
            'initComplete': function(){
                _this.search(); // Do search if input field is changed
                if (!_this.table.ajax.json().status) {
                    if (_this.table.ajax.json().content.recordsTotal > _this.table.ajax.json().content.recordsFiltered) {
                        $('.load-more').html('<button type="button" id="newsTicker-load-more">Load more <i class="fa fa-refresh"></i></button>');
                        // Load more data on button click
                        $('#newsTicker-load-more').click(function(e: any){
                            e.preventDefault();
                            AppConfig.newsTicker.size = AppConfig.newsTicker.size + AppConfig.newsTicker.loadAmount;
                            _this.reload();
                        });
                    }
                }
            }
        });

    }

    // Reloads data (datatables) for current module
    reload(): void {
        if (this.table) {
            this.table.destroy(); // it is necessary to destroy current instance
        }
        this.loadDataTable(this.dashboardService.query);
    }

    // Starts new search for datatables
    search(): void {
        this.table.search(this.dashboardService.query, 1, 0).draw();
    }
}
