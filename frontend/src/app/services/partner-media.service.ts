import { Injectable } from '@angular/core';
import { db } from '../firebase.config';
import { collection, query, where, getDocs, doc, getDoc } from 'firebase/firestore';

export interface PartnerMedia {
  partnerId: number;
  description: string;
  photoUrl: string;
  videoUrl: string;
  website: string;
}

@Injectable({ providedIn: 'root' })
export class PartnerMediaService {
  async getByPartnerId(partnerId: number): Promise<PartnerMedia | null> {

    // Méthode 1 — cherche par document ID (ex: document nommé "7")
    const docRef = doc(db, 'partners', String(partnerId));
    const docSnap = await getDoc(docRef);
    if (docSnap.exists()) {
      console.log('✅ Trouvé par document ID:', docSnap.data());
      return docSnap.data() as PartnerMedia;
    }

    // Méthode 2 — cherche par champ partnerId number
    let q = query(collection(db, 'partners'), where('partnerId', '==', partnerId));
    let snapshot = await getDocs(q);
    if (!snapshot.empty) {
      console.log('✅ Trouvé par partnerId number:', snapshot.docs[0].data());
      return snapshot.docs[0].data() as PartnerMedia;
    }

    // Méthode 3 — cherche par champ partnerId string
    q = query(collection(db, 'partners'), where('partnerId', '==', String(partnerId)));
    snapshot = await getDocs(q);
    if (!snapshot.empty) {
      console.log('✅ Trouvé par partnerId string:', snapshot.docs[0].data());
      return snapshot.docs[0].data() as PartnerMedia;
    }

    console.warn('❌ No media found for partnerId:', partnerId);
    return null;
  }
}
