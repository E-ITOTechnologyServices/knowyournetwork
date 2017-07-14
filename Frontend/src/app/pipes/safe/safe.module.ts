import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { SafePipe } from './safe.pipe';

@NgModule({
    imports: [BrowserModule],
    declarations: [SafePipe],
    exports: [SafePipe]
})
export class SafeModule { }