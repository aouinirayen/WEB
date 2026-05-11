import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Organization } from '../../../../models/organization-partner/organization';
import { Partner } from '../../../../models/organization-partner/partner';
import { OrganizationService } from '../../../../services/organization.service';
import { PartnerService } from '../../../../services/partner.service';

@Component({
  selector: 'app-organization-detail',
  templateUrl: './organization-detail.component.html',
  styleUrl: './organization-detail.component.scss'
})
export class OrganizationDetailComponent implements OnInit {
  organization?: Organization;
  partners: Partner[] = [];
  orgId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orgService: OrganizationService,
    private partnerService: PartnerService
  ) {}

  ngOnInit(): void {
    this.orgId = +this.route.snapshot.params['id'];
    this.orgService.getById(this.orgId).subscribe({
      next: (data) => this.organization = data,
      error: (err) => console.error(err)
    });
    this.partnerService.getByOrganizationId(this.orgId).subscribe({
      next: (data) => this.partners = data,
      error: (err) => console.error(err)
    });
  }
}
