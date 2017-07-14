import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { AuthenticationService } from '../../services/index';

@Component({
    moduleId: module.id,
    templateUrl: 'login.component.html'
})

export class LoginComponent implements OnInit {
    model: any = {};
    loading = false;
    //returnUrl: string;

    constructor(
        private router: Router,
        private authenticationService: AuthenticationService
        //,private alertService: AlertService
        ) { }

    ngOnInit() {
        // Reset login status
        this.authenticationService.logout();

        // Get return url from route parameters or default to '/'
        //this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    }

    login() {
        this.loading = true;

        this.authenticationService.login(this.model.username, this.model.password)
            .subscribe(
                data => {
                    this.loading = false;
                    if( !data.status ){
                        this.router.navigate(['']);
                    }else{
                        this.model.errorMessage = data.errorMessage;
                    }
                },
                error => {
                    this.loading = false;
                    this.model.errorMessage = 'Error establishing database connection.';
                });
    }
}
