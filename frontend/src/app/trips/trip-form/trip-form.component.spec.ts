import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TripFormComponent } from './trip-form.component';

describe('TripFormComponent', () => {
  let component: TripFormComponent;
  let fixture: ComponentFixture<TripFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TripFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TripFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
