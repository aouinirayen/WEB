import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-map',


  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements OnChanges {

  @Input() zones: any[] = [];
  @Input() mapId = 'map';

  mapUrl?: SafeResourceUrl;

  private governorateCoords: { [key: string]: [number, number] } = {
    'Tunis': [36.8065, 10.1815],
    'Ariana': [36.8625, 10.1956],
    'Ben Arous': [36.7535, 10.2282],
    'Manouba': [36.8080, 10.0983],
    'Nabeul': [36.4513, 10.7357],
    'Bizerte': [37.2744, 9.8739],
    'Beja': [36.7333, 9.1833],
    'Jendouba': [36.5011, 8.7803],
    'Kef': [36.1822, 8.7149],
    'Siliana': [36.0843, 9.3708],
    'Sousse': [35.8256, 10.6369],
    'Monastir': [35.7643, 10.8113],
    'Mahdia': [35.5047, 11.0622],
    'Sfax': [34.7406, 10.7603],
    'Kairouan': [35.6781, 10.0963],
    'Kasserine': [35.1676, 8.8365],
    'Sidi Bouzid': [35.0382, 9.4849],
    'Gabes': [33.8881, 10.0975],
    'Mednine': [33.3549, 10.5055],
    'Tataouine': [32.9211, 10.4516],
    'Gafsa': [34.4250, 8.7842],
    'Tozeur': [33.9197, 8.1335],
    'Kebili': [33.7042, 8.9689],
    'Zaghouan': [36.4029, 10.1429]
  };

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['zones'] && this.zones.length > 0) {
      this.buildMapUrl();
    }
  }

  buildMapUrl(): void {
     const firstZone = this.zones[0];
     const coords = this.governorateCoords[firstZone.governorate];
     if (coords) {
       const [lat, lng] = coords;
       const offset = 0.001;
       const url = `https://www.openstreetmap.org/export/embed.html?bbox=${lng - offset},${lat - offset},${lng + offset},${lat + offset}&layer=mapnik&marker=${lat},${lng}`;
       this.mapUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
     }
   }
}



