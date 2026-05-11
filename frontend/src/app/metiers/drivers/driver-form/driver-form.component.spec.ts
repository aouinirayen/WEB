import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverFormComponent } from './driver-form.component';

describe('DriverFormComponent', () => {
  let component: DriverFormComponent;
  let fixture: ComponentFixture<DriverFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
