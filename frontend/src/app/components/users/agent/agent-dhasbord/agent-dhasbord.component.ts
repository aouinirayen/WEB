import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '../../../../services/auth/auth.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-agent-dhasbord',
  templateUrl: './agent-dhasbord.component.html',
  styleUrls: ['./agent-dhasbord.component.css']
})
export class AgentDhasbordComponent implements OnInit {
  currentUser$!: Observable<User | null>;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser$ = this.authService.currentUser$;
  }
}
