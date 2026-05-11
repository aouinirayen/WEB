import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '../../../../services/auth/auth.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-operator-dhasbord',
  templateUrl: './operator-dhasbord.component.html',
  styleUrls: ['./operator-dhasbord.component.css']
})
export class OperatorDhasbordComponent implements OnInit {
  currentUser$!: Observable<User | null>;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser$ = this.authService.currentUser$;
  }
}
