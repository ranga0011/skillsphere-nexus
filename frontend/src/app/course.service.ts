import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Course, CourseEnrollment, LearningPath } from './feature-models';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly apiUrl = 'http://localhost:8080/api/courses';

  constructor(private http: HttpClient) {}

  getAllCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(this.apiUrl);
  }

  createCourse(course: Course): Observable<Course> {
    return this.http.post<Course>(this.apiUrl, course);
  }

  getAllLearningPaths(): Observable<LearningPath[]> {
    return this.http.get<LearningPath[]>(`${this.apiUrl}/learning-paths`);
  }

  createLearningPath(path: LearningPath): Observable<LearningPath> {
    return this.http.post<LearningPath>(`${this.apiUrl}/learning-paths`, path);
  }

  getMyEnrollments(): Observable<CourseEnrollment[]> {
    return this.http.get<CourseEnrollment[]>(`${this.apiUrl}/enrollments/me`);
  }

  enroll(courseId: number): Observable<CourseEnrollment> {
    return this.http.post<CourseEnrollment>(`${this.apiUrl}/${courseId}/enroll`, {});
  }

  updateProgress(enrollmentId: number, progress: number): Observable<CourseEnrollment> {
    return this.http.put<CourseEnrollment>(`${this.apiUrl}/enrollments/${enrollmentId}/progress?progress=${progress}`, {});
  }
}
