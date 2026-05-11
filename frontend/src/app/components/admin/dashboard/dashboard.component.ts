import { Component, OnInit } from '@angular/core';
import { OrganizationService } from '../../../services/organization.service';
import { PartnerService } from '../../../services/partner.service';
import { ContractService } from '../../../services/contract.service';

@Component({
  selector: 'app-dashboard',


  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  // Stats principales
  totalOrganizations = 0;
  totalPartners = 0;
  totalContracts = 0;
  activeContracts = 0;

  // Stats avancées Organizations
  publicOrgs = 0;
  privateOrgs = 0;
  mixedOrgs = 0;
  activeOrgs = 0;

  // Stats avancées Partners
  activePartners = 0;
  pendingPartners = 0;
  suspendedPartners = 0;

  // Stats avancées Contracts
  draftContracts = 0;
  expiredContracts = 0;
  commercialContracts = 0;
  institutionalContracts = 0;
  technicalContracts = 0;

  // Coverage stats
  busOrgs = 0;
  trainOrgs = 0;
  metroOrgs = 0;
  louageOrgs = 0;
  batahOrgs = 0;

  recentContracts: any[] = [];

  constructor(
    private organizationService: OrganizationService,
    private partnerService: PartnerService,
    private contractService: ContractService
  ) {}

  ngOnInit(): void {
    this.organizationService.getAll().subscribe({
      next: (data) => {
        this.totalOrganizations = data.length;
        this.publicOrgs = data.filter(o => o.type === 'PUBLIC').length;
        this.privateOrgs = data.filter(o => o.type === 'PRIVATE').length;
        this.mixedOrgs = data.filter(o => o.type === 'MIXED').length;
        this.activeOrgs = data.filter(o => o.status === 'ACTIVE').length;
        this.busOrgs = data.filter(o => o.coverageType === 'BUS').length;
        this.trainOrgs = data.filter(o => o.coverageType === 'TRAIN').length;
        this.metroOrgs = data.filter(o => o.coverageType === 'METRO').length;
        this.louageOrgs = data.filter(o => o.coverageType === 'LOUAGE').length;
        this.batahOrgs = data.filter(o => o.coverageType === 'BATAH').length;
      }
    });

    this.partnerService.getAll().subscribe({
      next: (data) => {
        this.totalPartners = data.length;
        this.activePartners = data.filter(p => p.status === 'ACTIVE').length;
        this.pendingPartners = data.filter(p => p.status === 'PENDING').length;
        this.suspendedPartners = data.filter(p => p.status === 'SUSPENDED').length;
      }
    });

    this.contractService.getAll().subscribe({
      next: (data) => {
        this.totalContracts = data.length;
        this.activeContracts = data.filter(c => c.status === 'ACTIVE').length;
        this.draftContracts = data.filter(c => c.status === 'DRAFT').length;
        this.expiredContracts = data.filter(c => c.status === 'EXPIRED').length;
        this.commercialContracts = data.filter(c => c.contractType === 'COMMERCIAL').length;
        this.institutionalContracts = data.filter(c => c.contractType === 'INSTITUTIONAL').length;
        this.technicalContracts = data.filter(c => c.contractType === 'TECHNICAL').length;
        this.recentContracts = data.slice(-3).reverse();
      }
    });
  }
}








