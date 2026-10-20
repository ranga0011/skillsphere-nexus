import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { AssessmentService } from './assessment.service';

describe('AssessmentService', () => {
  let service: AssessmentService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AssessmentService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('sends verification as an explicit PUT action', () => {
    service.verifyAssessment(19, 'VERIFIED').subscribe();

    const request = httpTestingController.expectOne(
      'http://localhost:8080/api/assessments/19/verification'
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ verificationStatus: 'VERIFIED' });

    request.flush({
      id: 19,
      employeeId: 4,
      skillId: 3,
      score: 88,
      maxScore: 100,
      assessmentDate: '2026-10-16',
      assessmentMethod: 'Practical',
      verificationStatus: 'VERIFIED'
    });
  });
});