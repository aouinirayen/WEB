import { Injectable } from '@angular/core';
import { storage } from '../firebase.config';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';

@Injectable({ providedIn: 'root' })
export class StorageService {

  async uploadImage(file: File, folder: string, id: number): Promise<string> {
    const ext = file.name.split('.').pop();
    const path = `${folder}/${id}.${ext}`;
    const storageRef = ref(storage, path);
    await uploadBytes(storageRef, file);
    return await getDownloadURL(storageRef);
  }
}
