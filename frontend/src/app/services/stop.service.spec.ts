import { TestBed } from '@angular/core/testing';

import { StopService } from './stop.service';

describe('StopService', () => {
  let service: StopService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StopService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
