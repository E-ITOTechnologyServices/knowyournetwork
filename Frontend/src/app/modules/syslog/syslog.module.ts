import { NgModule }      from '@angular/core';
import { TabsModule, CollapseModule } from 'ng2-bootstrap/ng2-bootstrap';
import { BrowserModule } from '@angular/platform-browser';

import { SyslogComponent }  from './syslog.component';

@NgModule({
  imports:      [ CollapseModule.forRoot(), TabsModule.forRoot(), BrowserModule ],
  exports: [ SyslogComponent ],
  declarations: [ SyslogComponent ],
  bootstrap:    [ SyslogComponent ]
})
export class SyslogModule {  }
