import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Http, Headers, Response, ResponseOptions, RequestOptions } from '@angular/http';
import { Cookie } from 'ng2-cookies/ng2-cookies';
import { AuthenticationService } from "../services/index";

@Injectable()
export class AuthGuard implements CanActivate {

    constructor( private router: Router, private authService: AuthenticationService ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot){
        return this.authService.isAuthenticated().map( res => {
            if(!res) {
                this.router.navigate(['login']);
            }
            return res;
        });
    }
}