import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LinesPublicComponent } from './lines-public.component';

describe('LinesPublicComponent', () => {
  let component: LinesPublicComponent;
  let fixture: ComponentFixture<LinesPublicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LinesPublicComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LinesPublicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
