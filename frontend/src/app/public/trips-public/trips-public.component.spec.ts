import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TripsPublicComponent } from './trips-public.component';

describe('TripsPublicComponent', () => {
  let component: TripsPublicComponent;
  let fixture: ComponentFixture<TripsPublicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TripsPublicComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TripsPublicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
