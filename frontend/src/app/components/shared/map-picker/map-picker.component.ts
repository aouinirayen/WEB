import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-map-picker',
  templateUrl: './map-picker.component.html',
  styleUrl: './map-picker.component.scss'
})
export class MapPickerComponent {
  @Input() governorate?: string;
  @Input() mapId?: string;
  @Input() searchQuery?: string;
  @Output() locationSelected = new EventEmitter<any>();

  selectedGovernorate?: string;
  mapImageUrl?: string;
  mapLink?: string;
  selectedCoords = '';

  governorates = [
    'Tunis', 'Ariana', 'Ben Arous', 'Manouba', 'Nabeul',
    'Zaghouan', 'Bizerte', 'Beja', 'Jendouba', 'Kef',
    'Siliana', 'Sousse', 'Monastir', 'Mahdia', 'Sfax',
    'Kairouan', 'Kasserine', 'Sidi Bouzid', 'Gabes',
    'Mednine', 'Tataouine', 'Gafsa', 'Tozeur', 'Kebili'
  ];

  private coords: { [key: string]: [number, number] } = {
    'Tunis': [36.8065, 10.1815], 'Ariana': [36.8625, 10.1956],
    'Ben Arous': [36.7535, 10.2282], 'Manouba': [36.8080, 10.0983],
    'Nabeul': [36.4513, 10.7357], 'Zaghouan': [36.4029, 10.1429],
    'Bizerte': [37.2744, 9.8739], 'Beja': [36.7333, 9.1833],
    'Jendouba': [36.5011, 8.7803], 'Kef': [36.1822, 8.7149],
    'Siliana': [36.0843, 9.3708], 'Sousse': [35.8256, 10.6369],
    'Monastir': [35.7643, 10.8113], 'Mahdia': [35.5047, 11.0622],
    'Sfax': [34.7406, 10.7603], 'Kairouan': [35.6781, 10.0963],
    'Kasserine': [35.1676, 8.8365], 'Sidi Bouzid': [35.0382, 9.4849],
    'Gabes': [33.8881, 10.0975], 'Mednine': [33.3549, 10.5055],
    'Tataouine': [32.9211, 10.4516], 'Gafsa': [34.4250, 8.7842],
    'Tozeur': [33.9197, 8.1335], 'Kebili': [33.7042, 8.9689]
  };

  selectGovernorate(gov: string): void {
    this.selectedGovernorate = gov;
    const c = this.coords[gov];
    if (c) {
      this.selectedCoords = c[0] + ', ' + c[1];
      // Image statique OpenStreetMap
      this.mapImageUrl = 'https://staticmap.openstreetmap.de/staticmap.php?center='
        + c[0] + ',' + c[1] + '&zoom=13&size=800x400&maptype=mapnik&markers='
        + c[0] + ',' + c[1] + ',red-pushpin';
      // Lien vers la carte interactive
      this.mapLink = 'https://www.openstreetmap.org/?mlat=' + c[0] + '&mlon=' + c[1] + '#map=13/' + c[0] + '/' + c[1];
      this.locationSelected.emit({ lat: c[0], lng: c[1], name: gov });
    }
  }
}