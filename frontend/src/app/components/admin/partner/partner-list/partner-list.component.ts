import { Component, OnInit } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Partner } from "../../../../models/organization-partner/partner";
import { PartnerService } from "../../../../services/partner.service";

@Component({
  selector: "app-partner-list",
  templateUrl: "./partner-list.component.html",
  styleUrl: "./partner-list.component.scss"
})
export class PartnerListComponent implements OnInit {
  partners: Partner[] = [];
  filteredPartners: Partner[] = [];
  paginatedPartners: Partner[] = [];
  contractMap: { [partnerId: number]: number } = {};
  currentPage = 1;
  itemsPerPage = 5;
  searchTerm = "";
  filterStatus = "";

  Math = Math;

  constructor(
    private partnerService: PartnerService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadPartners();
  }

  loadPartners(): void {
    this.partnerService.getAll().subscribe({
      next: (data) => {
        this.partners = data;
        // Charger le contract ID pour chaque partner
        this.partners.forEach(p => {
          if (p.id) {
            this.http.get<any[]>(`/api/contracts/partner/${p.id}`).subscribe({
              next: (contracts) => {
                if (contracts && contracts.length > 0) {
                  this.contractMap[p.id!] = contracts[0].id;
                }
              }
            });
          }
        });
        this.applyFilters();
      },
      error: (err) => console.error(err)
    });
  }

  getContractId(partnerId: number): number | null {
    return this.contractMap[partnerId] || null;
  }

  applyFilters(): void {
    this.filteredPartners = this.partners.filter(p => {
      const matchSearch = !this.searchTerm ||
        p.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.industrySector.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatus = !this.filterStatus || p.status === this.filterStatus;
      return matchSearch && matchStatus;
    });
    this.updatePage(1);
  }

  updatePage(page: number): void {
    this.currentPage = page;
    const start = (page - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedPartners = this.filteredPartners.slice(start, end);
  }

  clearFilters(): void {
    this.searchTerm = "";
    this.filterStatus = "";
    this.applyFilters();
  }

  openContract(partnerId: number): void {
    const contractId = this.contractMap[partnerId];
    if (contractId) {
      this.loadContractsForPartner(partnerId);
    } else {
      alert("No contracts available for this partner");
    }
  }

  delete(id: number): void {
    if (confirm("Do you want to delete this partner?")) {
      this.partnerService.delete(id).subscribe({
        next: () => this.loadPartners(),
        error: (err) => console.error(err)
      });
    }
  }
// Contracts modal
  showContractsModal = false;
  contractsModalLoading = false;
  selectedPartnerContracts: any[] = [];
  selectedPartnerName = '';

  loadContractsForPartner(partnerId: number): void {
    this.showContractsModal = true;
    this.contractsModalLoading = true;
    this.selectedPartnerContracts = [];
    const partner = this.partners.find(p => p.id === partnerId);
    this.selectedPartnerName = partner ? partner.name : 'Partner';

    this.http.get<any[]>(`http://localhost:8081/api/contracts/partner/${partnerId}`).subscribe({
      next: (contracts) => {
        this.selectedPartnerContracts = contracts || [];
        this.contractsModalLoading = false;
      },
      error: () => {
        this.selectedPartnerContracts = [];
        this.contractsModalLoading = false;
      }
    });
  }

  downloadContractPdf(contractId: number, partnerName: string, orgName: string): void {
    window.open(`http://localhost:8081/api/pdf/contract/${contractId}`, '_blank');
  }
}