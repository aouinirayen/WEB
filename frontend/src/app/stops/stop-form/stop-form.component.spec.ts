import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StopFormComponent } from './stop-form.component';

describe('StopFormComponent', () => {
  let component: StopFormComponent;
  let fixture: ComponentFixture<StopFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StopFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StopFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
