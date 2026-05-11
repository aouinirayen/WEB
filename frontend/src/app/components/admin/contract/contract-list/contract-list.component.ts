import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Contract } from '../../../../models/organization-partner/contract';
import { ContractService } from '../../../../services/contract.service';
import { ReportsService } from '../../../../services/reports.service';

@Component({
  selector: 'app-contract-list',
  templateUrl: './contract-list.component.html',
  styleUrl: './contract-list.component.scss'
})
export class ContractListComponent implements OnInit {

  contracts: Contract[] = [];
  paginatedContracts: Contract[] = [];
  currentPage = 1;
  itemsPerPage = 5;

  signatureMap: { [contractId: number]: any } = {};
  verifyResult: any = null;
  showVerifyModal = false;
  signResult: any = null;
  showSignModal = false;
  showConfirmModal = false;
  confirmContractId: number = 0;
  errorMessage = "";
  showErrorModal = false;

  Math = Math;

  // Weekly Report
  reportLoading = false;
  showReportModal = false;
  reportSuccesss = false;
  reportMessage = "";

  constructor(
    private contractService: ContractService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private reportsService: ReportsService
  ) {}

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.contractService.getAll().subscribe({
      next: (data) => {
        this.contracts = data;
        this.updatePage(1);
      },
      error: (err) => console.error(err)
    });
  }

  updatePage(page: number): void {
    this.currentPage = page;
    const start = (page - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedContracts = this.contracts.slice(start, end);
  }

  signContract(contractId: number): void {
    this.confirmContractId = contractId;
    this.showConfirmModal = true;
  }

  confirmSign(): void {
    this.showConfirmModal = false;
    this.http.post<any>(`/api/contracts/signature/${this.confirmContractId}/sign?signedBy=Admin`, {}).subscribe({
      next: (result) => {
        this.signResult = result;
        this.showSignModal = true;
        this.loadSignatureStatus(this.confirmContractId);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || "Ce contrat est deja signe ou une erreur est survenue.";
        this.showErrorModal = true;
      }
    });
  }

  verifyContract(contractId: number): void {
    this.http.get<any>(`/api/contracts/signature/${contractId}/verify`).subscribe({
      next: (result) => {
        this.verifyResult = result;
        this.showVerifyModal = true;
      },
      error: () => alert("Error lors de la verification")
    });
  }

  loadSignatureStatus(contractId: number): void {
    this.http.get<any>(`/api/contracts/signature/${contractId}/status`).subscribe({
      next: (status) => {
        this.signatureMap[contractId] = status;
        this.signatureMap = { ...this.signatureMap };
      }
    });
  }

  isSigned(contractId: number): boolean {
    const s = this.signatureMap[contractId];
    if (s) return s.isSigned === true;
    const c = this.contracts.find(x => x.id === contractId);
    return c?.isSigned === true;
  }

  isValidSignature(contractId: number): boolean {
    const s = this.signatureMap[contractId];
    if (s) return !!s.signatureValid;
    const c = this.contracts.find(x => x.id === contractId);
    return !!c?.signatureValid;
  }

  delete(id: number): void {
    if (confirm('Do you want to delete this contract?')) {
      this.contractService.delete(id).subscribe({
        next: () => this.loadContracts(),
        error: (err) => console.error(err)
      });
    }
  }

  sendWeeklyReport(): void {
    this.reportLoading = true;
    this.reportsService.triggerWeeklyReport().subscribe({
      next: (result) => {
        this.reportLoading = false;
        this.reportSuccesss = true;
        this.reportMessage = result.message || "Report sent successfully!";
        this.showReportModal = true;
      },
      error: (err) => {
        this.reportLoading = false;
        this.reportSuccesss = false;
        this.reportMessage = err.error?.message || "Error lors de l'envoi du rapport.";
        this.showReportModal = true;
      }
    });
  }
sendExpirationAlert(): void {
    this.reportLoading = true;
    this.reportsService.triggerExpirationAlert().subscribe({
      next: (result) => {
        this.reportLoading = false;
        this.reportSuccesss = true;
        this.reportMessage = result.message || "Expiration alert sent!";
        this.showReportModal = true;
      },
      error: (err) => {
        this.reportLoading = false;
        this.reportSuccesss = false;
        this.reportMessage = err.error?.message || "Error lors du declenchement de l'alerte.";
        this.showReportModal = true;
      }
    });
  }
}