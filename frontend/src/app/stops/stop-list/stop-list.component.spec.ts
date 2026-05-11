import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StopListComponent } from './stop-list.component';

describe('StopListComponent', () => {
  let component: StopListComponent;
  let fixture: ComponentFixture<StopListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StopListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StopListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
