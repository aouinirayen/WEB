import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { DriverService, Driver } from '../../../services/driver.service';

@Component({
  selector: 'app-driver-license-upload',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './driver-license-upload.component.html'
})
export class DriverLicenseUploadComponent implements OnInit {
  driver!: Driver;
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  loading = false;
  result: any = null;
  error = '';
  id!: number;

  constructor(
    private service: DriverService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.params['id'];
    this.service.getById(this.id).subscribe(d => this.driver = d);
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      // Preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  upload() {
    if (!this.selectedFile) return;
    this.loading = true;
    this.error = '';
    this.result = null;

    this.service.uploadLicense(this.id, this.selectedFile)
      .subscribe({
        next: (driver) => {
          this.loading = false;
          this.result = driver;
        },
        error: (err) => {
          this.loading = false;
          this.error = 'Upload failed. Please try again.';
        }
      });
  }
}