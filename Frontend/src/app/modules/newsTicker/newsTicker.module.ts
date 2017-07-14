import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CollapseModule } from 'ng2-bootstrap/ng2-bootstrap';

import { NewsTickerComponent }  from './newsTicker.component';
import {NewsTickerService} from './newsTicker.service';

@NgModule({
  imports:      [ CollapseModule.forRoot(), BrowserModule ],
  exports: [ NewsTickerComponent ],
  declarations: [ NewsTickerComponent ],
  providers: [ NewsTickerService ],
  bootstrap:    [ NewsTickerComponent ]
})
export class NewsTickerModule {  }
