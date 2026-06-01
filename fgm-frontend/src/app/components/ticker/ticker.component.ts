import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface TickerItem {
  id: number;
  code: string;
  risk: number;
  status: 'ok' | 'warn' | 'danger' | 'pending';
}

const ITEMS: TickerItem[] = [
  { id: 1,  code: 'U.F.I',        risk: 0,       status: 'ok' },
  { id: 2,  code: 'T.S.I',        risk: 0,       status: 'ok' },
  { id: 7,  code: 'Union Capital', risk: 67340,   status: 'ok' },
  { id: 9,  code: 'A.F.C',        risk: 0,       status: 'ok' },
  { id: 13, code: 'C.G.I',        risk: 98760,   status: 'ok' },
  { id: 14, code: 'BNA.C',        risk: 142100,  status: 'ok' },
  { id: 15, code: 'A.INVEST',     risk: 189450,  status: 'ok' },
  { id: 18, code: 'MAC.SA',       risk: 284320,  status: 'warn' },
  { id: 22, code: 'STB FIN',      risk: 0,       status: 'ok' },
  { id: 24, code: 'T.VAL',        risk: 215880,  status: 'ok' },
  { id: 26, code: 'T.QIB',        risk: 0,       status: 'ok' },
  { id: 27, code: 'MAX.BOUR',     risk: 0,       status: 'ok' },
  { id: 30, code: 'BIATCAP',      risk: 0,       status: 'ok' },
  { id: 35, code: 'ATTIJARI.I',   risk: 0,       status: 'ok' },
  { id: 37, code: 'UIB Finance',  risk: 0,       status: 'ok' },
  { id: 39, code: 'BH Invest',    risk: 312500,  status: 'danger' },
  { id: 43, code: 'BEST.I',       risk: 67800,   status: 'pending' },
  { id: 48, code: 'BMCE CAP S',   risk: 0,       status: 'ok' },
];

@Component({
  selector: 'app-ticker',
  standalone: true,
  imports: [CommonModule],
  template: `
<div class="fgm-ticker" aria-label="Risque de Valeur par intermédiaire — temps réel">
  <!-- Label -->
  <div class="fgm-ticker__label">
    <span class="fgm-market-badge__dot" style="background:hsl(var(--gold))"></span>
    Intermédiaires · RV
  </div>
  <!-- Scrolling track -->
  <div class="fgm-ticker__track">
    <div class="fgm-ticker__scroll animate-ticker">
      <ng-container *ngFor="let item of loop; let i = index">
        <div class="fgm-ticker__item">
          <span class="fgm-ticker__id">#{{ pad(item.id) }}</span>
          <span class="fgm-ticker__code">{{ item.code }}</span>
          <ng-container *ngIf="item.risk > 0; else sain">
            <span [class]="riskClass(item.status)">{{ fmt(item.risk) }}</span>
            <span class="fgm-ticker__tnd">TND</span>
          </ng-container>
          <ng-template #sain>
            <span class="fgm-ticker__risk-ok">— Sain</span>
          </ng-template>
        </div>
      </ng-container>
    </div>
    <div class="fgm-ticker__fade-l"></div>
    <div class="fgm-ticker__fade-r"></div>
  </div>
</div>
  `,
})
export class TickerComponent {
  loop = [...ITEMS, ...ITEMS];

  pad(id: number): string { return String(id).padStart(2, '0'); }
  fmt(n: number): string { return n.toLocaleString('fr-FR', { maximumFractionDigits: 0 }); }
  riskClass(s: string): string {
    const map: Record<string, string> = {
      warn: 'fgm-ticker__risk-warn',
      danger: 'fgm-ticker__risk-danger',
      pending: 'fgm-ticker__risk-pending',
      ok: 'fgm-ticker__risk-ok',
    };
    return map[s] ?? 'fgm-ticker__risk-ok';
  }
}
