import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { Organization } from '../../../../models/organization-partner/organization';
import { Partner } from '../../../../models/organization-partner/partner';
import { OrganizationService } from '../../../../services/organization.service';
import { PartnerService } from '../../../../services/partner.service';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-operator-detail',
  templateUrl: './operator-detail.component.html',
  styleUrl: './operator-detail.component.scss'
})
export class OperatorDetailComponent implements OnInit {
  operator?: Organization;
  zones: any[] = [];
  partners: Partner[] = [];

  constructor(
    private organizationService: OrganizationService,
    private partnerService: PartnerService,
    private route: ActivatedRoute,
    private http: HttpClient, private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.organizationService.getById(id).subscribe({
      next: (data) => this.operator = data
    });
    this.http.get<any[]>(`/api/zones/organization/${id}`).subscribe({
      next: (data) => this.zones = data
    });
    this.partnerService.getByOrganizationId(id).subscribe({
      next: (data) => this.partners = data
    });
  }
}

