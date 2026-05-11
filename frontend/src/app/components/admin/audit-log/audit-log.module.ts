import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { AuditLogComponent } from './audit-log.component';

@NgModule({
  declarations: [AuditLogComponent],
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  exports: [AuditLogComponent]
})
export class AuditLogModule { }
