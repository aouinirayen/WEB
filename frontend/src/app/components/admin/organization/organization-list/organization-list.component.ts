import { Component, OnInit } from '@angular/core';
import { Organization } from '../../../../models/organization-partner/organization';
import { OrganizationService } from '../../../../services/organization.service';

@Component({
  selector: 'app-organization-list',
  templateUrl: './organization-list.component.html',
  styleUrl: './organization-list.component.scss'
})
export class OrganizationListComponent implements OnInit {

  organizations: Organization[] = [];
  filteredOrganizations: Organization[] = [];
  paginatedOrganizations: Organization[] = [];
  currentPage = 1;
  itemsPerPage = 5;
  searchTerm = '';
  filterStatus = '';
  filterType = '';

  Math = Math;

  constructor(private organizationService: OrganizationService) {}

  ngOnInit(): void {
    this.loadOrganizations();
  }

  loadOrganizations(): void {
    this.organizationService.getAll().subscribe({
      next: (data) => {
        this.organizations = data;
        this.applyFilters();
      },
      error: (err) => console.error(err)
    });
  }

  applyFilters(): void {
    this.filteredOrganizations = this.organizations.filter(org => {
      const matchSearch = !this.searchTerm ||
        org.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        org.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        org.acronyme.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatus = !this.filterStatus || org.status === this.filterStatus;
      const matchType = !this.filterType || org.type === this.filterType;
      return matchSearch && matchStatus && matchType;
    });
    this.updatePage(1);
  }

  updatePage(page: number): void {
    this.currentPage = page;
    const start = (page - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedOrganizations = this.filteredOrganizations.slice(start, end);
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.filterStatus = '';
    this.filterType = '';
    this.applyFilters();
  }

  delete(id: number): void {
    if (confirm('Do you want to delete this organization?')) {
      this.organizationService.delete(id).subscribe({
        next: () => this.loadOrganizations(),
        error: (err) => console.error(err)
      });
    }
  }
}







