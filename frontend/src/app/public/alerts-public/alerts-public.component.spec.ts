import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlertsPublicComponent } from './alerts-public.component';

describe('AlertsPublicComponent', () => {
  let component: AlertsPublicComponent;
  let fixture: ComponentFixture<AlertsPublicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertsPublicComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AlertsPublicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
