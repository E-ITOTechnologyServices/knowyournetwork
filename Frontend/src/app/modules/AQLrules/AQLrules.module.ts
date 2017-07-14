import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';
import { CollapseModule, ModalModule } from 'ng2-bootstrap/ng2-bootstrap';
import { Ng2DatetimePickerModule } from 'ng2-datetime-picker';

import { AQLrulesComponent }  from './AQLrules.component';

@NgModule({
  imports:      [ CollapseModule.forRoot(), ModalModule.forRoot(), BrowserModule, FormsModule, Ng2DatetimePickerModule ],
  exports: [ AQLrulesComponent ],
  declarations: [ AQLrulesComponent ],
  bootstrap:    [ AQLrulesComponent ]
})
export class AQLrulesModule {  }