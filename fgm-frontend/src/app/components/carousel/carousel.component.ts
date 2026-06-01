import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

const SLIDES = [
  {
    img: 'assets/bvmt-building.jpg',
    eyebrow: 'BVMT · Siège Institutionnel',
    title: 'Siège de la Bourse de Tunis',
    desc: 'Le centre névralgique de la cotation tunisienne, aux Berges du Lac de Tunis.',
  },
  {
    img: 'assets/bvmt-data.webp',
    eyebrow: 'BVMT · Cotation Continue',
    title: 'Marché financier en mouvement',
    desc: 'Données temps réel · cotations · indices : la pulsation du marché tunisien.',
  },
  {
    img: 'assets/bvmt-building.jpg',
    eyebrow: 'FGM · Mission de Garantie',
    title: 'Fonds de Garantie de Marché',
    desc: 'Le FGM protège l\'intégrité du marché en couvrant les risques de contrepartie des intermédiaires agréés.',
  },
  {
    img: 'assets/bvmt-data.webp',
    eyebrow: 'FGM · Surveillance Temps Réel',
    title: 'Positions Nettes & Appels de Marge',
    desc: 'Calcul automatisé PNT/PNE · détection des suspens · appels de marge en temps réel.',
  },
];

@Component({
  selector: 'app-carousel',
  standalone: true,
  imports: [CommonModule],
  template: `
<section id="carousel" class="car-section">
  <div class="car-inner">

    <!-- Header -->
    <div class="car-header">
      <p class="car-eyebrow">BVMT × FGM</p>
      <h2 class="car-title">Au cœur du marché financier tunisien</h2>
      <p class="car-sub">Une infrastructure de garantie au service de la stabilité et de l'intégrité des marchés.</p>
    </div>

    <!-- Slider viewport -->
    <div class="car-viewport">
      <div class="car-track" [style.transform]="'translateX(-' + (active * 100) + '%)'">
        <div *ngFor="let s of slides" class="car-slide">
          <div class="car-slide__img">
            <img [src]="s.img" [alt]="s.title" loading="lazy" />
            <div class="car-slide__scrim"></div>
          </div>
          <div class="car-slide__body">
            <span class="car-slide__eyebrow">{{ s.eyebrow }}</span>
            <div class="car-slide__line"></div>
            <h3 class="car-slide__title">{{ s.title }}</h3>
            <p class="car-slide__desc">{{ s.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Controls row -->
    <div class="car-controls">
      <!-- Arrow prev -->
      <button class="car-arrow" (click)="prev()" aria-label="Précédent">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>

      <!-- Dots -->
      <div class="car-dots">
        <button *ngFor="let s of slides; let i = index"
                class="car-dot" [class.car-dot--on]="i === active"
                (click)="goTo(i)" [attr.aria-label]="'Slide ' + (i+1)">
        </button>
      </div>

      <!-- Arrow next -->
      <button class="car-arrow" (click)="next()" aria-label="Suivant">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </button>

      <!-- Counter -->
      <span class="car-counter">{{ active + 1 }} / {{ slides.length }}</span>
    </div>

  </div>
</section>
  `,
  styles: [`
    .car-section {
      background: #f2f0eb;
      padding: 96px 0 80px;
      overflow: hidden;
    }
    .car-inner {
      max-width: 1280px;
      margin: 0 auto;
      padding: 0 40px;
    }

    /* Header */
    .car-header { text-align: center; margin-bottom: 52px; }
    .car-eyebrow {
      font-size: 11px; font-weight: 700;
      letter-spacing: .28em; text-transform: uppercase;
      color: #3d6b52; margin-bottom: 16px;
    }
    .car-title {
      font-size: clamp(1.75rem, 3.5vw, 2.6rem);
      font-weight: 700; line-height: 1.1;
      color: #1c2c1c; margin-bottom: 14px;
    }
    .car-sub {
      font-size: 15px; color: #6b7c6b;
      max-width: 520px; margin: 0 auto; line-height: 1.7;
    }

    /* Viewport — clips the track */
    .car-viewport {
      overflow: hidden;
      border-radius: 16px;
      box-shadow: 0 24px 64px rgba(30,50,30,.12);
    }

    /* Track — holds all slides side-by-side */
    .car-track {
      display: flex;
      transition: transform .55s cubic-bezier(.77,0,.18,1);
      will-change: transform;
    }

    /* Individual slide */
    .car-slide {
      flex: 0 0 100%;
      min-width: 0;
      display: grid;
      grid-template-columns: 1fr 1fr;
      background: #fff;
    }
    @media (max-width: 720px) {
      .car-slide { grid-template-columns: 1fr; }
      .car-slide__img { aspect-ratio: 16/9; }
    }

    .car-slide__img {
      position: relative;
      overflow: hidden;
    }
    .car-slide__img img {
      width: 100%; height: 100%;
      object-fit: cover;
      display: block;
      min-height: 320px;
    }
    .car-slide__scrim {
      position: absolute; inset: 0;
      background: linear-gradient(to right, transparent 60%, rgba(255,255,255,.08));
    }

    .car-slide__body {
      padding: 44px 48px;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
    .car-slide__eyebrow {
      font-size: 10px; font-weight: 700;
      letter-spacing: .22em; text-transform: uppercase;
      color: #3d6b52; margin-bottom: 14px;
    }
    .car-slide__line {
      width: 36px; height: 2px;
      background: linear-gradient(to right, #3d6b52, #6b9b7c);
      border-radius: 1px;
      margin-bottom: 18px;
    }
    .car-slide__title {
      font-size: 1.5rem; font-weight: 700;
      color: #1c2c1c; line-height: 1.25;
      margin-bottom: 14px;
    }
    .car-slide__desc {
      font-size: 14px; color: #5a6b5a; line-height: 1.7;
    }

    /* Controls */
    .car-controls {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 20px;
      margin-top: 28px;
    }
    .car-arrow {
      width: 40px; height: 40px;
      border-radius: 50%;
      border: 1px solid rgba(61,107,82,.25);
      background: rgba(61,107,82,.07);
      color: #3d6b52;
      display: flex; align-items: center; justify-content: center;
      cursor: pointer;
      transition: all .2s;
    }
    .car-arrow:hover {
      background: #3d6b52;
      border-color: #3d6b52;
      color: #fff;
    }
    .car-dots { display: flex; gap: 8px; }
    .car-dot {
      width: 8px; height: 8px;
      border-radius: 50%;
      border: none;
      background: rgba(61,107,82,.25);
      cursor: pointer;
      transition: all .25s;
      padding: 0;
    }
    .car-dot--on {
      background: #3d6b52;
      width: 24px;
      border-radius: 4px;
    }
    .car-counter {
      font-size: 12px; font-weight: 700;
      color: #9aaa9a;
      letter-spacing: .05em;
      min-width: 36px;
    }
  `],
})
export class CarouselComponent implements OnInit, OnDestroy {
  slides = SLIDES;
  active = 0;
  private _timer: any;

  ngOnInit(): void { this._timer = setInterval(() => this.next(), 5000); }
  ngOnDestroy(): void { clearInterval(this._timer); }

  next(): void { this.active = (this.active + 1) % this.slides.length; }
  prev(): void { this.active = (this.active - 1 + this.slides.length) % this.slides.length; }
  goTo(i: number): void { this.active = i; }
}
