import { Injectable } from '@angular/core';
import {Observable, Observer, Subject, BehaviorSubject} from "rxjs/Rx";
import 'rxjs/add/operator/map'
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/timeout';
import { DashboardService } from "./dashboard.service";

@Injectable()
export class AuthenticationService {
    constructor( private dashboardService: DashboardService) { }

    login(username: string, password: string) {

        return this.dashboardService.httpPOST(this.dashboardService.dataSource('login'), { username: username, password: password })
            .map((response: any) => {
                let res = JSON.parse(response.text());

                // Store user details in local storage to keep user logged in between page refreshes
                if (!res.status && res.content) {
                    localStorage.setItem('currentUser', JSON.stringify(res.content));
                }

                return res;
            });
    }

    logout() {
        this.dashboardService.httpPOST(this.dashboardService.dataSource('logout'), { })
            .map((res:Response) => res.json())
            .subscribe(
                data => {
                    // Clear user data if is user logged in
                    if(!data.status && data.errorMessage == "OK") {
                        // Local user data
                        localStorage.removeItem('currentUser');
                    }
                },
                err => {}
            );
    }

    isAuthenticated() : Observable<boolean> {
        return this.dashboardService.httpPOST(this.dashboardService.dataSource('security/checkLogin'), {})
            .timeout(10000)// Timeout limit is currently needed because timeout on Node.js MW is set to 180s
            .map( (res: any) => {
                if(res.json().status == 2){
                    return false;
                }else{
                    return true;
                }
            } )
            .catch( () => { return Observable.of(false); });
    }
}