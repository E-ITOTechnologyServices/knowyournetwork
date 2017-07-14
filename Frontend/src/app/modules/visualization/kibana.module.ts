import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CollapseModule, TooltipModule } from 'ng2-bootstrap/ng2-bootstrap';
import { SafeModule } from '../../pipes/safe/safe.module';

import { KibanaComponent }  from './kibana.component';

@NgModule({
  imports:      [ CollapseModule.forRoot(), TooltipModule.forRoot(), BrowserModule, SafeModule ],
  exports: [ KibanaComponent ],
  declarations: [ KibanaComponent ],
  bootstrap:    [ KibanaComponent ]
})
export class KibanaModule {}
