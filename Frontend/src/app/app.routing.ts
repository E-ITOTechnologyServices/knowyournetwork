import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent, DashboardComponent } from "./components/index";
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
    { path: '', component: DashboardComponent, canActivate: [AuthGuard], pathMatch: 'full' },
    { path: 'login',  component: LoginComponent },
    { path: 'logout',  redirectTo: 'login' },
    { path: '**', redirectTo: ''}
];

@NgModule({
    imports: [ RouterModule.forRoot(routes) ],
    exports: [ RouterModule ]
})
export class AppRoutingModule {}