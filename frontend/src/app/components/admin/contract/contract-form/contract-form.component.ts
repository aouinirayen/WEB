import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Contract } from '../../../../models/organization-partner/contract';
import { ContractService } from '../../../../services/contract.service';
import { PartnerService } from '../../../../services/partner.service';
import { OrganizationService } from '../../../../services/organization.service';
import { GeminiService } from '../../../../services/gemini.service';

@Component({
  selector: 'app-contract-form',
  templateUrl: './contract-form.component.html',
  styleUrl: './contract-form.component.scss'
})
export class ContractFormComponent implements OnInit {

  contract: Contract = {
    contractType: 'COMMERCIAL',
    status: 'DRAFT',
    startDate: '',
    endDate: '',
    description: '',
    organizationId: 0,
    partnerId: 0
  };

  isEditMode = false;
  formSubmitted = false;
  id?: number;
  partners: any[] = [];
  organizations: any[] = [];

  // AI Clauses
  aiLoading = false;
  aiClauses: any[] = [];
  aiError = '';
  showClauses = false;

  constructor(
    private contractService: ContractService,
    private partnerService: PartnerService,
    private organizationService: OrganizationService,
    private geminiService: GeminiService,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.params['id'];
    
    // Load partners and organizations
    this.partnerService.getAll().subscribe({ next: (data) => this.partners = data });
    this.organizationService.getAll().subscribe({ next: (data) => this.organizations = data });

    if (this.id) {
      this.isEditMode = true;
      this.contractService.getById(this.id).subscribe({
        next: (data) => {
          if (data.startDate) data.startDate = new Date(data.startDate).toISOString().split("T")[0];
          if (data.endDate) data.endDate = new Date(data.endDate).toISOString().split("T")[0];
          this.contract = data;
        }
      });
    }
  }

  getPartnerName(): string {
    const p = this.partners.find(p => p.id === +this.contract.partnerId!);
    return p ? p.name : '';
  }

  getPartnerSector(): string {
    const p = this.partners.find(p => p.id === +this.contract.partnerId!);
    return p ? p.industrySector : '';
  }

  getOrgName(): string {
    const o = this.organizations.find(o => o.id === +this.contract.organizationId!);
    return o ? o.name : '';
  }

  getOrgType(): string {
    const o = this.organizations.find(o => o.id === +this.contract.organizationId!);
    return o ? o.coverageType : '';
  }

  getDurationMonths(): number {
    if (!this.contract.startDate || !this.contract.endDate) return 12;
    const start = new Date(this.contract.startDate);
    const end = new Date(this.contract.endDate);
    return Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24 * 30));
  }

  generateClauses(): void {
    if (!this.contract.partnerId || !this.contract.organizationId) {
      this.aiError = 'Please select a partner and an organization before generating clauses.';
      return;
    }

    this.aiLoading = true;
    this.aiError = '';
    this.aiClauses = [];
    this.showClauses = true;

    this.geminiService.generateContractClauses(
      this.getPartnerName(),
      this.getPartnerSector(),
      this.getOrgName(),
      this.getOrgType(),
      this.contract.contractType,
      this.getDurationMonths()
    ).subscribe({
      next: (response: any) => {
        try {
          const text = response.candidates[0].content.parts[0].text;
          const clean = text.replace(/```json|```/g, '').trim();
          const parsed = JSON.parse(clean);
          this.aiClauses = parsed.clauses;
        } catch (e) {
          this.aiError = 'Error lors du parsing de la reponse AI.';
        }
        this.aiLoading = false;
      },
      error: (err) => {
        this.aiError = 'Error API Gemini: ' + (err.error?.error?.message || err.message);
        this.aiLoading = false;
      }
    });
  }

  applyClausesToDescription(): void {
    if (this.aiClauses.length > 0) {
      this.contract.description = this.aiClauses
        .map(c => `CLAUSE ${c.numero} - ${c.titre}:\n${c.contenu}`)
        .join('\n\n');
    }
  }

  save(): void {
    this.formSubmitted = true;
    // Convert to numbers and fix dates
    this.contract.organizationId = +this.contract.organizationId!;
    this.contract.partnerId = +this.contract.partnerId!;
    // Fix date format - ensure YYYY-MM-DD
    if (this.contract.startDate) {
      this.contract.startDate = new Date(this.contract.startDate).toISOString().split("T")[0];
    }
    if (this.contract.endDate) {
      this.contract.endDate = new Date(this.contract.endDate).toISOString().split("T")[0];
    }
    if (this.isEditMode && this.id) {
      this.contractService.update(this.id, this.contract).subscribe({
        next: () => this.router.navigate(['/admin/contracts'])
      });
    } else {
      this.contractService.create(this.contract).subscribe({
        next: () => this.router.navigate(['/admin/contracts'])
      });
    }
  }
}







