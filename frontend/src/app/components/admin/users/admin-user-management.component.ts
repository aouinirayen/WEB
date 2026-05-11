import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { UserDto, UserFilterParams, UserService } from '../../../services/admin/user.service';
import { ToastService } from '../../../services/shared/toast.service';

@Component({
  selector: 'app-admin-user-management',
  templateUrl: './admin-user-management.component.html',
  styleUrls: ['./admin-user-management.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminUserManagementComponent implements OnInit {
  users: UserDto[] = [];
  loading = false;
  errorMessage = '';
  selectedIds = new Set<number>();
  activeFilters: string[] = [];
  showUserModal = false;
  showConfirmModal = false;
  showQuickView = false;
  showBanModal = false;
  userModalMode: 'create' | 'edit' | 'view' = 'create';
  selectedUser?: UserDto;
  confirmTitle = 'Confirm deletion';
  confirmMessage = 'This action cannot be undone.';
  confirmAction: 'deleteSingle' | 'deleteSelected' = 'deleteSelected';
  confirmTargetId?: number;
  banUserTarget?: UserDto;

  filters: UserFilterParams = {
    page: 0,
    size: 10,
    keyword: '',
    role: 'ALL',
    status: 'all',
    sortBy: 'createdAt',
    sortDir: 'desc'
  };

  stats = {
    total: 0,
    active: 0,
    admins: 0,
    agents: 0,
    operators: 0,
    passengers: 0
  };

  public Math = Math;

  constructor(
    private userService: UserService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  get pageIndex(): number {
    return this.filters.page ?? 0;
  }

  get pageSize(): number {
    return this.filters.size ?? 10;
  }

  get pageStart(): number {
    return this.pageIndex * this.pageSize + 1;
  }

  get pageEnd(): number {
    return Math.min(this.stats.total, (this.pageIndex + 1) * this.pageSize);
  }

  getSelectedUser(): UserDto | undefined {
    return this.users.find(user => this.selectedIds.has(user.id));
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.userService.getUsers(this.filters).subscribe({
      next: response => {
        // Filter out admin users
        this.users = (response.content || []).filter(user => user.role !== 'ADMIN');

        // Calculate stats excluding admins
        this.stats.total = this.users.length;
        this.stats.active = this.users.filter(user => user.enabled).length;
        this.stats.admins = (response.content || []).filter(user => user.role === 'ADMIN').length; // Show actual admin count but don't include in total
        this.stats.agents = this.users.filter(user => user.role === 'AGENT').length;
        this.stats.operators = this.users.filter(user => user.role === 'OPERATOR').length;
        this.stats.passengers = this.users.filter(user => user.role === 'PASSENGER').length;
        this.loading = false;
        this.cdr.markForCheck();
        // Load photos for all users
        this.loadPhotosForUsers();
      },
      error: () => {
        this.errorMessage = 'Unable to load users. Please try again later.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadPhotosForUsers(): void {
    // Load photos for each user (don't block UI)
    // Only load if photoUrl is not already set from backend
    this.users.forEach(user => {
      if (user.id && !user.photoUrl) {
        this.userService.loadUserPhotoUrl(user).then(photoUrl => {
          if (photoUrl) {
            user.photoUrl = photoUrl;
            this.cdr.markForCheck();
          }
        }).catch(() => {
          // Silently ignore photo loading errors - user doesn't have a photo
        });
      }
    });
  }

  searchUsers(): void {
    this.filters.page = 0;
    this.updateChips();
    this.loadUsers();
  }

  applyRoleFilter(role: string): void {
    this.filters.role = role;
    this.filters.page = 0;
    this.searchUsers();
  }

  updateChips(): void {
    const chips: string[] = [];
    if (this.filters.keyword) {
      chips.push(`Search: ${this.filters.keyword}`);
    }
    if (this.filters.role && this.filters.role !== 'ALL') {
      chips.push(`Role: ${this.filters.role}`);
    }
    if (this.filters.status && this.filters.status !== 'all') {
      chips.push(`Status: ${this.filters.status}`);
    }
    this.activeFilters = chips;
  }

  removeFilter(filter: string): void {
    if (filter.startsWith('Search:')) {
      this.filters.keyword = '';
    }
    if (filter.startsWith('Role:')) {
      this.filters.role = 'ALL';
    }
    if (filter.startsWith('Status:')) {
      this.filters.status = 'all';
    }
    this.searchUsers();
  }

  toggleSelectAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.users.forEach(user => this.selectedIds.add(user.id!));
    } else {
      this.selectedIds.clear();
    }
    this.cdr.markForCheck();
  }

  toggleSelection(userId: number): void {
    if (this.selectedIds.has(userId)) {
      this.selectedIds.delete(userId);
    } else {
      this.selectedIds.add(userId);
    }
    this.cdr.markForCheck();
  }

  get selectedCount(): number {
    return this.selectedIds.size;
  }

  openUserModal(mode: 'create' | 'edit' | 'view', user?: UserDto): void {
    this.userModalMode = mode;

    if (mode === 'edit' && user?.id) {
      // Fetch complete user details to ensure all fields are populated
      this.userService.getUserById(user.id).subscribe({
        next: completeUser => {
          this.selectedUser = completeUser;
          // photoUrl is already included in the user response from backend
          // Only try to load photo if photoUrl is not in response and user has photo
          if (!completeUser.photoUrl && completeUser.id) {
            this.userService.loadUserPhotoUrl(completeUser).then(photoUrl => {
              if (photoUrl) {
                completeUser.photoUrl = photoUrl;
              }
              this.showUserModal = true;
              this.cdr.markForCheck();
            }).catch(() => {
              // Photo loading failed, but continue with modal
              this.showUserModal = true;
              this.cdr.markForCheck();
            });
          } else {
            // photoUrl is already in response or no photo exists
            this.showUserModal = true;
            this.cdr.markForCheck();
          }
        },
        error: () => {
          this.toastService.error('Error', 'Unable to load user details.');
        }
      });
    } else {
      this.selectedUser = user;
      this.showUserModal = true;
    }
  }

  closeUserModal(): void {
    this.showUserModal = false;
    this.selectedUser = undefined;
  }

  onUserSaved(payload: { userData: any; photoFile?: File }): void {
    if (this.userModalMode === 'create') {
      this.userService.createUser(payload.userData).subscribe({
        next: user => {
          if (payload.photoFile) {
            this.userService.uploadUserPhoto(user.id!, payload.photoFile).subscribe();
          }
          this.toastService.success('User created', `${user.username} has been added successfully.`);
          this.closeUserModal();
          this.loadUsers();
        },
        error: () => {
          this.toastService.error('Create failed', 'Unable to create the user.');
        }
      });
      return;
    }

    if (this.selectedUser?.id) {
      this.userService.updateUser(this.selectedUser.id, payload.userData).subscribe({
        next: user => {
          if (payload.photoFile) {
            this.userService.uploadUserPhoto(user.id!, payload.photoFile).subscribe();
          }
          this.toastService.success('User updated', `${user.username} has been updated.`);
          this.closeUserModal();
          this.loadUsers();
        },
        error: () => {
          this.toastService.error('Update failed', 'Unable to update the user.');
        }
      });
    }
  }

  openConfirmDelete(user: UserDto): void {
    this.confirmAction = 'deleteSingle';
    this.confirmTargetId = user.id;
    this.confirmTitle = 'Delete user';
    this.confirmMessage = `Delete ${user.username} permanently? This cannot be undone.`;
    this.showConfirmModal = true;
  }

  openBulkDelete(): void {
    this.confirmAction = 'deleteSelected';
    this.confirmTargetId = undefined;
    this.confirmTitle = 'Delete selected users';
    this.confirmMessage = `Delete ${this.selectedCount} selected users? This action is irreversible.`;
    this.showConfirmModal = true;
  }

  closeConfirmModal(): void {
    this.showConfirmModal = false;
  }

  executeConfirm(): void {
    if (this.confirmAction === 'deleteSingle' && this.confirmTargetId != null) {
      this.userService.deleteUser(this.confirmTargetId).subscribe({
        next: () => {
          this.toastService.success('Deleted', 'User has been removed.');
          this.showConfirmModal = false;
          this.loadUsers();
        },
        error: () => {
          this.toastService.error('Delete failed', 'Unable to remove the user.');
        }
      });
      return;
    }

    if (this.confirmAction === 'deleteSelected') {
      this.userService.deleteUsers(Array.from(this.selectedIds)).subscribe({
        next: () => {
          this.toastService.success('Bulk delete', 'Selected users have been removed.');
          this.selectedIds.clear();
          this.showConfirmModal = false;
          this.loadUsers();
        },
        error: () => {
          this.toastService.error('Delete failed', 'Unable to remove the selected users.');
        }
      });
    }
  }

  changeRoleInline(user: UserDto, role: string): void {
    this.userService.updateUserRole(user.id!, role).subscribe({
      next: updatedUser => {
        user.role = updatedUser.role;
        this.toastService.success('Role updated', `${updatedUser.username} is now ${updatedUser.role}.`);
        this.cdr.markForCheck();
      },
      error: () => {
        this.toastService.error('Update failed', 'Unable to change role.');
      }
    });
  }

  toggleStatus(user: UserDto): void {
    const newStatus = !user.enabled;

    if (newStatus) {
      // Enabling user = Unbanning
      this.userService.unbanUser(user.id!).subscribe({
        next: updatedUser => {
          user.enabled = updatedUser.enabled;
          user.inactivatedUntil = updatedUser.inactivatedUntil;
          this.toastService.info('Status updated', `${updatedUser.username} has been activated.`);
          this.cdr.markForCheck();
        },
        error: () => {
          this.toastService.error('Update failed', 'Unable to activate user.');
        }
      });
    } else {
      // Disabling user = Open ban duration selector
      this.openBanModal(user);
    }
  }

  openBanModal(user: UserDto): void {
    this.banUserTarget = user;
    this.showBanModal = true;
  }

  closeBanModal(): void {
    this.showBanModal = false;
    this.banUserTarget = undefined;
  }

  banUserForDuration(durationDays: number | null): void {
    if (!this.banUserTarget?.id) return;

    const durationText = durationDays === null ? 'permanently' : `for ${durationDays} day${durationDays !== 1 ? 's' : ''}`;

    this.userService.banUser(this.banUserTarget.id, durationDays).subscribe({
      next: updatedUser => {
        this.banUserTarget!.enabled = updatedUser.enabled;
        this.banUserTarget!.inactivatedUntil = updatedUser.inactivatedUntil;
        this.toastService.success('User deactivated', `${updatedUser.username} has been deactivated ${durationText}.`);
        this.closeBanModal();
        this.cdr.markForCheck();
      },
      error: () => {
        this.toastService.error('Deactivate failed', 'Unable to deactivate the user.');
      }
    });
  }

  isUserBanned(user: UserDto): boolean {
    return !user.enabled;
  }

  getBanExpiryText(inactivatedUntil?: string): string {
    if (!inactivatedUntil) return '—';
    const date = new Date(inactivatedUntil);
    const now = new Date();
    if (date <= now) {
      return 'Expired';
    }
    return `Until ${date.toLocaleDateString()}`;
  }

  sortBy(column: string): void {
    if (this.filters.sortBy === column) {
      this.filters.sortDir = this.filters.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.filters.sortBy = column;
      this.filters.sortDir = 'asc';
    }
    this.loadUsers();
  }

  changePage(page: number): void {
    if (page < 0 || page > this.getLastPageIndex()) {
      return;
    }
    this.filters.page = page;
    this.loadUsers();
  }

  changePageSize(size: number): void {
    this.filters.size = size;
    this.filters.page = 0;
    this.loadUsers();
  }

  getLastPageIndex(): number {
    return Math.max(0, Math.ceil(this.stats.total / this.filters.size!) - 1);
  }

  exportCsv(): void {
    this.userService.exportUsers(this.filters).subscribe({
      next: blob => this.downloadBlob(blob, 'users-export.csv'),
      error: () => this.toastService.error('Export failed', 'Unable to export users.')
    });
  }

  downloadBlob(blob: Blob, filename: string): void {
    const objectUrl = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = objectUrl;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(objectUrl);
  }

  showQuickDetails(user: UserDto): void {
    this.selectedUser = user;
    this.showQuickView = true;
  }

  closeQuickView(): void {
    this.showQuickView = false;
    this.selectedUser = undefined;
  }

  formatDate(value?: string): string {
    if (!value) {
      return '—';
    }
    return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value));
  }

  trackByUser(index: number, user: UserDto): number {
    return user.id!;
  }

  getRoleTagClass(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'cell-tag-orange';
      case 'AGENT':
        return 'cell-tag-blue';
      case 'OPERATOR':
        return 'cell-tag-green';
      case 'PASSENGER':
        return 'cell-tag-blue';
      default:
        return 'cell-tag-blue';
    }
  }
}
