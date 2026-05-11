import { Component, OnInit } from '@angular/core';
import { Organization } from '../../../../models/organization-partner/organization';
import { OrganizationService } from '../../../../services/organization.service';

@Component({
  selector: 'app-operator-list',
  templateUrl: './operator-list.component.html',
  styleUrl: './operator-list.component.scss'
})
export class OperatorListComponent implements OnInit {
  operators: Organization[] = [];

  constructor(private organizationService: OrganizationService) {}

  ngOnInit(): void {
    this.organizationService.getAll().subscribe({
      next: (data) => this.operators = data,
      error: (err) => console.error(err)
    });
  }

  onMouseMove(event: MouseEvent, card: HTMLElement): void {
    const rect = card.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    const rotateX = ((y - centerY) / centerY) * -12;
    const rotateY = ((x - centerX) / centerX) * 12;
    card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.04)`;
    card.style.boxShadow = `0 25px 50px rgba(26,35,126,0.25)`;
  }

  onMouseLeave(card: HTMLElement): void {
    card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale(1)';
    card.style.boxShadow = '0 8px 32px rgba(26,35,126,0.12)';
  }
}
