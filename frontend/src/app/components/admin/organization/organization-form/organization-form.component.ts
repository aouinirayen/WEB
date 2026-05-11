import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Organization } from '../../../../models/organization-partner/organization';
import { Partner } from '../../../../models/organization-partner/partner';
import { OrganizationService } from '../../../../services/organization.service';
import { PartnerService } from '../../../../services/partner.service';

@Component({
  selector: 'app-organization-form',
  templateUrl: './organization-form.component.html',
  styleUrl: './organization-form.component.scss'
})
export class OrganizationFormComponent implements OnInit {
  organization: Organization = {
    name: '', acronyme: '', transportType: '',
    email: '', phoneNumber: '', website: '',
    logo: '', type: 'PUBLIC', status: 'ACTIVE', coverageType: 'BUS'
  };
  isEditMode = false;
  id?: number;
  formSubmitted = false;

  governorates = [
    'Tunis', 'Ariana', 'Ben Arous', 'Manouba', 'Nabeul', 'Zaghouan',
    'Bizerte', 'Beja', 'Jendouba', 'Kef', 'Siliana', 'Sousse',
    'Monastir', 'Mahdia', 'Sfax', 'Kairouan', 'Kasserine', 'Sidi Bouzid',
    'Gabes', 'Mednine', 'Tataouine', 'Gafsa', 'Tozeur', 'Kebili'
  ];
  selectedGovernorates: string[] = [];
  existingZones: any[] = [];

  allPartners: Partner[] = [];
  linkedPartners: Partner[] = [];
  existingContracts: any[] = [];
  showNewPartnerForm = false;
  newPartner: Partner = {
    name: '', industrySector: '', partnershipType: '',
    email: '', phoneNumber: '', website: '', logo: '', status: 'ACTIVE'
  };

  constructor(
    private organizationService: OrganizationService,
    private partnerService: PartnerService,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.params['id'];
    this.partnerService.getAll().subscribe({
      next: (data) => this.allPartners = data
    });

    if (this.id) {
      this.isEditMode = true;
      this.organizationService.getById(this.id).subscribe({
        next: (data) => this.organization = data
      });
      this.http.get<any[]>(`/api/zones/organization/${this.id}`).subscribe({
        next: (zones) => {
          this.existingZones = zones;
          this.selectedGovernorates = zones.map(z => z.governorate);
        }
      });
      // Load existing contracts to know which partners are linked
      this.http.get<any[]>(`/api/contracts/organization/${this.id}`).subscribe({
        next: (contracts) => {
          this.existingContracts = contracts;
          this.linkedPartners = contracts
            .filter(c => c.partnerId)
            .map(c => ({ id: c.partnerId, name: c.partnerName } as Partner));
        }
      });
    }
  }

  toggleGovernorate(gov: string): void {
    const idx = this.selectedGovernorates.indexOf(gov);
    if (idx === -1) this.selectedGovernorates.push(gov);
    else this.selectedGovernorates.splice(idx, 1);
  }

  isSelected(gov: string): boolean {
    return this.selectedGovernorates.includes(gov);
  }

  isPartnerLinked(partner: Partner): boolean {
    return this.linkedPartners.some(p => p.id === partner.id);
  }

  togglePartner(partner: Partner): void {
    if (this.isPartnerLinked(partner)) {
      this.linkedPartners = this.linkedPartners.filter(p => p.id !== partner.id);
    } else {
      this.linkedPartners.push(partner);
    }
  }

  addNewPartner(): void {
    this.partnerService.create({ ...this.newPartner, organizationId: this.id || null }).subscribe({
      next: (created) => {
        this.allPartners.push(created);
        this.linkedPartners.push(created);
        this.newPartner = { name: '', industrySector: '', partnershipType: '', email: '', phoneNumber: '', status: 'ACTIVE' };
        this.showNewPartnerForm = false;
      }
    });
  }

  getFieldError(field: string, form: any): string {
    const control = form.controls[field];
    if (!control || !control.errors) return "";
    if (control.errors["required"]) return "This field is required.";
    if (control.errors["minlength"]) return "Minimum " + control.errors["minlength"].requiredLength + " characters required.";
    if (control.errors["maxlength"]) return "Maximum " + control.errors["maxlength"].requiredLength + " characters allowed.";
    if (control.errors["pattern"]) return "Invalid format.";
    return "Invalid value.";
  }

  save(): void {
    this.formSubmitted = true;
    const saveOrg = (orgId: number) => {
      // Save zones
      const existing = this.existingZones.map(z => z.governorate);
      const toAdd = this.selectedGovernorates.filter(g => !existing.includes(g));
      const toRemove = this.existingZones.filter(z => !this.selectedGovernorates.includes(z.governorate));
      const zoneRequests = [
        ...toAdd.map(gov => this.http.post('/api/zones', {
          governorate: gov, region: 'NORTH', isHeadquarter: false,
          coverageType: this.organization.coverageType, organizationId: orgId
        }).toPromise()),
        ...toRemove.map(z => this.http.delete(`/api/zones/${z.id}`).toPromise())
      ];

      // Save partner links via contracts
      const existingPartnerIds = this.existingContracts.map(c => c.partnerId);
      const toLink = this.linkedPartners.filter(p => !existingPartnerIds.includes(p.id));
      const toUnlink = this.existingContracts.filter(c =>
        !this.linkedPartners.some(p => p.id === c.partnerId)
      );

      const now = new Date();
      const nextYear = new Date(now.getFullYear() + 1, now.getMonth(), now.getDate());

      const contractRequests = [
        ...toLink.map(p => this.http.post('/api/contracts', {
          contractType: 'COMMERCIAL',
          status: 'ACTIVE',
          startDate: now.toISOString(),
          endDate: nextYear.toISOString(),
          description: `Contract between organization ${orgId} and partner ${p.name}`,
          organizationId: orgId,
          partnerId: p.id
        }).toPromise()),
        ...toUnlink.map(c => this.http.delete(`/api/contracts/${c.id}`).toPromise())
      ];

      Promise.all([...zoneRequests, ...contractRequests]).then(() => {
        this.router.navigate(['/admin/organizations']);
      });
    };

    if (this.isEditMode && this.id) {
      this.organizationService.update(this.id, this.organization).subscribe({
        next: () => saveOrg(this.id!)
      });
    } else {
      this.organizationService.create(this.organization).subscribe({
        next: (org: any) => saveOrg(org.id)
      });
    }
  }

  // Logo URL safety check
  showLogoModal = false;
  logoCheckLoading = false;
  logoCheckResult: 'safe' | 'danger' | 'unknown' | null = null;
  logoCheckMessage = '';
  pendingLogoUrl = '';

  checkLogoUrl(): void {
    const url = (this.organization.logo || '').trim();

    // Cas base64
    if (!url || url.startsWith('data:')) {
      this.pendingLogoUrl = url;
      this.logoCheckResult = 'unknown';
      this.logoCheckMessage = 'Image base64 locale detectee. Impossible de verifier la source via API. Utilisez une URL externe pour une verification complete.';
      this.showLogoModal = true;
      return;
    }

    // Validation format URL
    let urlObj: URL;
    try {
      urlObj = new URL(url);
    } catch {
      this.pendingLogoUrl = url;
      this.logoCheckResult = 'danger';
      this.logoCheckMessage = 'URL invalide ou malformee. Verifiez que le lien commence par https://.';
      this.showLogoModal = true;
      return;
    }

    // HTTP non securise
    if (urlObj.protocol === 'http:') {
      this.pendingLogoUrl = url;
      this.logoCheckResult = 'danger';
      this.logoCheckMessage = 'URL non securisee (HTTP). Utilisez HTTPS pour proteger votre site.';
      this.showLogoModal = true;
      return;
    }

    // Ouvrir modal + demarrer analyse
    this.pendingLogoUrl = url;
    this.logoCheckLoading = true;
    this.logoCheckResult = null;
    this.logoCheckMessage = '';
    this.showLogoModal = true;

    const hostname = urlObj.hostname.toLowerCase();

    // Patterns dangereux connus
    const dangerousPatterns = [
      /bit\.ly|tinyurl\.com|goo\.gl|t\.co|shorturl/i,
      /\.(tk|ml|ga|cf|gq)$/i,
      /malware|phishing|virus|hack|crack|exploit/i,
      /free.*download|crack.*software/i,
    ];

    // Domaines de confiance
    const trustedDomains = [
      /\.(gov\.tn|gov|edu|ac\.)$/i,
      /wikipedia\.org|wikimedia\.org/i,
      /githubusercontent\.com|github\.com|gitlab\.com/i,
      /cloudinary\.com|imgur\.com|ibb\.co/i,
      /googleapis\.com|gstatic\.com|google\.com/i,
      /microsoft\.com|azure\.com|office\.com/i,
      /amazonaws\.com|s3\.|cloudfront\.net/i,
      /unsplash\.com|pexels\.com|pixabay\.com/i,
      /cdn\.|static\.|assets\./i,
      /tunisietelecom\.tn|topnet\.tn|orange\.tn/i,
    ];

    const isDangerous = dangerousPatterns.some(p => p.test(hostname));
    const isTrusted = trustedDomains.some(p => p.test(hostname));

    if (isDangerous) {
      this.logoCheckResult = 'danger';
      this.logoCheckMessage = 'Domaine suspect detecte: ' + hostname + '. Ce domaine est connu pour des activites malveillantes.';
      this.logoCheckLoading = false;
      return;
    }

    // Appel API URLScan.io - recherche si le domaine a deja ete analyse
    const apiUrl = '/urlscan/api/v1/search/?q=domain:' + hostname + '&size=1';

    this.http.get<any>(apiUrl).subscribe({
      next: (response) => {
        const results = response?.results || [];
        if (results.length > 0) {
          const scan = results[0];
          const verdicts = scan?.verdicts?.overall;
          if (verdicts?.malicious === true) {
            this.logoCheckResult = 'danger';
            this.logoCheckMessage = 'DANGER: Le domaine ' + hostname + ' a ete signale comme malveillant par URLScan.io. Ne pas utiliser cette URL.';
          } else if (verdicts?.score > 50) {
            this.logoCheckResult = 'danger';
            this.logoCheckMessage = 'ATTENTION: Score de risque eleve (' + verdicts.score + '/100) pour ' + hostname + ' selon URLScan.io.';
          } else if (isTrusted) {
            this.logoCheckResult = 'safe';
            this.logoCheckMessage = 'Verified and trusted domain: ' + hostname + '. No threat detected by URLScan.io.';
          } else {
            this.logoCheckResult = 'safe';
            this.logoCheckMessage = 'Domain analyzed by URLScan.io: ' + hostname + '. No threat detected. Score: ' + (verdicts?.score || 0) + '/100.';
          }
        } else if (isTrusted) {
          this.logoCheckResult = 'safe';
          this.logoCheckMessage = 'Recognized trusted domain: ' + hostname + '. No malicious history.';
        } else {
          this.logoCheckResult = 'unknown';
          this.logoCheckMessage = 'Domaine ' + hostname + ' non repertorie dans URLScan.io. Verifiez manuellement la fiabilite de cette source.';
        }
        this.logoCheckLoading = false;
      },
      error: () => {
        // Si API indisponible, fallback sur analyse locale
        if (isTrusted) {
          this.logoCheckResult = 'safe';
          this.logoCheckMessage = 'Recognized trusted domain localement: ' + hostname + '. (API URLScan.io indisponible)';
        } else {
          this.logoCheckResult = 'unknown';
          this.logoCheckMessage = 'Impossible de contacter l API de verification pour ' + hostname + '. Verifiez manuellement.';
        }
        this.logoCheckLoading = false;
      }
    });
  }
  applyLogoUrl(): void {
    this.organization.logo = this.pendingLogoUrl;
    this.showLogoModal = false;
  }

  rejectLogoUrl(): void {
    this.organization.logo = '';
    this.pendingLogoUrl = '';
    this.showLogoModal = false;
  }
}