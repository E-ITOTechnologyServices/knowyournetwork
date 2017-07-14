import { NgModule }      from '@angular/core';
import { FormsModule } from "@angular/forms";
import { BrowserModule } from '@angular/platform-browser';
import { CollapseModule } from 'ng2-bootstrap/ng2-bootstrap';
import { Ng2DatetimePickerModule } from 'ng2-datetime-picker';

/* App includes */
import { AppComponent }  from './app.component';
import { AppRoutingModule }  from './app.routing';
import { NewsTickerModule, ReportsModule, HostReportsModule, HostInfoModule, SyslogModule, KibanaModule, GraphsModule, AQLrulesModule, LaraModule }  from './modules/index';
import { SearchComponent, DashboardComponent, LoginComponent }  from './components/index';
import { DashboardService, AuthenticationService, NewsTickerService } from './services/index';
import { AuthGuard } from "./guards/auth.guard";

@NgModule({
  imports: [
    BrowserModule,
    CollapseModule.forRoot(),
    Ng2DatetimePickerModule,
    FormsModule,
    AppRoutingModule,
    NewsTickerModule,
    ReportsModule,
    HostReportsModule,
    HostInfoModule,
    SyslogModule,
    KibanaModule,
    GraphsModule,
    AQLrulesModule,
    LaraModule
  ],
  declarations: [
    AppComponent,
    SearchComponent,
    LoginComponent,
    DashboardComponent
  ],
  providers: [ AuthGuard, AuthenticationService, DashboardService, NewsTickerService ],
  bootstrap: [ AppComponent ]
})
export class AppModule {}
