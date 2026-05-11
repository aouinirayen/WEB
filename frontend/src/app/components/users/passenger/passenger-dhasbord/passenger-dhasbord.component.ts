import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '../../../../services/auth/auth.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-passenger-dhasbord',
  templateUrl: './passenger-dhasbord.component.html',
  styleUrls: ['./passenger-dhasbord.component.css']
})
export class PassengerDhasbordComponent implements OnInit {
  currentUser$!: Observable<User | null>;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser$ = this.authService.currentUser$;
  }
}
