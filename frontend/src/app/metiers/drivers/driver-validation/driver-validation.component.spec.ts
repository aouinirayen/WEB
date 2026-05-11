import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverValidationComponent } from './driver-validation.component';

describe('DriverValidationComponent', () => {
  let component: DriverValidationComponent;
  let fixture: ComponentFixture<DriverValidationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverValidationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverValidationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
