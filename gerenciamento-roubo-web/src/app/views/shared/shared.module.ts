import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HeaderComponent} from './header';
import {NavbarComponent} from './navbar';
import {RouterModule} from '@angular/router';
import {ReactiveFormsModule} from '@angular/forms';


@NgModule({
            declarations: [
              HeaderComponent,
              NavbarComponent,
            ],
            exports: [
              HeaderComponent,
              NavbarComponent
            ],
            imports: [
              CommonModule,
              RouterModule,
              ReactiveFormsModule,
            ]
          })
export class SharedModule {
}
