import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { Partner } from '../../../../models/organization-partner/partner';
import { PartnerService } from '../../../../services/partner.service';

@Component({
  selector: 'app-partner-form',
  templateUrl: './partner-form.component.html',
  styleUrl: './partner-form.component.scss'
})
export class PartnerFormComponent implements OnInit {

  partner: Partner = {
    name: '',
    industrySector: '',
    partnershipType: '',
    email: '',
    phoneNumber: '',
    website: '',
    logo: '',
    status: 'ACTIVE',
    organizationId: null
  };

  isEditMode = false;
  formSubmitted = false;
  id?: number;
  preselectedOrgId: number | null = null;

  constructor(
    private partnerService: PartnerService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const orgId = this.route.snapshot.queryParams['orgId'];
    if (orgId) {
      this.preselectedOrgId = +orgId;
      this.partner.organizationId = +orgId;
    }
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEditMode = true;
      this.partnerService.getById(this.id).subscribe({
        next: (data) => this.partner = data,
        error: (err) => console.error(err)
      });
    }
  }

  save(): void {
    this.formSubmitted = true;
    if (this.isEditMode && this.id) {
      this.partnerService.update(this.id, this.partner).subscribe({
        next: () => this.router.navigate(['/admin/partners']),
        error: (err) => console.error(err)
      });
    } else {
      this.partnerService.create(this.partner).subscribe({
        next: () => this.router.navigate(['/admin/partners']),
        error: (err) => console.error(err)
      });
    }
  }
}







