import { NgModule }      from '@angular/core';
import { TabsModule, CollapseModule } from 'ng2-bootstrap/ng2-bootstrap';
import { BrowserModule } from '@angular/platform-browser';

import { HostReportsComponent }  from './hostReports.component';
@NgModule({
  imports:      [ CollapseModule.forRoot(), TabsModule.forRoot(), BrowserModule ],
  exports: [ HostReportsComponent ],
  declarations: [ HostReportsComponent ],
  bootstrap:    [ HostReportsComponent ]
})
export class HostReportsModule {  }
