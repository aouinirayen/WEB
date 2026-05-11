import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverLicenseUploadComponent } from './driver-license-upload.component';

describe('DriverLicenseUploadComponent', () => {
  let component: DriverLicenseUploadComponent;
  let fixture: ComponentFixture<DriverLicenseUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverLicenseUploadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverLicenseUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
