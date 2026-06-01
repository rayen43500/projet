import {
  Component, OnInit, OnDestroy, ChangeDetectorRef, ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { TranslationService } from '../../services/translation.service';
import { Alerte, PositionNette, RisqueGlobal } from '../../models/fgm.models';

@Component({
  selector: 'app-intermediaire',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
<!-- TOPBAR (minimal — no admin actions) -->
<div class="dt">
  <a class="dt__brand" href="#">
    <div class="dt__logo-box">
      <img src="assets/bvmt-logo.png" alt="BVMT" class="dt__logo-img" />
    </div>
    <div class="dt__brand-text">
      <div class="dt__brand-name">FGM BVMT</div>
      <div class="dt__brand-sub">Fonds de Garantie de Marché</div>
    </div>
  </a>
  <div class="dt__right">
    <div class="dt__market"><span class="dt__market-dot"></span>{{ ts.t('seance.statut.OUVERTE') }}</div>

    <!-- Lang toggle -->
    <div class="dt__lang">
      <button [class.active]="ts.lang==='fr'" (click)="ts.setLang('fr')">FR</button>
      <span>|</span>
      <button [class.active]="ts.lang==='en'" (click)="ts.setLang('en')">EN</button>
    </div>

    <!-- User info -->
    <div class="dt__user" *ngIf="auth.currentUser">
      <div class="dt__user-avatar">{{ initials }}</div>
      <div class="dt__user-info">
        <div class="dt__user-name">{{ auth.currentUser.fullName || auth.currentUser.username }}</div>
        <div class="dt__user-role">{{ ts.t('login.roleIntermediaire') }}</div>
      </div>
      <button class="dt__logout" (click)="logout()" title="{{ ts.t('common.logout') }}">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
        </svg>
      </button>
    </div>
  </div>
</div>

<!-- VALUE VARIATIONS CAROUSEL (visible to Intermediaire) -->
<div class="ticker-wrap">
  <div class="ticker-label">BVMT LIVE</div>
  <div class="ticker-scroll">
    <div class="ticker-inner">
      <ng-container *ngFor="let t of tickerData">
        <div class="ticker-item">
          <span class="ticker-name">{{t.n}}</span>
          <span class="ticker-price">{{t.p}}</span>
          <span class="ticker-var" [class.up]="t.up" [class.dn]="!t.up">{{t.v}}</span>
        </div>
      </ng-container>
      <ng-container *ngFor="let t of tickerData">
        <div class="ticker-item">
          <span class="ticker-name">{{t.n}}</span>
          <span class="ticker-price">{{t.p}}</span>
          <span class="ticker-var" [class.up]="t.up" [class.dn]="!t.up">{{t.v}}</span>
        </div>
      </ng-container>
    </div>
  </div>
</div>

<div class="dashboard-page">
  <div class="page-wrap">

    <!-- ── MY ALERTS ─────────────────────────────────────── -->
    <div class="section-header" style="margin-top:0">
      <div class="section-header-icon">
        <svg viewBox="0 0 24 24"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0"/></svg>
      </div>
      <span class="section-header-title">{{ ts.t('intermediaire.myAlerts') }}</span>
      <div class="section-header-line"></div>
      
    </div>

    <ng-container *ngIf="myAlerts.length > 0">
      <div *ngFor="let a of myAlerts"
           class="alert-banner"
           [class.danger]="a.type==='DEFAUT_TITRES' || a.type==='ANNULATION'">
        <span class="alert-icon">{{ a.type==='DEFAUT_TITRES' || a.type==='ANNULATION' ? '⚠' : 'ℹ' }}</span>
        <div>
          <strong>{{ a.type | uppercase }} :</strong>&nbsp;{{ a.message }}
          <span style="float:right;color:var(--text-muted)">{{ a.timestamp }}</span>
        </div>
      </div>
    </ng-container>
    <div *ngIf="myAlerts.length === 0"
         class="alert-banner"
         style="background:#edf6f0;border-color:rgba(61,107,82,.25);border-left-color:#3d6b52;border-left-width:4px">
      <span class="alert-icon">✓</span>
      <strong>{{ ts.t('intermediaire.noAlerts') }}</strong>
    </div>

    <!-- ── MY MARKET SITUATION ────────────────────────────── -->
    <div class="section-header" style="margin-top:24px">
      <div class="section-header-icon">
        <svg viewBox="0 0 24 24"><path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/></svg>
      </div>
      <span class="section-header-title">{{ ts.t('intermediaire.myMarketTitle') }}</span>
      <div class="section-header-line"></div>
    </div>

    <div class="card">
      <div class="card-body" style="padding:0;overflow-x:auto">
        <table class="data-table" *ngIf="myPositions.length > 0">
          <thead>
            <tr>
              <th>{{ ts.t('intermediaire.isin') }}</th>
              <th>{{ ts.t('intermediaire.valeur') }}</th>
              <th class="right">{{ ts.t('intermediaire.pnt') }}</th>
              <th class="right">{{ ts.t('intermediaire.pne') }}</th>
              <th class="right">{{ ts.t('intermediaire.risque') }}</th>
              <th>{{ ts.t('intermediaire.position') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let p of myPositions"
                [style.background]="p.statut==='CRITICAL' ? '#fff5f5' : ''">
              <td class="isin-code">{{ p.codeIsin || p.libelleValeur }}</td>
              <td>{{ p.libelleValeur || '—' }}</td>
              <td class="right mono" [class.val-up]="p.pnt>0" [class.val-dn]="p.pnt<0">
                {{ p.pnt>0?'+':'' }}{{ p.pnt | number:'1.0-0' }}
              </td>
              <td class="right mono" [class.val-up]="p.pne>0" [class.val-dn]="p.pne<0">
                {{ p.pne>0?'+':'' }}{{ p.pne | number:'1.0-0' }}
              </td>
              <td class="right mono" style="font-weight:700"
                  [style.color]="p.statut==='CRITICAL' ? 'var(--red-raw)' : 'var(--navy-raw)'">
                {{ p.risqueJ | number:'1.0-0' }}
              </td>
              <td>
                <span class="badge"
                  [class.badge-ok]="p.typeRisque==='AUCUN'"
                  [class.badge-danger]="p.typeRisque==='DEFAUT_TITRES'"
                  [class.badge-warn]="p.typeRisque==='DEFAUT_ESPECES'">
                  {{ typeRisqueLabel(p.typeRisque) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <div *ngIf="myPositions.length === 0"
             style="text-align:center;padding:32px;color:var(--text-muted);font-size:13px">
          {{ loading ? ts.t('common.loading') : ts.t('intermediaire.noPositions') }}
        </div>
      </div>
    </div>

  </div>
</div>

<!-- FOOTER -->
<div style="background:#1c2c1c;color:rgba(255,255,255,.45);font-size:11px;padding:16px 24px;display:flex;justify-content:space-between;align-items:center">
  <div>
    <span style="color:#fff;font-weight:700">FGM BVMT</span> —
    Fonds de Garantie de Marché · © 2026
  </div>
  <div>
      ''
  </div>

</div>


<style>
.dt__lang {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: rgba(255,255,255,.4);
}
.dt__lang button {
  background: none; border: none;
  color: rgba(255,255,255,.4); font-size: 12px;
  font-weight: 700; cursor: pointer; padding: 2px 4px;
  border-radius: 3px; transition: color .15s;
}
.dt__lang button.active { color: #fff; }
</style>
  `,
})
export class IntermediaireComponent implements OnInit, OnDestroy {

  loading     = false;

  allAlerts:    Alerte[]       = [];
  allPositions: PositionNette[] = [];

  tickerData = [
    { n:'SFBT',       p:'13.490',  v:'+0.75%', up:true  },
    { n:'BIAT',       p:'142.800', v:'+2.37%', up:true  },
    { n:'ATTIJARI BK',p:'71.000',  v:'+0.28%', up:true  },
    { n:'BH BANK',    p:'10.300',  v:'+0.10%', up:true  },
    { n:'MONOPRIX',   p:'6.700',   v:'-0.15%', up:false },
    { n:'SPDIT-SICAF',p:'14.690',  v:'-2.07%', up:false },
    { n:'BNA',        p:'14.450',  v:'+1.05%', up:true  },
    { n:'AMEN BANK',  p:'59.850',  v:'+0.59%', up:true  },
    { n:'ATB',        p:'3.500',   v:'+0.57%', up:true  },
    { n:'STB',        p:'3.950',   v:'-1.25%', up:false },
  ];

  private subs: Subscription[] = [];

  constructor(
    public  auth:   AuthService,
    private api:    ApiService,
    private cd:     ChangeDetectorRef,
    private router: Router,
    public  ts:     TranslationService,
  ) {}

  get initials(): string {
    const name = this.auth.currentUser?.fullName || this.auth.currentUser?.username || '?';
    return name.split(' ').map((p: string) => p[0]).slice(0, 2).join('').toUpperCase();
  }

  get myCode(): number | undefined {
    return this.auth.currentUser?.codeIntermediaire;
  }

  /** Alerts filtered to this intermediaire only */
  get myAlerts(): Alerte[] {
    if (!this.myCode) return [];
    return this.allAlerts.filter(a =>
      a.intermediaire === String(this.myCode) ||
      a.intermediaire === this.auth.currentUser?.username
    );
  }

  /** Positions for this intermediaire only */
  get myPositions(): PositionNette[] {
    if (!this.myCode) return [];
    return this.allPositions.filter(p =>
      Number(p.codeIntermediaire) === this.myCode
    );
  }

  ngOnInit(): void {
    this.loadMyPositionsFromApi();
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  async logout(): Promise<void> {
    await this.auth.logout();
  }

  private loadMyPositionsFromApi(): void {
    this.loading = true;
    this.api.getSeanceCourante().subscribe({
      next: (s: any) => {
        const d = (s?.dateSeance ?? s?.seance) as string | undefined;
        if (!d || !this.myCode) {
          this.loading = false;
          this.cd.markForCheck();
          return;
        }
        this.api.getMyPositions(d).subscribe({
          next: (pos: any[]) => {
            this.allPositions = (pos ?? []).map((p: any) => this.normalizePosition(p));
            this.loading = false;
            this.cd.markForCheck();
          },
          error: () => {
            this.loading = false;
            this.cd.markForCheck();
          },
        });
      },
      error: () => {
        this.loading = false;
        this.cd.markForCheck();
      },
    });
  }

  private normalizePosition(p: any): PositionNette {
    return {
      ...p,
      codeIntermediaire: String(p.codeIntermediaire ?? ''),
      statut: p.statut ?? 'NORMAL',
      typeRisque: p.typeRisque ?? 'AUCUN',
    } as PositionNette;
  }

  typeRisqueLabel(t: string): string {
    return ({
      AUCUN: 'Normal',
      DEFAUT_TITRES: 'Suspens titres',
      DEFAUT_ESPECES: 'Suspens espèces',
    } as any)[t] ?? t;
  }
}