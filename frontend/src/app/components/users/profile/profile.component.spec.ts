import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ProfileComponent } from './profile.component';
import { ProfileService } from '../../../services/shared/profile.service';
import { ToastService } from '../../../services/shared/toast.service';
import { of, throwError } from 'rxjs';
import { ProfileResponse } from '../../../models/profile.model';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let profileService: jasmine.SpyObj<ProfileService>;
  let toastService: jasmine.SpyObj<ToastService>;

  const mockProfile: ProfileResponse = {
    id: 1,
    username: 'testuser',
    email: 'test@example.com',
    name: 'Test User',
    cin: 12345678,
    role: 'AGENT',
    photoContentType: 'image/jpeg',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  beforeEach(async () => {
    const profileServiceSpy = jasmine.createSpyObj('ProfileService', [
      'getCurrentProfile',
      'updateProfile',
      'uploadPhoto',
      'getPhoto',
      'deletePhoto'
    ]);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', [
      'success',
      'error',
      'warning',
      'info'
    ]);

    await TestBed.configureTestingModule({
      declarations: [ProfileComponent],
      imports: [ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        { provide: ProfileService, useValue: profileServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy }
      ]
    }).compileComponents();

    profileService = TestBed.inject(ProfileService) as jasmine.SpyObj<ProfileService>;
    toastService = TestBed.inject(ToastService) as jasmine.SpyObj<ToastService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    profileService.getCurrentProfile.and.returnValue(of(mockProfile));
    profileService.getPhoto.and.returnValue(
      of(new Blob(['photo data'], { type: 'image/jpeg' }))
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load profile on init', (done) => {
    fixture.detectChanges();
    expect(profileService.getCurrentProfile).toHaveBeenCalled();
    expect(component.profile).toEqual(mockProfile);
    done();
  });

  it('should enable editing mode', () => {
    component.profile = mockProfile;
    component.editing = false;
    component.startEditing();
    expect(component.editing).toBe(true);
  });

  it('should cancel editing and revert changes', () => {
    component.profile = mockProfile;
    component.editing = true;
    component.cancelEditing();
    expect(component.editing).toBe(false);
  });

  it('should update username when profile is updated', (done) => {
    component.profile = mockProfile;
    component.profileForm.patchValue({
      username: 'newusername',
      email: 'test@example.com',
      name: 'Test User',
      cin: 12345678
    });

    const updatedProfile = { ...mockProfile, username: 'newusername' };
    profileService.updateProfile.and.returnValue(of(updatedProfile));

    component.saveProfile();
    expect(profileService.updateProfile).toHaveBeenCalledWith(
      jasmine.objectContaining({ username: 'newusername' })
    );
    done();
  });

  it('should validate CIN - reject if not exactly 8 digits', () => {
    fixture.detectChanges();
    const cinControl = component.profileForm.get('cin');

    // Test invalid CIN (7 digits)
    cinControl?.setValue('1234567');
    expect(cinControl?.hasError('invalidCin')).toBe(true);

    // Test invalid CIN (9 digits)
    cinControl?.setValue('123456789');
    expect(cinControl?.hasError('invalidCin')).toBe(true);

    // Test invalid CIN (non-numeric)
    cinControl?.setValue('1234567a');
    expect(cinControl?.hasError('invalidCin')).toBe(true);
  });

  it('should validate CIN - accept if exactly 8 digits', () => {
    fixture.detectChanges();
    const cinControl = component.profileForm.get('cin');

    cinControl?.setValue('12345678');
    expect(cinControl?.hasError('invalidCin')).toBe(false);
  });

  it('should validate file type for photo upload', () => {
    const invalidFile = new File(['test'], 'test.txt', { type: 'text/plain' });
    const event = new Event('change');
    Object.defineProperty(event.target, 'files', { value: [invalidFile] });
    component.onFileSelected(event as any);
    expect(toastService.error).toHaveBeenCalledWith(
      'Error',
      'Only JPEG, PNG, GIF, and WebP images are allowed'
    );
  });

  it('should validate file size for photo upload', () => {
    const largeFile = new File(['x'.repeat(6 * 1024 * 1024)], 'large.jpg', {
      type: 'image/jpeg'
    });
    const event = new Event('change');
    Object.defineProperty(event.target, 'files', { value: [largeFile] });
    component.onFileSelected(event as any);
    expect(toastService.error).toHaveBeenCalledWith(
      'Error',
      'File size exceeds maximum limit of 5MB'
    );
  });

  it('should upload profile photo', (done) => {
    component.profile = mockProfile;
    const file = new File(['photo'], 'photo.jpg', { type: 'image/jpeg' });
    component.selectedFile = file;
    component.photoSelected = true;

    profileService.uploadPhoto.and.returnValue(of(mockProfile));

    component.uploadPhoto();
    expect(profileService.uploadPhoto).toHaveBeenCalledWith(file);
    expect(toastService.success).toHaveBeenCalledWith(
      'Success',
      'Profile photo uploaded successfully'
    );
    done();
  });

  it('should delete profile photo', (done) => {
    component.profile = mockProfile;
    spyOn(window, 'confirm').and.returnValue(true);

    profileService.deletePhoto.and.returnValue(of(mockProfile));

    component.deletePhoto();
    expect(profileService.deletePhoto).toHaveBeenCalled();
    expect(toastService.success).toHaveBeenCalledWith(
      'Success',
      'Profile photo deleted successfully'
    );
    done();
  });
});
