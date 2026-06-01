import { Injectable, signal } from '@angular/core';
import fr from '../i18n/fr.json';
import en from '../i18n/en.json';

export type Lang = 'fr' | 'en';

const translations: Record<Lang, any> = { fr, en };

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private _lang = signal<Lang>(
    (localStorage.getItem('fgm_lang') as Lang) || 'fr'
  );

  get lang(): Lang { return this._lang(); }

  /** Accepts Lang or plain string (e.g. from EventEmitter<string>) */
  setLang(lang: Lang | string): void {
    const l: Lang = lang === 'en' ? 'en' : 'fr';
    this._lang.set(l);
    localStorage.setItem('fgm_lang', l);
  }

  t(key: string): string {
    const parts = key.split('.');
    let obj: any = translations[this._lang()];
    for (const p of parts) {
      if (obj == null) return key;
      obj = obj[p];
    }
    return typeof obj === 'string' ? obj : key;
  }
}
