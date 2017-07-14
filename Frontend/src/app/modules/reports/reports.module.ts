import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';
import { CollapseModule, ModalModule } from 'ng2-bootstrap/ng2-bootstrap';
import { Ng2DatetimePickerModule } from 'ng2-datetime-picker';

import { ReportsComponent }  from './reports.component';

@NgModule({
  imports:      [ CollapseModule.forRoot(), ModalModule.forRoot(), BrowserModule, FormsModule, Ng2DatetimePickerModule ],
  exports: [ ReportsComponent ],
  declarations: [ ReportsComponent ],
  bootstrap:    [ ReportsComponent ]
})
export class ReportsModule {  }
