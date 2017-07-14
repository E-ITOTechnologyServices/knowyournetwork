import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CollapseModule } from 'ng2-bootstrap/ng2-bootstrap';

import { GraphsComponent }  from './graphs.component';
import { ResetsComponent }  from './resets/resets.component';

@NgModule({
  imports:      [ CollapseModule.forRoot(), BrowserModule ],
  exports: [ GraphsComponent ],
  declarations: [ GraphsComponent, ResetsComponent ],
  bootstrap:    [ GraphsComponent ]
})
export class GraphsModule {  }
