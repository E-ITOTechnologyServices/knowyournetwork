import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from '@angular/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'ng2-select/ng2-select';
import { CollapseModule, TooltipModule, ModalModule } from 'ng2-bootstrap/ng2-bootstrap';
import { Ng2DatetimePickerModule } from 'ng2-datetime-picker';
import { ClipboardModule } from 'ngx-clipboard';
import { SafeModule } from '../../pipes/safe/safe.module';

import { LaraComponent }  from './lara.component';
import { RulesetComponent }  from './ruleset/ruleset.component';
import { TopHostsComponent }  from './topHosts/topHosts.component';
import { PolicyFormComponent }  from './policyForm/policyForm.component';
import { HostInfoService } from '../hostInfo/hostInfo.service';
import { PolicyFormService } from './policyForm/policyForm.service';

@NgModule({
  imports: [ CollapseModule.forRoot(), TooltipModule.forRoot(), ModalModule.forRoot(), Ng2DatetimePickerModule, BrowserModule, HttpModule, FormsModule, CommonModule, ClipboardModule, SafeModule, SelectModule ],
  exports: [ LaraComponent ],
  declarations: [ LaraComponent, TopHostsComponent, PolicyFormComponent, RulesetComponent ],
  providers: [ HostInfoService, PolicyFormService ],
  bootstrap: [ LaraComponent ]
})
export class LaraModule {}
