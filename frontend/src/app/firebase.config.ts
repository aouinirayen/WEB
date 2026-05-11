import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
  apiKey: 'AIzaSyBQGu3Wx-_bcBIn1cLYOfoMfTv7YsXtK70',
  authDomain: 'transittn-5eeda.firebaseapp.com',
  projectId: 'transittn-5eeda',
  storageBucket: 'transittn-5eeda.firebasestorage.app',
  messagingSenderId: '618235972199',
  appId: '1:618235972199:web:0e3ea7d99261247d8c62c2'
};

export const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const storage = getStorage(app);
