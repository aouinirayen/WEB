import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OperatorListComponent } from './operator-list.component';

describe('OperatorListComponent', () => {
  let component: OperatorListComponent;
  let fixture: ComponentFixture<OperatorListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OperatorListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OperatorListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});







