import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

const pillars = [
  {
    icon: '🛡️',
    title: 'Risque couvert',
    desc: 'Défaut titres (vendeur) et défaut espèces (acheteur). Le FGM achète ou vend sur le marché pour livrer la contrepartie au prix initial.',
  },
  {
    icon: '🔀',
    title: 'Compensation (Netting)',
    desc: 'Calcul par broker et par valeur d\'une Position Nette Titres (PNT) et d\'une Position Nette Espèces (PNE) afin d\'identifier le montant de risque.',
  },
  {
    icon: '📅',
    title: 'Cycle J+2',
    desc: 'Le délai de règlement-livraison sur le marché est de J+2. Le risque est ré-estimé chaque jour avec un coefficient majorateur de 6%.',
  },
  {
    icon: '📊',
    title: 'Surveillance temps réel',
    desc: 'Détection automatique des anomalies, suivi continu des positions et génération des appels de marge.',
  },
];

const missionPoints = [
  'Protéger les investisseurs et les épargnants tunisiens.',
  'Garantir le bon dénouement des transactions sur la BVMT.',
  'Préserver la confiance et la stabilité du marché financier.',
  'Réagir immédiatement en cas de défaillance d\'un intermédiaire.',
];

const impactItems = [
  { value: '100%', label: 'Transparence', desc: 'Toutes les opérations couvertes sont suivies et tracées.' },
  { value: 'J+2',  label: 'Réactivité',   desc: 'Le règlement-livraison est garanti en deux jours ouvrés.' },
  { value: '24/7', label: 'Confiance',    desc: 'Une surveillance continue au service de la stabilité du marché.' },
];

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  template: `
<section id="about" class="fgm-about" aria-labelledby="about-title">
  <div class="fgm-about__inner">
    <div style="text-align:center;max-width:768px;margin:0 auto">
      <span class="fgm-section-eyebrow">À propos du FGM</span>
      <h2 id="about-title" class="fgm-section-title">
        Le mécanisme central de protection du marché
      </h2>
      <p class="fgm-section-desc" style="margin:20px auto 0">
        Le Fonds de Garantie de Marché garantit la bonne fin des opérations réalisées
        entre intermédiaires sur le marché financier tunisien. En cas de défaillance,
        le FGM se substitue à l'intermédiaire pour assurer le dénouement normal de la transaction.
      </p>
    </div>

    <!-- 4 pillars -->
    <div class="fgm-pillars">
      <article *ngFor="let p of pillars" class="fgm-pillar">
        <div class="fgm-pillar__icon">{{ p.icon }}</div>
        <h3>{{ p.title }}</h3>
        <p>{{ p.desc }}</p>
      </article>
    </div>

    <!-- Mission + Impact -->
    <div class="fgm-mission-grid">
      <!-- Mission card -->
      <div class="fgm-mission-card">
        <div class="fgm-mission-card__glow"></div>
        <div style="position:relative">
          <span style="font-size:32px">✨</span>
          <h3>Notre mission</h3>
          <p>
            Le FGM agit comme un filet de sécurité collectif : il assure que chaque
            opération boursière se conclut normalement, même si un intermédiaire
            rencontre une difficulté inattendue.
          </p>
          <ul class="fgm-mission-list">
            <li *ngFor="let point of missionPoints">
              <span class="fgm-mission-list__dot"></span>
              <span>{{ point }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Impact card -->
      <div class="fgm-impact-card">
        <h3>Un impact concret</h3>
        <p>Trois engagements simples qui font du FGM un pilier du marché tunisien.</p>
        <div class="fgm-impact-items">
          <div *ngFor="let it of impactItems" class="fgm-impact-item">
            <div class="fgm-impact-item__value">{{ it.value }}</div>
            <div class="fgm-impact-item__label">{{ it.label }}</div>
            <div class="fgm-impact-item__desc">{{ it.desc }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
  `,
})
export class AboutComponent {
  pillars = pillars;
  missionPoints = missionPoints;
  impactItems = impactItems;
}
