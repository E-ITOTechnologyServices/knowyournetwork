import { NgModule }      from '@angular/core';
import { CollapseModule } from 'ng2-bootstrap/ng2-bootstrap';
import { BrowserModule } from '@angular/platform-browser';

import { HostInfoComponent }  from './hostInfo.component';
import { HostInfoService } from './hostInfo.service';

@NgModule({
  imports:      [ CollapseModule.forRoot(), BrowserModule ],
  exports: [ HostInfoComponent ],
  declarations: [ HostInfoComponent ],
  providers: [ HostInfoService ],
  bootstrap:    [ HostInfoComponent ]
})
export class HostInfoModule {  }
