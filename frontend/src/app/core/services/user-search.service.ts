import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { Subject } from 'rxjs';

export interface UserSuggestion {
  name: string;
  phone: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class UserSearchService {

  private url = 'http://localhost:8081/api/users/search-autocomplete';

  constructor(private http: HttpClient) {}

  search(query: string): Observable<UserSuggestion[]> {
    if (!query || query.trim().length < 2) return of([]);
    return this.http.get<UserSuggestion[]>(this.url, { params: { q: query.trim() } });
  }
}
