import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserActivityLogComponent } from './user-activity-log.component';

@NgModule({
  declarations: [UserActivityLogComponent],
  imports: [CommonModule],
  exports: [UserActivityLogComponent]
})
export class UserActivityLogModule { }
