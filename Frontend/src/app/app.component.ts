import { Component, OnInit } from '@angular/core';

declare var $: any;

@Component({
    moduleId: module.id,
    selector: 'kyn-app',
    template: `<router-outlet></router-outlet>`
    //templateUrl: 'app.component.html'
})
export class AppComponent implements OnInit{

    ngOnInit(): void {}
}