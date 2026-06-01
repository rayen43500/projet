import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

const facts = [
  { icon: '🏛️', value: '1969',     label: 'Création',         desc: 'Fondation de la Bourse de Tunis, modernisée en 1995.' },
  { icon: '🏢', value: '80+',      label: 'Sociétés cotées',   desc: 'Banques, assurances, industrie, télécoms et services.' },
  { icon: '👥', value: '28',       label: 'Intermédiaires',    desc: 'Sociétés de bourse agréées opérant sur le marché.' },
  { icon: '📈', value: 'TUNINDEX', label: 'Indice phare',      desc: 'L\'indice de référence du marché tunisien depuis 1998.' },
];

const indices = [
  { name: 'TUNINDEX',   value: '10 248,32', var: '+0,84%', up: true },
  { name: 'TUNINDEX20', value: '4 612,17',  var: '+1,12%', up: true },
  { name: 'TUN BANKS',  value: '2 184,55',  var: '−0,21%', up: false },
];

const movers = [
  { name: 'BIAT',     price: '112,50', var: '+3,21%', up: true },
  { name: 'SFBT',     price: '18,75',  var: '+2,40%', up: true },
  { name: 'ATTIJARI', price: '44,20',  var: '+1,80%', up: true },
  { name: 'POULINA',  price: '9,15',   var: '−1,42%', up: false },
];

const dashboardItems = [
  { title: 'Positions nettes',       desc: 'Suivi des positions par intermédiaire et par valeur.' },
  { title: 'Risque de marché',       desc: 'Évaluation continue de l\'exposition au risque.' },
  { title: 'Alertes du jour',        desc: 'Notifications immédiates en cas d\'anomalie.' },
  { title: 'Historique des séances', desc: 'Consultation des journées passées et tendances.' },
];

@Component({
  selector: 'app-market-overview',
  standalone: true,
  imports: [CommonModule],
  template: `
<section id="market" class="fgm-market" aria-labelledby="market-title">
  <div class="fgm-market__inner">
    <div style="text-align:center;max-width:768px;margin:0 auto">
      <span class="fgm-section-eyebrow">Marché tunisien</span>
      <h2 id="market-title" class="fgm-section-title">Le marché financier tunisien en bref</h2>
      <p class="fgm-section-desc" style="margin:20px auto 0">
        La Bourse des Valeurs Mobilières de Tunis (BVMT) est le cœur du marché financier
        tunisien. Elle accueille les plus grandes entreprises du pays et structure l'épargne nationale.
      </p>
    </div>

    <!-- Facts -->
    <div class="fgm-facts-grid">
      <div *ngFor="let f of facts" class="fgm-fact-card">
        <div class="fgm-fact-card__top">
          <span class="fgm-fact-card__icon">{{ f.icon }}</span>
          <span class="fgm-fact-card__value">{{ f.value }}</span>
        </div>
        <p class="fgm-fact-card__label">{{ f.label }}</p>
        <p class="fgm-fact-card__desc">{{ f.desc }}</p>
      </div>
    </div>

    <!-- Dashboard preview -->
    <div class="fgm-market-preview">
      <div class="fgm-market-preview__header">
        <div>
          <h3>Aperçu du tableau de bord</h3>
          <p>Une vision claire et instantanée du marché : indices, valeurs en mouvement et alertes du jour.</p>
        </div>
        <span class="fgm-live-badge">
          <span class="fgm-live-badge__dot"></span>
          Temps réel
        </span>
      </div>

      <div class="fgm-market-cards">
        <!-- Indices -->
        <div class="fgm-market-card">
          <div class="fgm-market-card__header">
            <span class="fgm-market-card__title">Indices BVMT</span>
            <span class="fgm-market-card__badge">Live</span>
          </div>
          <div class="fgm-market-card__rows">
            <div *ngFor="let idx of indices" class="fgm-market-card__row">
              <div>
                <p class="fgm-index-name">{{ idx.name }}</p>
                <p class="fgm-index-sub">BVMT</p>
              </div>
              <div style="text-align:right">
                <p class="fgm-index-val">{{ idx.value }}</p>
                <p class="fgm-index-var" [class.text-success]="idx.up" [class.text-danger]="!idx.up"
                   style="font-size:12px;font-weight:700">{{ idx.var }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Movers -->
        <div class="fgm-market-card">
          <div class="fgm-market-card__header">
            <span class="fgm-market-card__title">Valeurs en mouvement</span>
            <span class="fgm-market-card__badge">Top</span>
          </div>
          <div>
            <div *ngFor="let m of movers" class="fgm-mover-row">
              <span class="fgm-mover-bar" [style.background]="m.up ? 'hsl(var(--green))' : 'hsl(var(--red))'"></span>
              <span class="fgm-mover-name">{{ m.name }}</span>
              <span class="fgm-mover-price">{{ m.price }}</span>
              <span class="fgm-mover-var" [class.text-success]="m.up" [class.text-danger]="!m.up">
                {{ m.up ? '↑' : '↓' }} {{ m.var }}
              </span>
            </div>
          </div>
        </div>

        <!-- Dashboard items -->
        <div class="fgm-market-card">
          <div class="fgm-market-card__header">
            <span class="fgm-market-card__title">Ce que vous y trouverez</span>
          </div>
          <div>
            <div *ngFor="let it of dashboardItems" class="fgm-dashboard-item">
              <p class="fgm-dashboard-item__title">{{ it.title }}</p>
              <p class="fgm-dashboard-item__desc">{{ it.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
  `,
})
export class MarketOverviewComponent {
  facts = facts;
  indices = indices;
  movers = movers;
  dashboardItems = dashboardItems;
}
