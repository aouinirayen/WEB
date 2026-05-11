import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { Partner } from '../../../../models/organization-partner/partner';
import { PartnerService } from '../../../../services/partner.service';
import { PartnerMediaService, PartnerMedia } from '../../../../services/partner-media.service';

@Component({
  selector: 'app-partner-list',
  templateUrl: './partner-list.component.html',
  styleUrl: './partner-list.component.scss'
})
export class PartnerListComponent implements OnInit {
  partners: Partner[] = [];
  mediaMap: { [partnerId: number]: PartnerMedia } = {};
  selectedPartner?: Partner;
  selectedMedia?: PartnerMedia | null;
  showModal = false;
  loadingMedia = false;
  safeVideoUrl?: SafeResourceUrl;

  constructor(
    private partnerService: PartnerService,
    private mediaService: PartnerMediaService,
    private sanitizer: DomSanitizer,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.partnerService.getAll().subscribe({
      next: (data) => {
        this.partners = data.filter(p => p.status === 'ACTIVE');
        // Charger les medias pour chaque partner
        this.partners.forEach(p => {
          if (p.id) {
            this.mediaService.getByPartnerId(p.id).then(media => {
              if (media) this.mediaMap[p.id!] = media;
            }).catch(() => {});
          }
        });
        // Si un ID est dans l URL, ouvrir le modal directement
        const id = this.route.snapshot.params['id'];
        if (id) {
          const partner = this.partners.find(p => p.id === +id);
          if (partner) this.openModal(partner);
        }
      }
    });
  }

  getPhotoUrl(partnerId: number): string | null {
    return this.mediaMap[partnerId]?.photoUrl || null;
  }

  openModal(partner: Partner): void {
    this.selectedPartner = partner;
    this.showModal = true;
    this.loadingMedia = true;
    this.selectedMedia = null;
    this.safeVideoUrl = undefined;

    if (partner.id) {
      this.mediaService.getByPartnerId(partner.id).then(media => {
        this.selectedMedia = media;
        this.loadingMedia = false;
        if (media?.videoUrl) {
          this.safeVideoUrl = this.sanitizer.bypassSecurityTrustResourceUrl(media.videoUrl);
        }
      }).catch(() => {
        this.loadingMedia = false;
      });
    }
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedPartner = undefined;
    this.selectedMedia = undefined;
    this.safeVideoUrl = undefined;
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
