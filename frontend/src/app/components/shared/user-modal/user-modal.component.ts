import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserCreatePayload, UserDto, UserUpdatePayload } from '../../../services/admin/user.service';

export interface UserModalPayload {
  userData: UserCreatePayload | UserUpdatePayload;
  photoFile?: File;
}

@Component({
  selector: 'app-user-modal',
  templateUrl: './user-modal.component.html',
  styleUrls: ['./user-modal.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserModalComponent implements OnChanges {
  @Input() isOpen = false;
  @Input() mode: 'create' | 'edit' | 'view' = 'create';
  @Input() user?: UserDto;
  @Input() loading = false;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<UserModalPayload>();

  form: FormGroup;
  avatarPreview = '';
  selectedFile?: File;
  fileError = '';
  passwordStrength = 0;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      name: ['', [Validators.required, Validators.minLength(2)]],
      cin: [''],
      password: [''],
      role: ['PASSENGER', [Validators.required]],
      enabled: [true]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['user'] && this.user) {
      this.form.patchValue({
        username: this.user.username,
        email: this.user.email,
        name: this.user.name || '',
        cin: this.user.cin || '',
        role: this.user.role,
        enabled: this.user.enabled ?? true
      });
      // Show existing photo in edit mode if available
      this.avatarPreview = this.user.photoUrl || '';
    }

    if (changes['mode'] && this.mode === 'create') {
      this.form.patchValue({ password: '' });
      this.selectedFile = undefined;
      this.avatarPreview = '';
    }

    if (changes['isOpen'] && !this.isOpen) {
      this.resetForm();
    }
  }

  get title(): string {
    return this.mode === 'create' ? 'Add New User' : 'Edit User';
  }

  get passwordFieldVisible(): boolean {
    return this.mode === 'create';
  }

  resetForm(): void {
    if (this.mode === 'create') {
      this.form.reset({ role: 'PASSENGER', enabled: true });
      this.avatarPreview = '';
      this.selectedFile = undefined;
      this.fileError = '';
      this.passwordStrength = 0;
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.handleFile(file);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    this.handleFile(file);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  private handleFile(file: File | undefined): void {
    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.fileError = 'Please upload a valid image file.';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.fileError = 'Maximum file size is 5MB.';
      return;
    }

    this.fileError = '';
    this.selectedFile = file;
    const reader = new FileReader();
    reader.onload = () => {
      this.avatarPreview = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  onPasswordInput(value: string): void {
    this.passwordStrength = Math.min(100, value.length * 15);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: UserCreatePayload | UserUpdatePayload = {
      username: this.form.value.username,
      email: this.form.value.email,
      name: this.form.value.name,
      role: this.form.value.role,
      enabled: this.form.value.enabled
    };

    if (this.form.value.cin) {
      (payload as any).cin = parseInt(this.form.value.cin, 10);
    }

    if (this.mode === 'create') {
      (payload as UserCreatePayload).password = this.form.value.password;
    }

    this.save.emit({ userData: payload, photoFile: this.selectedFile });
  }

  closeModal(): void {
    this.close.emit();
  }
}
