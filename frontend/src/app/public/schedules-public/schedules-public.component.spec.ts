import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchedulesPublicComponent } from './schedules-public.component';

describe('SchedulesPublicComponent', () => {
  let component: SchedulesPublicComponent;
  let fixture: ComponentFixture<SchedulesPublicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchedulesPublicComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SchedulesPublicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
