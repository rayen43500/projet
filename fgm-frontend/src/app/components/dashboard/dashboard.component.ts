import {
  Component, OnInit, OnDestroy, AfterViewInit, AfterViewChecked,
  ViewChild, ViewChildren, QueryList, ElementRef, ChangeDetectorRef, ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { TopbarComponent } from '../topbar/topbar.component';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { TranslationService } from '../../services/translation.service';
import { BvmtNumberPipe } from '../../pipes/bvmt-number.pipe';
import {
  Seance, Intermediaire, PositionNette,
  RisqueGlobal, Alerte, SeanceEvent,
  Transaction, Valeur, MouvementBancaire, AppelRestitution,
  ApportInitial, Placement, Tmm, Parametrage, FeuilleAppelMarge
} from '../../models/fgm.models';

declare const Chart: any;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, TopbarComponent, BvmtNumberPipe],
  template: `
<!-- TOPBAR -->
<app-topbar
  [isWelcome]="false"
  [activePage]="activePage"
  [newAlerts]="alertes"
  [lang]="ts.lang"
  (pageChange)="showPage($event)"
  (langChange)="ts.setLang($any($event))"
/>

<!-- HORIZONTAL NAV BAR (replaces sidebar) -->
<nav class="hnav">
  <div class="hnav__inner">
    <a class="hnav__item" [class.active]="activePage==='dashboard'" (click)="showPage('dashboard')">
      <svg viewBox="0 0 24 24"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>
      <span>Tableau de bord</span>
    </a>
    <a class="hnav__item" [class.active]="activePage==='seance'" (click)="showPage('seance')">
      <svg viewBox="0 0 24 24"><path d="M19 3h-1V1h-2v2H8V1H6v2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/></svg>
      <span>Séances</span>
    </a>
    <a class="hnav__item" [class.active]="activePage==='positions'" (click)="showPage('positions')">
      <svg viewBox="0 0 24 24"><path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/></svg>
      <span>Positions PNT/PNE</span>
    </a>
    <a class="hnav__item" [class.active]="activePage==='risque'" (click)="showPage('risque')">
      <svg viewBox="0 0 24 24"><path d="M12 2L1 21h22L12 2zm0 3.5L20.5 19h-17L12 5.5zM11 10v4h2v-4h-2zm0 6v2h2v-2h-2z"/></svg>
      <span>Risque Marché</span>
      <span *ngIf="alertes.length>0" class="hnav__badge">{{alertes.length}}</span>
    </a>
    <a *ngIf="canViewContributions" class="hnav__item" [class.active]="activePage==='contributions'" (click)="showPage('contributions')">
      <svg viewBox="0 0 24 24"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>
      <span>Contributions</span>
    </a>
    <a class="hnav__item" [class.active]="activePage==='intermediaires'" (click)="showPage('intermediaires')">
      <svg viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
      <span>Intermédiaires</span>
      <span *ngIf="intermediaires.length>0" class="hnav__count">{{intermediaires.length}}</span>
    </a>
    <a class="hnav__item" [class.active]="activePage==='historique'" (click)="showPage('historique')">
      <svg viewBox="0 0 24 24"><path d="M13 3a9 9 0 1 0 .001 18.001A9 9 0 0 0 13 3zm0 16c-3.86 0-7-3.14-7-7s3.14-7 7-7 7 3.14 7 7-3.14 7-7 7zm.5-11H12v6l5.25 3.15.75-1.23-4.5-2.67V8z"/></svg>
      <span>Historique</span>
    </a>
    <a class="hnav__item" [class.active]="activePage==='swift'" (click)="showPage('swift')">
      <svg viewBox="0 0 24 24"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>
      <span>SWIFT</span>
    </a>
    <a *ngIf="isAdmin" class="hnav__item" [class.active]="activePage==='parametrage'" (click)="showPage('parametrage')">
      <svg viewBox="0 0 24 24"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
      <span>Paramétrage</span>
    </a>
    <div class="hnav__lang-sep"></div>
    <button class="hnav__lang" [class.active]="ts.lang==='fr'" (click)="ts.setLang('fr')">FR</button>
    <button class="hnav__lang" [class.active]="ts.lang==='en'" (click)="ts.setLang('en')">EN</button>
    <div *ngIf="seanceCourante" class="hnav__seance-chip">
      <span class="hnav__seance-dot" [class.open]="seanceCourante.statut==='OUVERTE'"></span>
      {{seanceCourante.dateSeance | date:'dd/MM/yy'}}
      <span class="hnav__seance-badge" [class.ok]="seanceCourante.statut==='OUVERTE'">{{seanceCourante.statut}}</span>
    </div>
  </div>
</nav>

<!-- STOCK TICKER TAPE -->
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

<!-- WS INDICATOR -->

<!-- LOADING OVERLAY -->
<div *ngIf="loading" style="position:fixed;inset:0;z-index:999;background:rgba(255,255,255,.6);display:flex;align-items:center;justify-content:center">
  <div style="background:#fff;border:1px solid #ddd;padding:24px 36px;border-radius:6px;box-shadow:0 8px 30px rgba(0,0,0,.15);text-align:center">
    <div class="auth-card__spinner" style="width:28px;height:28px;margin:0 auto 12px"></div>
    <div style="font-size:13px;color:#1c2c1c;font-weight:600">{{loadingMsg}}</div>
  </div>
</div>

<!-- TOAST NOTIFICATIONS -->
<div style="position:fixed;bottom:24px;right:24px;z-index:1000;display:flex;flex-direction:column;gap:8px">
  <div *ngFor="let t of toastList" class="toast-msg" [class.toast-ok]="t.type==='ok'" [class.toast-warn]="t.type==='warn'" [class.toast-danger]="t.type==='danger'">
    {{t.msg}}
  </div>
</div>

<div class="dashboard-page">
  <div class="page-wrap">

    <!-- ══════════════════════════════════════════════════
         PAGE DASHBOARD
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='dashboard'">

      <!-- KPI CARDS -->
      <div class="kpi-row">
        <div class="kpi kpi-blue">
          <div class="kpi-label">{{ ts.t('dashboard.riskTotal') }}</div>
          <div class="kpi-value">{{fmt(computedRisqueTotal)}}</div>
          <div class="kpi-sub">TND · {{seanceCourante?.dateSeance || '—'}}</div>
        </div>
        <div class="kpi kpi-gold">
          <div class="kpi-label">{{ ts.t('dashboard.guaranteeFund') }}</div>
          <div class="kpi-value">{{fmt(risque?.provision ?? 0)}}</div>
          <div class="kpi-sub">TND</div>
        </div>
        <div class="kpi kpi-green">
          <div class="kpi-label">{{ ts.t('dashboard.activeIntermed') }}</div>
          <div class="kpi-value">{{nbIntermedActifs}} / {{intermediaires.length || '—'}}</div>
          <div class="kpi-sub up" *ngIf="seanceCourante?.statut==='OUVERTE'">● {{ ts.t('seance.statut.OUVERTE') }}</div>
          <div class="kpi-sub" *ngIf="seanceCourante?.statut!=='OUVERTE'">● {{seanceCourante?.statut ? ts.t('seance.statut.' + seanceCourante!.statut) : '—'}}</div>
        </div>

      </div>

      <!-- TOP 3 INTERMÉDIAIRES PAR TRANSACTIONS/HEURE -->
      <div class="section-header" style="margin-top:16px">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg></div>
        <span class="section-header-title">Top 3 Intermédiaires — Transactions / heure (08h30–15h00)</span>
        <div class="section-header-line"></div>
        
      </div>
      <div class="index-charts-grid">
        <ng-container *ngFor="let item of top3IntermedByTransRate; let rank = index">
          <div class="index-chart-card" style="padding:0;overflow:hidden">
            <!-- Header info — clones BVMT index card layout exactly -->
            <div style="padding:16px 16px 0">
              <div class="index-chart-top" style="margin-bottom:2px">
                <div>
                  <div class="index-chart-name">
                    <span style="font-size:14px;margin-right:6px">{{rank===0?'🥇':rank===1?'🥈':'🥉'}}</span>{{item.nom}}
                  </div>
                  <div class="index-chart-pts">{{item.nbTx | number:'1.0-0'}}</div>
                  <div class="index-chart-var val-up">+{{item.txParHeure | number:'1.1-1'}} tx/h</div>
                </div>
                <div style="text-align:right;display:flex;flex-direction:column;gap:4px">
                  <div class="index-chart-type">BVMT</div>
                  <div class="index-chart-type2">{{rank===0?'Top 1':rank===1?'Top 2':'Top 3'}}</div>
                </div>
              </div>
              <div class="index-chart-date" style="margin-bottom:8px">{{seanceCourante?.dateSeance || today}}</div>
            </div>
            <!-- Mini line chart — full width, axes visible -->
            <div style="position:relative;height:120px">
              <canvas #intermedCanvas style="display:block;width:100%;height:120px"></canvas>
            </div>
          </div>
        </ng-container>
        <div *ngIf="top3IntermedByTransRate.length === 0" class="index-chart-card" style="grid-column:1/-1;text-align:center;padding:32px;color:var(--text-muted);font-size:13px">
          {{ (loading && positions.length === 0) ? ts.t('common.loading') : 'Importez les fichiers BVMT pour afficher le classement (page Séances → Calculer & Importer)' }}
        </div>
      </div>

      <!-- POSITIONS TABLE -->
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z"/></svg></div>
        <span class="section-header-title">Positions — {{seanceCourante?.dateSeance || '—'}}</span>
        <div class="section-header-line"></div>
        <span *ngIf="positions.length > 0" style="font-size:10px;background:#edf6f0;color:#2e7d46;padding:3px 10px;border-radius:100px;font-weight:700;border:1px solid rgba(61,107,82,.2)">{{positions.length}} positions calculées</span>
      </div>
      <div class="main-grid">
        <div>
          <div class="card" style="margin-bottom:0">
            <div class="card-header">
              <span class="card-header-title">Positions à risque (TOP {{topPositions.length}})</span>
              <span class="badge badge-info">{{seanceCourante?.nbTransactions || nbTradesImportes}} opérations</span>
            </div>
            <div class="card-body" style="padding:0;overflow-x:auto">
              <table class="data-table">
                <thead><tr>
                  <th>Intermédiaire</th><th>ISIN</th><th>Valeur</th>
                  <th class="right">PNT</th><th>Sens</th>
                  <th class="right">PNE (TND)</th><th class="right">Cours</th>
                  <th class="right">RV (TND)</th><th>Statut</th>
                </tr></thead>
                <tbody>
                  <ng-container *ngIf="topPositions.length > 0">
                    <tr *ngFor="let p of topPositions" [style.background]="p.typeRisque!=='AUCUN'?'#fff5f5':''">
                      <td style="font-weight:700" [style.color]="p.typeRisque!=='AUCUN'?'var(--red-raw)':''">{{p.nomIntermediaire || '#'+p.codeIntermediaire}}</td>
                      <td class="isin-code">{{p.codeIsin || p.isin}}</td>
                      <td>{{p.libelleValeur || '—'}}</td>
                      <td class="right mono" [class.val-up]="p.pnt>0" [class.val-dn]="p.pnt<0">{{p.pnt>0?'+':''}}{{p.pnt | bvmtNumber:0}}</td>
                      <td><span class="pos-tag" [class.pos-buy]="p.pnt>=0" [class.pos-sell]="p.pnt<0">{{p.pnt>=0?'▲ Acheteur':'▼ Vendeur'}}</span></td>
                      <td class="right mono" [class.val-up]="p.pne>0" [class.val-dn]="p.pne<0">{{p.pne>0?'+':''}}{{p.pne | bvmtNumber:3}}</td>
                      <td class="right mono">{{p.coursCloture | number:'1.3-3'}}</td>
                      <td class="right mono" style="font-weight:700" [style.color]="p.typeRisque!=='AUCUN'?'var(--red-raw)':'var(--navy-raw)'">{{p.risqueJ | number:'1.0-0'}}</td>
                      <td><span class="badge" [class.badge-ok]="p.typeRisque==='AUCUN'" [class.badge-danger]="p.typeRisque==='DEFAUT_TITRES'" [class.badge-warn]="p.typeRisque==='DEFAUT_ESPECES'">{{typeRisqueLabel(p.typeRisque)}}</span></td>
                    </tr>
                  </ng-container>
                  <tr *ngIf="topPositions.length===0">
                    <td colspan="9" style="text-align:center;padding:24px;color:var(--text-muted)">{{positions.length > 0 ? 'Aucune position à risque' : ((loading && positions.length === 0) ? ts.t('common.loading') : 'Importez les fichiers BVMT pour calculer les positions (page Séances)') }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div>

          <div class="sidebar-panel">

            <div class="sidebar-section">
              <div class="sidebar-title">{{ ts.t('nav.myAlerts') }}</div>
              <div style="font-size:11px;color:var(--text-muted);text-align:center;padding:12px">
                <span *ngIf="alertes.length > 0" style="color:var(--red-raw);font-weight:600">
                  ⚠ {{alertes.length}} alerte(s) — consultez la cloche 🔔
                </span>
                <span *ngIf="alertes.length === 0">✓ {{ ts.t('dashboard.noAnomaly') }}</span>
              </div>
            </div>
            <div class="sidebar-section">
              <div class="sidebar-title">Taux de couverture</div>
              <div style="text-align:center;padding:8px 0">
                <div style="font-family:'Roboto Condensed',sans-serif;font-size:28px;font-weight:700;color:var(--navy-raw)">{{computedTauxCouverture | number:'1.1-1'}} %</div>
                <div style="font-size:11px;color:var(--text-muted);margin-top:4px">{{fmt(risque?.provision ?? 0)}} / {{fmt(computedRisqueTotal)}} TND</div>
                <div style="margin-top:8px;background:#eee;height:8px;border-radius:4px;overflow:hidden">
                  <div [style.width]="coverageBarPct+'%'" [style.background]="coverageBarPct>200?'var(--green-raw)':coverageBarPct>100?'var(--gold-raw)':'var(--red-raw)'" style="height:100%;border-radius:4px;transition:width .5s"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE SÉANCE — with file upload for Admin
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='seance'">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>{{ ts.t('nav.seances') }}</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/></svg></div>
        <span class="section-header-title">{{ ts.t('nav.seances') }}</span>
        <div class="section-header-line"></div>
        <div class="section-actions" *ngIf="canWriteActions">
          <button class="act-btn act-success" (click)="actionPreparer()" [disabled]="loading">
            <svg viewBox="0 0 24 24"><path d="M19 3h-1V1h-2v2H8V1H6v2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/></svg>
            {{ ts.t('seance.prepare') }}
          </button>
          <button class="act-btn act-danger" (click)="actionAnnuler()" [disabled]="loading">
            <svg viewBox="0 0 24 24"><path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
            {{ ts.t('seance.cancel') }}
          </button>
        </div>
      </div>

      <div class="main-grid">
        <div>
          <!-- SESSION FILE UPLOAD — Admin only -->
          <div class="card" *ngIf="canWriteActions && seanceCourante">
            <div class="card-header">
              <span class="card-header-title">Import fichiers BVMT — Calcul des risques</span>
              <span class="badge badge-info">{{ seanceCourante.dateSeance }}</span>
            </div>
            <div class="card-body">
              <div style="font-size:11px;color:var(--text-muted);margin-bottom:14px;padding:8px 12px;background:#f8faf9;border-radius:4px;border-left:3px solid var(--green-raw)">
                ℹ Chargez les 3 fichiers BVMT (transactions, valeurs, intermédiaires). Le serveur calcule automatiquement : positions nettes (PNT/PNE), risques (RisqueJ, RM) et feuille d'appel de marge.
              </div>

              <!-- 1. Transactions -->
              <div class="upload-row">
                <div class="upload-label">
                  <strong>1. Fichier Transactions</strong>
                  <span style="font-size:10px;color:var(--text-muted);display:block">transactions_YYYYMMDD.txt — format fixe BVMT (enreg. type 02)</span>
                </div>
                <label class="upload-zone" [class.uploading]="sessionUploadState === 'uploading'" [class.uploaded]="!!sessionFiles['tx']">
                  <input type="file" accept=".txt" (change)="onSessionFile($event, 'tx')" [disabled]="sessionUploadState === 'uploading'" />
                  <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/></svg>
                  <span>{{ sessionFiles['tx'] ? '✓ ' + sessionFiles['tx'].name : 'Déposer transactions_YYYYMMDD.txt' }}</span>
                </label>
              </div>

              <!-- 2. Valeurs -->
              <div class="upload-row">
                <div class="upload-label">
                  <strong>2. Fichier Valeurs (cours de clôture)</strong>
                  <span style="font-size:10px;color:var(--text-muted);display:block">valeurs_YYYYMMDD.txt — date;type;ISIN;libelle;open;close</span>
                </div>
                <label class="upload-zone" [class.uploading]="sessionUploadState === 'uploading'" [class.uploaded]="!!sessionFiles['val']">
                  <input type="file" accept=".txt" (change)="onSessionFile($event, 'val')" [disabled]="sessionUploadState === 'uploading'" />
                  <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/></svg>
                  <span>{{ sessionFiles['val'] ? '✓ ' + sessionFiles['val'].name : 'Déposer valeurs_YYYYMMDD.txt' }}</span>
                </label>
              </div>

              <!-- 3. Intermédiaires -->
              <div class="upload-row">
                <div class="upload-label">
                  <strong>3. Fichier Intermédiaires</strong>
                  <span style="font-size:10px;color:var(--text-muted);display:block">intermediaires.txt / .json — CSV code;libelle ou tableau JSON BVMT</span>
                </div>
                <label class="upload-zone" [class.uploading]="sessionUploadState === 'uploading'" [class.uploaded]="!!sessionFiles['inter']">
                  <input type="file" accept=".txt,.json" (change)="onSessionFile($event, 'inter')" [disabled]="sessionUploadState === 'uploading'" />
                  <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/></svg>
                  <span>{{ sessionFiles['inter'] ? '✓ ' + sessionFiles['inter'].name : 'Déposer intermediaires.txt ou .json' }}</span>
                </label>
              </div>

              <!-- Submit -->
              <!-- Date detected from transactions file -->
              <div *ngIf="detectedFileDate" style="margin-top:10px;display:flex;align-items:center;gap:8px;padding:8px 14px;background:#e8f5e9;border:1px solid #a5d6a7;border-radius:6px;font-size:12px">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="#2e7d46"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/></svg>
                <span style="color:#1b5e20">Date séance détectée depuis le fichier&nbsp;: <strong>{{detectedFileDate}}</strong> — la séance sera créée automatiquement si elle n'existe pas.</span>
              </div>

              <div style="margin-top:14px;display:flex;gap:10px;align-items:center;flex-wrap:wrap">
                <button class="act-btn act-success"
                  [disabled]="!sessionFiles['tx'] || !sessionFiles['val'] || !sessionFiles['inter'] || sessionUploadState === 'uploading'"
                  (click)="submitSessionImport()">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14-5-5 1.41-1.41L12 14.17l7.59-7.59L21 8l-9 9z"/></svg>
                  {{ sessionUploadState === 'uploading' ? 'Calcul en cours...' : 'Calculer & Importer' }}
                </button>
                <button class="act-btn" style="background:#fff;border:1px solid #ddd;color:#666"
                  *ngIf="sessionFiles['tx'] || sessionFiles['val'] || sessionFiles['inter']"
                  (click)="resetSessionFiles()">
                  Réinitialiser
                </button>
                <span *ngIf="sessionUploadState === 'done'" style="color:#2e7d46;font-size:12px;font-weight:600">✓ Import calculé — dashboard mis à jour</span>
                <span *ngIf="sessionUploadState === 'error'" style="color:#c0392b;font-size:12px;font-weight:600">✗ Erreur — voir les logs</span>
              </div>

              <!-- Results summary -->
              <div *ngIf="sessionImportResult" style="margin-top:16px;padding:12px;background:#f8faf9;border-radius:6px;border:1px solid rgba(61,107,82,.2)">
                <div style="font-weight:700;font-size:12px;color:var(--green-dark);margin-bottom:8px">📊 Résultats du calcul — tous les tableaux sont mis à jour</div>
                <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px;font-size:11px">
                  <div><span style="color:var(--text-muted)">Transactions lues</span><br><strong>{{sessionImportResult.statistiques?.nbTrades}}</strong></div>
                  <div><span style="color:var(--text-muted)">Positions nettes</span><br><strong>{{sessionImportResult.statistiques?.nbPositions}}</strong></div>
                  <div><span style="color:var(--text-muted)">Positions à risque</span><br><strong style="color:var(--red-raw)">{{sessionImportResult.statistiques?.nbPositionsRisque}}</strong></div>
                  <div><span style="color:var(--text-muted)">R_val total (TND)</span><br><strong>{{sessionImportResult.statistiques?.totalRval | number:'1.0-0'}}</strong></div>
                  <div><span style="color:var(--text-muted)">R_susp total (TND)</span><br><strong>{{sessionImportResult.statistiques?.totalRsusp | number:'1.0-0'}}</strong></div>
                  <div><span style="color:var(--text-muted)">Défaillants</span><br><strong style="color:var(--red-raw)">{{sessionImportResult.statistiques?.nbDefaillants}}</strong></div>
                </div>
              </div>
            </div>
          </div>

          <!-- SESSION HISTORY TABLE -->
          <div class="card">
            <div class="card-header">
              <span class="card-header-title">{{ ts.t('seance.sessionHistory') }}</span>
              <span class="badge badge-info">{{historique.length}} {{ ts.lang === 'fr' ? 'séances' : 'sessions' }}</span>
            </div>
            <div class="card-body" style="padding:0">
              <div *ngFor="let s of historique" style="padding:14px;border-bottom:1px solid #f0f0f0">
                <div class="session-row">
                  <div class="session-date-box" [style.background]="seanceBg(s.statut)">
                    <span class="day">{{s.dateSeance | slice:8:10}}</span>
                    {{monthLabel(s.dateSeance)}} {{s.dateSeance | slice:0:4}}
                  </div>
                  <div class="session-info">
                    <div class="session-title">Séance COB — Marché Central BVMT</div>
                    <div class="session-meta">
                      Ouverture {{s.heureOuverture}} · Clôture {{s.heureCloture}} ·
                      {{s.nbIntermediaires}} intermédiaires ·
                      {{s.nbTransactions}} transactions ·
                      Vol. {{(s.volumeTND/1000000 | number:'1.2-2')}} MTND
                    </div>
                    <div *ngIf="s.motifAnnulation" class="session-meta" style="color:var(--red-raw)">Motif : {{s.motifAnnulation}}</div>
                  </div>
                  <div class="session-status" [style.background]="statutBg(s.statut)" [style.color]="statutColor(s.statut)">
                    {{statutIcon(s.statut)}} {{ ts.t('seance.statut.' + s.statut) }}
                  </div>
                </div>
              </div>
              <div *ngIf="historique.length===0" style="padding:24px;text-align:center;color:var(--text-muted);font-size:13px">
                {{loading ? ts.t('common.loading') : ts.t('seance.noSession')}}
              </div>
            </div>
          </div>
        </div>

        <div>
          <div class="card">
            <div class="card-header"><span class="card-header-title">{{ ts.t('seance.currentSession') }}</span></div>
            <div class="card-body">
              <ng-container *ngIf="seanceCourante; else noSeance">
                <table class="data-table">
                  <tbody>
                    <tr><td style="font-weight:700">Date</td><td class="right mono">{{seanceCourante.dateSeance}}</td></tr>
                    <tr><td style="font-weight:700">Statut</td><td class="right">
                      <span class="badge" [class.badge-ok]="seanceCourante.statut==='OUVERTE'" [class.badge-warn]="seanceCourante.statut==='PREPAREE'" [class.badge-info]="seanceCourante.statut==='CLOTUREE'" [class.badge-danger]="seanceCourante.statut==='ANNULEE'">{{ ts.t('seance.statut.' + seanceCourante.statut) }}</span>
                    </td></tr>
                    <tr><td style="font-weight:700">Ouverture</td><td class="right mono">{{seanceCourante.heureOuverture}}</td></tr>
                    <tr><td style="font-weight:700">Clôture</td><td class="right mono">{{seanceCourante.heureCloture}}</td></tr>
                    <tr><td style="font-weight:700">Intermédiaires</td><td class="right mono">{{seanceCourante.nbIntermediaires}}</td></tr>
                    <tr><td style="font-weight:700">Transactions</td><td class="right mono">{{seanceCourante.nbTransactions || nbTradesImportes}}</td></tr>
                    <tr><td style="font-weight:700">Volume TND</td><td class="right mono val-up">{{seanceCourante.volumeTND | number:'1.0-0'}} TND</td></tr>
                    <tr><td style="font-weight:700">Positions</td><td class="right mono">{{positions.length}}</td></tr>
                    <tr><td style="font-weight:700">Positions à risque</td><td class="right mono" [class.val-dn]="nbPositionsARisque>0">{{nbPositionsARisque}}</td></tr>
                    <tr><td style="font-weight:700">Anomalies</td><td class="right mono" [class.val-dn]="seanceCourante.anomalies.length>0">{{seanceCourante.anomalies.length}}</td></tr>
                  </tbody>
                </table>
                <button *ngIf="canWriteActions && seanceCourante.statut==='OUVERTE'"
                        class="act-btn act-success" style="width:100%;margin-top:12px;justify-content:center"
                        (click)="actionCloturer()" [disabled]="loading">
                  ✓ {{ ts.t('seance.close') }}
                </button>
                <button *ngIf="canWriteActions && seanceCourante.statut==='OUVERTE'"
                        class="act-btn" style="width:100%;margin-top:8px;justify-content:center;background:#566;color:#fff"
                        (click)="actionDetecterAnomalies()" [disabled]="loading">
                  🔍 {{ ts.t('seance.detect') }}
                </button>
              </ng-container>
              <ng-template #noSeance>
                <div style="text-align:center;padding:20px;color:var(--text-muted);font-size:13px">{{ ts.t('seance.noOpenSession') }}</div>
                <button *ngIf="canWriteActions" class="act-btn act-success" style="width:100%;justify-content:center;margin-top:8px"
                        (click)="actionPreparer()" [disabled]="loading">
                  {{ ts.t('seance.openNewSession') }}
                </button>
              </ng-template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE HISTORIQUE
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='historique'">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>{{ ts.t('nav.historique') }}</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M13 3a9 9 0 1 0 .001 18.001A9 9 0 0 0 13 3zm0 16c-3.86 0-7-3.14-7-7s3.14-7 7-7 7 3.14 7 7-3.14 7-7 7zm.5-11H12v6l5.25 3.15.75-1.23-4.5-2.67V8z"/></svg></div>
        <span class="section-header-title">{{ ts.t('historique.title') }}</span>
        <div class="section-header-line"></div>
      </div>
      <div class="card">
        <div class="card-header">
          <span class="card-header-title">{{ ts.t('historique.subtitle') }}</span>
          <span class="badge badge-info">{{historique.length}} {{ ts.lang === 'fr' ? 'séances' : 'sessions' }}</span>
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>{{ ts.t('historique.date') }}</th>
              <th>{{ ts.t('historique.statut') }}</th>
              <th class="right">{{ ts.t('historique.nbTransactions') }}</th>
              <th class="right">{{ ts.t('historique.volume') }}</th>
              <th class="right">{{ ts.t('historique.nbAnomalies') }}</th>
              <th>{{ ts.t('historique.actions') }}</th>
            </tr></thead>
            <tbody>
              <tr *ngFor="let s of historique" [style.background]="s.statut==='ANNULEE'?'#fff8f8':s.statut==='OUVERTE'?'#f8fff8':''">
                <td>
                  <div style="font-weight:700;font-size:13px">{{s.dateSeance}}</div>
                  <div style="font-size:11px;color:var(--text-muted)">{{monthLabel(s.dateSeance)}} {{s.dateSeance | slice:0:4}}</div>
                </td>
                <td><span class="badge" [class.badge-ok]="s.statut==='OUVERTE'" [class.badge-info]="s.statut==='CLOTUREE'" [class.badge-warn]="s.statut==='PREPAREE'" [class.badge-danger]="s.statut==='ANNULEE'">{{statutIcon(s.statut)}} {{ ts.t('seance.statut.' + s.statut) }}</span></td>
                <td class="right mono">{{s.nbTransactions | number:'1.0-0'}}</td>
                <td class="right mono">{{(s.volumeTND / 1000000) | number:'1.3-3'}}</td>
                <td class="right mono" [class.val-dn]="s.anomalies.length > 0">{{s.anomalies.length > 0 ? s.anomalies.length : '—'}}</td>
                <td>
                  <button *ngIf="canWriteActions && s.statut==='OUVERTE'"
                          class="act-btn act-success" style="padding:4px 10px;font-size:11px"
                          (click)="actionCloturerDate(s.dateSeance)" [disabled]="loading">
                    ✓ {{ ts.t('seance.close') }}
                  </button>
                  <span *ngIf="s.motifAnnulation" style="font-size:11px;color:var(--red-raw)" [title]="s.motifAnnulation">⚠ {{ s.motifAnnulation | slice:0:20 }}…</span>
                </td>
              </tr>
              <tr *ngIf="historique.length===0">
                <td colspan="6" style="text-align:center;padding:32px;color:var(--text-muted)">{{loading ? ts.t('common.loading') : ts.t('historique.noData')}}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE POSITIONS
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='positions'">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>{{ ts.t('nav.positions') }}</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/></svg></div>
        <span class="section-header-title">Positions Nettes — {{seanceCourante?.dateSeance || '—'}}</span>
        <div class="section-header-line"></div>
        
      </div>
      <div class="card">
        <div class="card-header">
          <span class="card-header-title">Positions nettes par intermédiaire et valeur</span>
          <span class="badge badge-info">{{positions.length}} positions</span>
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>Intermédiaire</th><th>ISIN</th><th>Valeur</th>
              <th class="right">PNT</th><th>Sens PNT</th>
              <th class="right">PNE (TND)</th><th>Sens PNE</th>
              <th>Type Risque</th><th class="right">Cours</th>
              <th class="right">RisqueJ</th><th class="right">RM</th>
            </tr></thead>
            <tbody>
              <tr *ngFor="let p of positions" [style.background]="p.typeRisque!=='AUCUN'?'#fff5f5':''">
                <td style="font-weight:700" [style.color]="p.typeRisque!=='AUCUN'?'var(--red-raw)':''">{{p.nomIntermediaire || '#'+p.codeIntermediaire}}</td>
                <td class="isin-code">{{p.codeIsin || p.isin}}</td>
                <td>{{p.libelleValeur || '—'}}</td>
                <td class="right mono" [class.val-up]="p.pnt>0" [class.val-dn]="p.pnt<0">{{p.pnt>0?'+':''}}{{p.pnt | bvmtNumber:0}}</td>
                <td><span class="pos-tag" [class.pos-buy]="p.pnt>=0" [class.pos-sell]="p.pnt<0">{{p.pnt>=0?'▲ Ach':'▼ Ven'}}</span></td>
                <td class="right mono" [class.val-up]="p.pne>0" [class.val-dn]="p.pne<0">{{p.pne>0?'+':''}}{{p.pne | bvmtNumber:3}}</td>
                <td><span class="pos-tag" [class.pos-buy]="p.pne>=0" [class.pos-sell]="p.pne<0">{{p.pne>=0?'▲ Créd':'▼ Déb'}}</span></td>
                <td><span class="badge" [class.badge-ok]="p.typeRisque==='AUCUN'" [class.badge-danger]="p.typeRisque==='DEFAUT_TITRES'" [class.badge-warn]="p.typeRisque==='DEFAUT_ESPECES'">{{typeRisqueLabel(p.typeRisque)}}</span></td>
                <td class="right mono">{{p.coursCloture | bvmtNumber:3}}</td>
                <td class="right mono" style="font-weight:700" [style.color]="p.typeRisque!=='AUCUN'?'var(--red-raw)':'var(--navy-raw)'">{{p.risqueJ | number:'1.0-0'}}</td>
                <td class="right mono">{{(p.rm ?? p.risqueJ) | number:'1.0-0'}}</td>
              </tr>
              <tr *ngIf="positions.length===0">
                <td colspan="11" style="text-align:center;padding:24px;color:var(--text-muted)">{{ (loading && positions.length === 0) ? ts.t('common.loading') : 'Importez les fichiers BVMT pour voir les positions (page Séances)' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE RISQUE
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='risque'">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>{{ ts.t('nav.risque') }}</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M12 2L1 21h22L12 2zm0 3.5L20.5 19h-17L12 5.5zM11 10v4h2v-4h-2zm0 6v2h2v-2h-2z"/></svg></div>
        <span class="section-header-title">{{ ts.t('nav.risque') }}</span>
        <div class="section-header-line"></div>
        <div class="section-actions" *ngIf="canWriteActions">
          <button class="act-btn act-success" (click)="actionDetecterAnomalies()" [disabled]="loading">
            <svg viewBox="0 0 24 24"><path d="M17.65 6.35A7.96 7.96 0 0 0 12 4a8 8 0 1 0 7.74 10h-2.08A6 6 0 1 1 12 6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
            {{ ts.t('seance.detect') }}
          </button>
        </div>
      </div>
      <div class="kpi-row">
        <div class="kpi kpi-blue">
          <div class="kpi-label">RM — Risque Marché</div>
          <div class="kpi-value">{{fmt(computedRM)}}</div>
          <div class="kpi-sub">TND · {{positions.length}} positions</div>
        </div>
        <div class="kpi kpi-red">
          <div class="kpi-label">Positions critiques</div>
          <div class="kpi-value">{{nbPositionsARisque}}</div>
          <div class="kpi-sub" *ngIf="nbPositionsARisque>0" style="color:var(--red-raw)">{{defaillants.length}} défaillant(s)</div>
        </div>
        <div class="kpi kpi-gold">
          <div class="kpi-label">R Total</div>
          <div class="kpi-value">{{fmt(computedRisqueTotal)}}</div>
          <div class="kpi-sub">TND</div>
        </div>
        <div class="kpi kpi-green">
          <div class="kpi-label">Taux de couverture</div>
          <div class="kpi-value">{{computedTauxCouverture | number:'1.1-1'}} %</div>
        </div>
      </div>

      <!-- Feuille d'appel de marge -->
      <div class="card" *ngIf="feuilleAppelMarge.length > 0">
        <div class="card-header">
          <span class="card-header-title">Feuille d'appel de marge</span>
          <span class="badge" [class.badge-danger]="defaillants.length>0" [class.badge-ok]="defaillants.length===0">{{defaillants.length}} défaillant(s)</span>
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>Intermédiaire</th>
              <th class="right">R_susp (TND)</th>
              <th class="right">R_val (TND)</th>
              <th class="right">Total (TND)</th>
              <th class="right">Provision (TND)</th>
              <th class="right">Différence</th>
              <th>Statut</th>
            </tr></thead>
            <tbody>
              <tr *ngFor="let f of feuilleAppelMarge" [style.background]="f.defaillant?'#fff5f5':''">
                <td style="font-weight:700" [style.color]="f.defaillant?'var(--red-raw)':''">{{f.nomIntermediaire}}</td>
                <td class="right mono" [class.val-dn]="f.rSusp>0">{{f.rSusp | number:'1.0-0'}}</td>
                <td class="right mono" [class.val-dn]="f.rVal>0">{{f.rVal | number:'1.0-0'}}</td>
                <td class="right mono" style="font-weight:700">{{f.total | number:'1.0-0'}}</td>
                <td class="right mono val-up">{{f.provision | number:'1.0-0'}}</td>
                <td class="right mono" [class.val-dn]="f.difference>0" [class.val-up]="f.difference<=0">{{f.difference>0?'+':''}}{{f.difference | number:'1.0-0'}}</td>
                <td><span class="badge" [class.badge-danger]="f.defaillant" [class.badge-ok]="!f.defaillant">{{f.defaillant?'DÉFAILLANT':'COUVERT'}}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-header-title">Suspens et anomalies</span>
          <span class="badge" [class.badge-danger]="alertes.length>0" [class.badge-ok]="alertes.length===0">{{alertes.length}} alerte(s)</span>
        </div>
        <div class="card-body">
          <div style="text-align:center;padding:16px;font-size:13px;color:var(--text-muted)">
            <span *ngIf="alertes.length > 0" style="color:var(--red-raw);font-weight:600">
              ⚠ {{alertes.length}} alerte(s) active(s) — consultez la cloche 🔔 en haut à droite
            </span>
            <span *ngIf="alertes.length === 0" style="color:#2e9e4f;font-weight:600">✓ Aucun suspens — Marché sain</span>
          </div>
        </div>
      </div>
      <div class="card" style="margin-bottom:0">
        <div class="card-header"><span class="card-header-title">Évolution RM par intermédiaire</span></div>
        <div class="card-body"><div style="position:relative;height:220px"><canvas #rmChart></canvas></div></div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE CONTRIBUTIONS
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='contributions'" *ngIf="canViewContributions">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>{{ ts.t('nav.contributions') }}</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg></div>
        <span class="section-header-title">{{ ts.t('nav.contributions') }}</span>
        <div class="section-header-line"></div>
      </div>
      <div class="card">
        <div class="card-header">
          <span class="card-header-title">Provisions par intermédiaire</span>
          <span class="badge badge-info">Fonds total: {{fmt(risque?.provision ?? 0)}} TND</span>
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>Intermédiaire</th>
              <th class="right">R_val (TND)</th>
              <th class="right">R_susp (TND)</th>
              <th class="right">Total Risque R</th>
              <th class="right">Part du fonds</th>
              <th>Statut</th>
            </tr></thead>
            <tbody>
              <tr *ngFor="let row of contributionsRows" [style.background]="row.defaillant?'#fff5f5':row.statut==='APPEL'?'#fffbf0':''">
                <td style="font-weight:700" [style.color]="row.defaillant?'var(--red-raw)':row.statut==='APPEL'?'var(--gold-raw)':''">{{row.nom}}</td>
                <td class="right mono" [class.val-dn]="row.rVal>0">{{row.rVal | number:'1.0-0'}}</td>
                <td class="right mono" [class.val-dn]="row.rSusp>0">{{row.rSusp | number:'1.0-0'}}</td>
                <td class="right mono val-dn" style="font-weight:700">{{row.total | number:'1.0-0'}}</td>
                <td class="right">
                  <div class="contrib-bar-bg" style="display:inline-block;width:80px;margin-right:6px">
                    <div class="contrib-bar" [style.width]="row.pct+'%'"></div>
                  </div>
                  <span style="font-size:11px;color:var(--text-muted)">{{row.pct | number:'1.1-1'}}%</span>
                </td>
                <td><span class="badge" [class.badge-ok]="!row.defaillant && row.statut==='OK'" [class.badge-pending]="row.statut==='APPEL'" [class.badge-danger]="row.defaillant">{{row.defaillant?'DÉFAILLANT':row.statut}}</span></td>
              </tr>
              <tr *ngIf="contributionsRows.length===0">
                <td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">Importez les fichiers BVMT pour voir les contributions</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="card" style="margin-top:16px">
        <div class="card-header">
          <span class="card-header-title">Mouvements bancaires (appels / restitutions)</span>
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>Intermédiaire</th>
              <th class="right">RM J</th>
              <th class="right">RM J-1</th>
              <th class="right">Total RV</th>
              <th class="right">Provision</th>
              <th class="right">Appel</th>
              <th class="right">Restitution</th>
            </tr></thead>
            <tbody>
              <tr *ngFor="let m of mouvementBancaireList">
                <td style="font-weight:700">{{m.intermediaire}}</td>
                <td class="right mono">{{m.totalSeance | number:'1.0-0'}}</td>
                <td class="right mono">{{m.totalSeancePrecedent | number:'1.0-0'}}</td>
                <td class="right mono val-dn">{{m.total | number:'1.0-0'}}</td>
                <td class="right mono">{{m.provision | number:'1.0-0'}}</td>
                <td class="right mono val-up">{{m.appel | number:'1.0-0'}}</td>
                <td class="right mono val-dn">{{m.restitution | number:'1.0-0'}}</td>
              </tr>
              <tr *ngIf="mouvementBancaireList.length===0">
                <td colspan="7" style="text-align:center;padding:24px;color:var(--text-muted)">Importez les fichiers BVMT pour voir les mouvements bancaires</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="card" style="margin-top:16px">
        <div class="card-header">
          <span class="card-header-title">MvtBanqueInter (virements intermédiaires)</span>
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>Intermédiaire</th>
              <th>Code</th>
              <th>Banque</th>
              <th>N° compte</th>
              <th class="right">Débit</th>
              <th class="right">Crédit</th>
              <th class="right">Total</th>
              <th>Date valeur</th>
            </tr></thead>
            <tbody>
              <tr *ngFor="let m of mvtBanqueInterList">
                <td style="font-weight:700">{{m.intermediaire}}</td>
                <td class="mono">{{m.codeInterm}}</td>
                <td>{{m.banque}}</td>
                <td class="mono">{{m.numeroCompte}}</td>
                <td class="right mono val-dn">{{m.debit | number:'1.0-0'}}</td>
                <td class="right mono val-up">{{m.credit | number:'1.0-0'}}</td>
                <td class="right mono">{{m.total | number:'1.0-0'}}</td>
                <td>{{m.dateValeur | date:'dd/MM/yy'}}</td>
              </tr>
              <tr *ngIf="mvtBanqueInterList.length===0">
                <td colspan="8" style="text-align:center;padding:24px;color:var(--text-muted)">Aucun mouvement intermédiaire pour cette séance</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE INTERMÉDIAIRES
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='intermediaires'">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>{{ ts.t('nav.intermediaires') }}</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg></div>
        <span class="section-header-title">{{ ts.t('nav.intermediaires') }}</span>
        <div class="section-header-line"></div>
      </div>
      <div class="card">
        <div class="card-header">
          <span class="card-header-title">{{intermediaires.length}} intermédiaires habilités</span>
          <input type="text" class="search-bar" style="width:280px;margin:0" [placeholder]="ts.t('common.search')"
                 [(ngModel)]="searchQuery" (ngModelChange)="filterIntermed()" />
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>Code</th><th>Libellé court</th><th>Dénomination</th>
              <th>Adresse</th><th>Banque</th>
              <th class="right">RisqueJ (TND)</th><th class="right">RM (TND)</th><th>Statut</th>
            </tr></thead>
            <tbody>
              <ng-container *ngFor="let m of filteredIntermed">
                <tr [style.background]="intermedRiskStatus(+m.codeIntermediaire)==='CRITICAL'?'#fff5f5':''">
                  <td class="mono" style="font-weight:700">{{m.codeIntermediaire}}</td>
                  <td style="font-weight:700" [style.color]="intermedRiskStatus(+m.codeIntermediaire)==='CRITICAL'?'var(--red-raw)':''">{{m.libelleCourtIntermediaire}}</td>
                  <td>{{m.libelleLongIntermediaire}}</td>
                  <td style="font-size:11px;color:var(--text-muted)">{{m.adresseIntermediaire || '—'}}</td>
                  <td style="font-size:12px">{{bankName(m.codeBanque + '')}}</td>
                  <td class="right mono" [class.val-dn]="intermedRisk(+m.codeIntermediaire)>0">{{intermedRisk(+m.codeIntermediaire)>0 ? (intermedRisk(+m.codeIntermediaire)|number:'1.0-0') : '—'}}</td>
                  <td class="right mono" [class.val-dn]="intermedRM(+m.codeIntermediaire)>0">{{intermedRM(+m.codeIntermediaire)>0 ? (intermedRM(+m.codeIntermediaire)|number:'1.0-0') : '—'}}</td>
                  <td><span class="badge" [class.badge-ok]="intermedRiskStatus(+m.codeIntermediaire)==='NORMAL'" [class.badge-danger]="intermedRiskStatus(+m.codeIntermediaire)==='CRITICAL'">{{intermedRiskStatus(+m.codeIntermediaire)==='CRITICAL'?'Suspens actif':'Actif'}}</span></td>
                </tr>
              </ng-container>
              <tr *ngIf="filteredIntermed.length===0">
                <td colspan="8" style="text-align:center;padding:24px;color:var(--text-muted)">{{loading ? ts.t('common.loading') : 'Aucun résultat'}}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE SWIFT — Messages SWIFT pacs.010
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='swift'">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>SWIFT</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg></div>
        <span class="section-header-title">Messages SWIFT — pacs.010.001.02</span>
        <div class="section-header-line"></div>
        <span *ngIf="swiftList.length>0" style="font-size:10px;background:#edf6f0;color:#2e7d46;padding:3px 10px;border-radius:100px;font-weight:700;border:1px solid rgba(61,107,82,.2)">{{swiftList.length}} message(s)</span>
      </div>

      <!-- Prerequisite warning banner -->
      <div *ngIf="canWriteActions && seanceCourante && !swiftPrereqOk" style="margin-bottom:16px;padding:14px 18px;background:#fff8e1;border:1px solid #f9a825;border-radius:8px;display:flex;align-items:flex-start;gap:12px">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="#f9a825" style="flex-shrink:0;margin-top:1px"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
        <div>
          <div style="font-weight:700;font-size:13px;color:#e65100;margin-bottom:4px">Traitement de séance requis avant la génération SWIFT</div>
          <div style="font-size:12px;color:#795548;line-height:1.6">
            Aucun état bancaire trouvé pour la séance <strong>{{swiftSeanceLabel}}</strong>.<br>
            Allez dans l'onglet <strong>Séances</strong>, chargez les <strong>3 fichiers BVMT</strong>
            (transactions, valeurs, intermédiaires) et cliquez <strong>Calculer &amp; Importer</strong>.<br>
            <span style="color:#5d4037">Les positions PNT/PNE, risques et mouvements bancaires sont calculés automatiquement par le serveur.</span>
            <button style="margin-top:8px;padding:4px 12px;background:#e65100;color:#fff;border:none;border-radius:4px;cursor:pointer;font-size:11px;font-weight:700"
              (click)="showPage('seance')">→ Aller aux Séances</button>
          </div>
        </div>
      </div>

      <!-- Prereq OK badge -->
      <div *ngIf="canWriteActions && seanceCourante && swiftPrereqOk && swiftList.length===0" style="margin-bottom:16px;padding:10px 16px;background:#edf6f0;border:1px solid #a5d6a7;border-radius:8px;display:flex;align-items:center;gap:10px">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="#2e7d46"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
        <span style="font-size:12px;font-weight:600;color:#2e7d46">{{banqueEtatCount}} état(s) bancaire(s) chargés pour cette séance — prêt pour la génération SWIFT</span>
      </div>

      <!-- Generate button for admin -->
      <div *ngIf="canWriteActions && seanceCourante" style="margin-bottom:16px;display:flex;gap:10px;align-items:center">
        <button class="act-btn act-success" (click)="generateSwift()" [disabled]="loading || !swiftPrereqOk"
          [title]="swiftPrereqOk ? 'Generer les messages SWIFT pacs.010.001.02' : 'Lancez dabord le traitement de seance'">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/></svg>
          Générer SWIFT pour {{swiftSeanceLabel}}
        </button>
        <button class="act-btn" style="background:#fff;border:1px solid #ddd;color:#666" (click)="loadSwift(); checkSwiftPrereq()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M17.65 6.35A7.958 7.958 0 0 0 12 4a8 8 0 1 0 8 8h-2c0 3.31-2.69 6-6 6s-6-2.69-6-6 2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
          Rafraîchir
        </button>
      </div>

      <!-- Summary card -->
      <div *ngIf="swiftList.length>0" class="kpi-row" style="margin-bottom:16px">
        <div class="kpi kpi-blue">
          <div class="kpi-label">Montant Total</div>
          <div class="kpi-value">{{swiftTotalAmount | number:'1.0-0'}}</div>
          <div class="kpi-sub">TND · Somme des débits</div>
        </div>
        <div class="kpi kpi-gold">
          <div class="kpi-label">Nb. Transactions</div>
          <div class="kpi-value">{{swiftList.length}}</div>
          <div class="kpi-sub">Messages pacs.010</div>
        </div>
        <div class="kpi kpi-green">
          <div class="kpi-label">Solde Net Global</div>
          <div class="kpi-value">{{swiftNetBalance | number:'1.0-0'}}</div>
          <div class="kpi-sub">TND</div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-header-title">Détail des instructions SWIFT</span>
          <span class="badge badge-info">pacs.010.001.02 · Règlement RTGS BCT</span>
        </div>
        <div class="card-body" style="padding:0;overflow-x:auto">
          <table class="data-table">
            <thead><tr>
              <th>InstrId</th>
              <th>Débiteur (BIC)</th>
              <th>Créditeur (BIC)</th>
              <th class="right">Montant (TND)</th>
              <th>Date valeur</th>
              <th>Compte créditeur</th>
              <th>Motif</th>
            </tr></thead>
            <tbody>
              <tr *ngFor="let s of swiftList">
                <td class="mono" style="font-size:11px">{{s.InstrId || s.instrId || s.BizMsgIdr || '—'}}</td>
                <td class="mono" style="font-weight:700;color:var(--red-raw)">{{s.BICfgmbanque || s.BICfgmbankopt || '—'}}</td>
                <td class="mono" style="font-weight:700;color:var(--green-dark)">{{s.BICbanquecred || s.BICbanquecentral || '—'}}</td>
                <td class="right mono" style="font-weight:700">{{(s.soldenette !== 0 ? s.soldenette : s.total) | number:'1.0-0'}}</td>
                <td class="mono">{{s.datevaleur || s.dateseance || '—'}}</td>
                <td class="mono" style="font-size:11px;color:var(--text-muted)">{{s.Numcptbanquecred || s.Numcptbanquecentral || '—'}}</td>
                <td style="font-size:11px;color:var(--text-muted)">NET COMPENSATION BVMT · CV {{s.Prtry || 85}}</td>
              </tr>
              <tr *ngIf="swiftList.length===0">
                <td colspan="7" style="text-align:center;padding:32px;color:var(--text-muted)">
                  {{loading ? 'Chargement…' : 'Aucun message SWIFT — générez-en via le bouton ci-dessus (Admin)'}}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════
         PAGE PARAMÉTRAGE — Admin only
    ══════════════════════════════════════════════════ -->
    <div [class.page]="true" [class.active]="activePage==='parametrage'" *ngIf="isAdmin">
      <div class="breadcrumb"><a href="#">Accueil</a><span>›</span>Paramétrage</div>
      <div class="section-header">
        <div class="section-header-icon"><svg viewBox="0 0 24 24"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg></div>
        <span class="section-header-title">Paramétrage du système</span>
        <div class="section-header-line"></div>
        <span style="font-size:10px;background:#fff3d4;color:#c8922a;padding:3px 10px;border-radius:100px;font-weight:700;border:1px solid rgba(200,146,42,.3)">⚙ ADMIN UNIQUEMENT</span>
      </div>

      <div class="main-grid" style="grid-template-columns:1fr 1fr;gap:16px">

        <!-- Seuils / risque parameters -->
        <div class="card">
          <div class="card-header">
            <span class="card-header-title">Paramètres de risque et seuils</span>
          </div>
          <div class="card-body">
            <div *ngIf="parametrage" style="display:flex;flex-direction:column;gap:14px">
              <div class="param-row">
                <label class="param-label">Seuil variation 1 (%)</label>
                <input class="param-input" type="number" step="0.01" [(ngModel)]="parametrage.seuil_var_1" />
              </div>
              <div class="param-row">
                <label class="param-label">Seuil variation 2 (%)</label>
                <input class="param-input" type="number" step="0.01" [(ngModel)]="parametrage.seuil_var_2" />
              </div>
              <div class="param-row">
                <label class="param-label">Seuil variation 3 (%)</label>
                <input class="param-input" type="number" step="0.01" [(ngModel)]="parametrage.seuil_var_3" />
              </div>
              <div class="param-row">
                <label class="param-label">Seuil dép. provisoire (%)</label>
                <input class="param-input" type="number" step="0.01" [(ngModel)]="parametrage.seuil_dep_pro" />
              </div>
              <div class="param-row">
                <label class="param-label">Dépôt risque (TND)</label>
                <input class="param-input" type="number" [(ngModel)]="parametrage.dep_risq" />
              </div>
              <div class="param-row">
                <label class="param-label">Contribution initiale min. (TND)</label>
                <input class="param-input" type="number" [(ngModel)]="parametrage.min_contr_init" />
              </div>
              <div class="param-row">
                <label class="param-label">Délai règlement livraison (j)</label>
                <input class="param-input" type="number" [(ngModel)]="parametrage.del_reg_liv" />
              </div>
              <div class="param-row">
                <label class="param-label">Délai règlement DT (j)</label>
                <input class="param-input" type="number" [(ngModel)]="parametrage.del_reg_DT" />
              </div>
              <div class="param-row">
                <label class="param-label">Délai règlement DE (j)</label>
                <input class="param-input" type="number" [(ngModel)]="parametrage.del_reg_DE" />
              </div>
              <div class="param-row">
                <label class="param-label">Bénéfice (%)</label>
                <input class="param-input" type="number" step="0.01" [(ngModel)]="parametrage.benefice" />
              </div>
              <button class="act-btn act-success" style="margin-top:8px" (click)="saveParametrage()" [disabled]="loading">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M17 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm2 16H5V5h11.17L19 7.83V19zm-7-7c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3zM6 6h9v4H6z"/></svg>
                Enregistrer les paramètres
              </button>
            </div>
            <div *ngIf="!parametrage" style="text-align:center;padding:32px;color:var(--text-muted)">
              {{loading ? 'Chargement…' : 'Paramétrage non disponible'}}
            </div>
          </div>
        </div>

        <!-- TMM management -->
        <div class="card">
          <div class="card-header">
            <span class="card-header-title">Taux Moyen du Marché (TMM)</span>
            <span class="badge badge-info">{{tmmList.length}} enregistrement(s)</span>
          </div>
          <div class="card-body">
            <!-- Add new TMM -->
            <div style="background:#f8faf9;border-radius:6px;padding:14px;margin-bottom:16px;border:1px solid rgba(61,107,82,.15)">
              <div style="font-size:11px;font-weight:700;color:var(--green-dark);margin-bottom:10px;text-transform:uppercase;letter-spacing:.06em">Ajouter / Mettre à jour</div>
              <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px;margin-bottom:10px">
                <div>
                  <label class="param-label">Mois</label>
                  <select class="param-input" [(ngModel)]="newTmm.MOIS">
                    <option *ngFor="let m of moisOptions" [value]="m.v">{{m.l}}</option>
                  </select>
                </div>
                <div>
                  <label class="param-label">Année</label>
                  <input class="param-input" type="number" [(ngModel)]="newTmm.ANNEE" [min]="2020" [max]="2035" />
                </div>
                <div>
                  <label class="param-label">TMM (%)</label>
                  <input class="param-input" type="number" step="0.001" [(ngModel)]="newTmm.TMM" />
                </div>
              </div>
              <button class="act-btn act-success" (click)="saveTmm()" [disabled]="loading" style="width:100%;justify-content:center">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
                Ajouter TMM
              </button>
            </div>
            <!-- TMM table -->
            <div style="max-height:320px;overflow-y:auto">
              <table class="data-table">
                <thead><tr><th>Mois</th><th>Année</th><th class="right">TMM (%)</th><th></th></tr></thead>
                <tbody>
                  <tr *ngFor="let t of tmmList">
                    <td>{{t.MOIS}}</td>
                    <td>{{t.ANNEE}}</td>
                    <td class="right mono" style="font-weight:700;color:var(--navy-raw)">{{t.TMM | number:'1.3-3'}}</td>
                    <td>
                      <button style="background:none;border:none;color:#c84040;cursor:pointer;font-size:12px;font-weight:700" (click)="deleteTmm(t.id)">✕</button>
                    </td>
                  </tr>
                  <tr *ngIf="tmmList.length===0">
                    <td colspan="4" style="text-align:center;padding:16px;color:var(--text-muted)">Aucun TMM enregistré</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!-- Taux field in parametrage -->
            <div *ngIf="parametrage" style="margin-top:14px;padding:12px;background:#f8faf9;border-radius:6px;border:1px solid rgba(61,107,82,.15)">
              <div class="param-row">
                <label class="param-label" style="font-weight:700;color:var(--green-dark)">Taux appliqué (TMM actuel, %)</label>
                <input class="param-input" type="number" step="0.001" [(ngModel)]="parametrage.taux" />
              </div>
              <button class="act-btn act-success" style="margin-top:8px;width:100%;justify-content:center" (click)="saveParametrage()" [disabled]="loading">
                Appliquer le taux
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</div>

<div style="background:#1c2c1c;color:rgba(255,255,255,.45);font-size:11px;padding:16px 24px;display:flex;justify-content:space-between;align-items:center">
  <div>
    <span style="color:#fff;font-weight:700">FGM BVMT</span> —
    Fonds de Garantie de Marché · Spring Boot 3.2 · MongoDB · © 2026
  </div>
  <div style="display:flex;align-items:center;gap:12px">

    <span style="color:rgba(255,255,255,.4)">v2.2</span>
  </div>
</div>

<style>
/* ── HORIZONTAL NAV BAR ─────────────────────────────────────────── */
.hnav {
  background: #243824;
  border-bottom: 1px solid rgba(255,255,255,.08);
  position: sticky;
  top: 52px;
  z-index: 150;
  box-shadow: 0 2px 8px rgba(20,40,20,.18);
}
.hnav__inner {
  display: flex;
  align-items: center;
  padding: 0 16px;
  overflow-x: auto;
  scrollbar-width: none;
  gap: 2px;
  min-height: 44px;
}
.hnav__inner::-webkit-scrollbar { display: none; }
.hnav__item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  height: 44px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(255,255,255,.55);
  cursor: pointer;
  text-decoration: none;
  white-space: nowrap;
  border-bottom: 2px solid transparent;
  transition: color .15s, border-color .15s, background .15s;
  position: relative;
  flex-shrink: 0;
}
.hnav__item svg {
  width: 13px; height: 13px; fill: currentColor; opacity: .7; flex-shrink: 0;
}
.hnav__item:hover {
  color: rgba(255,255,255,.88);
  background: rgba(255,255,255,.05);
}
.hnav__item.active {
  color: #88d4a8;
  border-bottom-color: #5a9470;
  background: rgba(107,184,136,.08);
}
.hnav__item.active svg { opacity: 1; }
.hnav__badge {
  background: #c84040; color: #fff;
  font-size: 9px; font-weight: 800;
  padding: 1px 5px; border-radius: 100px;
}
.hnav__count {
  background: rgba(107,184,136,.2); color: #88d4a8;
  font-size: 9px; font-weight: 700;
  padding: 1px 5px; border-radius: 100px;
}
.hnav__lang-sep {
  margin-left: auto; width: 1px; height: 20px; background: rgba(255,255,255,.1); flex-shrink: 0;
}
.hnav__lang {
  background: none; border: 1px solid rgba(255,255,255,.12);
  color: rgba(255,255,255,.45); font-size: 10px; font-weight: 700;
  padding: 3px 8px; border-radius: 4px; cursor: pointer; flex-shrink: 0;
  transition: all .15s;
}
.hnav__lang.active, .hnav__lang:hover {
  background: rgba(107,184,136,.15); border-color: rgba(107,184,136,.3); color: #88d4a8;
}
.hnav__seance-chip {
  display: flex; align-items: center; gap: 6px;
  background: rgba(255,255,255,.06); border: 1px solid rgba(255,255,255,.1);
  border-radius: 100px; padding: 3px 10px 3px 8px;
  font-size: 10px; font-weight: 600; color: rgba(255,255,255,.6);
  white-space: nowrap; flex-shrink: 0; margin-left: 6px;
}
.hnav__seance-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: rgba(255,255,255,.3); flex-shrink: 0;
}
.hnav__seance-dot.open { background: #5de085; animation: blink 1.5s infinite; }
.hnav__seance-badge {
  font-size: 8px; font-weight: 800; padding: 1px 5px;
  border-radius: 100px; background: rgba(255,255,255,.12); color: rgba(255,255,255,.5);
}
.hnav__seance-badge.ok { background: rgba(93,224,133,.2); color: #88d4a8; }

/* ── LAYOUT (no sidebar) ────────────────────────────────────────── */
.dashboard-page {
  display: flex;
  min-height: calc(100vh - 96px);
  background: var(--bg-raw, #f4f6f4);
}

.page-wrap {
  flex: 1;
  min-width: 0;
  overflow-x: hidden;
}

/* ── UPLOAD ──────────────────────────────────────────────────────── */
.upload-row { margin-bottom: 16px; }
.param-row { display:flex; flex-direction:column; gap:4px; }
.param-label { font-size:10px; font-weight:700; color:var(--text-muted); text-transform:uppercase; letter-spacing:.06em; }
.param-input { height:34px; padding:0 10px; border:1px solid #ddd; border-radius:4px; font-size:13px; color:var(--navy-raw); background:#fff; width:100%; box-sizing:border-box; }
.param-input:focus { outline:none; border-color:var(--green-raw); box-shadow:0 0 0 2px rgba(61,107,82,.15); }
.upload-label { font-size: 12px; font-weight: 700; color: var(--text-muted); margin-bottom: 6px; text-transform: uppercase; letter-spacing: .04em; }
.upload-zone {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px; border: 1.5px dashed #ccd;
  border-radius: 6px; cursor: pointer;
  font-size: 13px; color: var(--text-muted);
  background: #f9f9fb; transition: border-color .15s, background .15s;
}
.upload-zone:hover { border-color: var(--navy-raw); background: #f3f4f8; color: var(--navy-raw); }
.upload-zone.uploading { opacity: .6; cursor: not-allowed; }
.upload-zone.uploaded { border-color: var(--green-raw); background: #f0faf4; color: var(--green-dark); }
.upload-zone input[type="file"] { display: none; }
.upload-ok { display: inline-block; margin-top: 4px; font-size: 12px; color: #2e9e4f; font-weight: 700; }
.toast-msg { padding: 10px 16px; border-radius: 6px; font-size: 12px; font-weight: 600; color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,.2); animation: fadeIn .2s ease; max-width: 320px; }
.toast-ok     { background: #2e7d46; }
.toast-warn   { background: #c8922a; }
.toast-danger { background: #c0392b; }
@keyframes fadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:none; } }
</style>
  `,
})
export class DashboardComponent implements OnInit, AfterViewInit, AfterViewChecked, OnDestroy {

  @ViewChild('rmChart')        rmChartRef!:    ElementRef<HTMLCanvasElement>;
  @ViewChildren('intermedCanvas') intermedCanvases!: QueryList<ElementRef<HTMLCanvasElement>>;

  activePage   = 'dashboard';
  loading      = false;
  loadingMsg   = 'Chargement…';
  searchQuery  = '';
  today        = new Date().toISOString().slice(0, 10);

  historique:       Seance[]        = [];
  seanceCourante:   Seance | null   = null;
  intermediaires:   Intermediaire[] = [];
  filteredIntermed: Intermediaire[] = [];

  // ── Core data — populated from import response ──
  positions:       any[]            = [];   // full list (all types)
  feuilleAppelMarge: any[]          = [];
  mouvementBancaireList: any[]     = [];
  mvtBanqueInterList: any[]        = [];
  /** transactionsByIntermed from import API: { codeStr: { code, nom, nbTransactions } } */
  _transactionsByIntermed: Record<string, {code:number; nom:string; nbTransactions:number}> = {};
  risque:          RisqueGlobal | null = null;
  alertes:         Alerte[]         = [];

  // ── Extra tracking from import ──
  nbTradesImportes = 0;

  // ── Session import ──
  sessionFiles:        Record<string, File|null>           = { tx: null, val: null, inter: null };
  detectedFileDate:    string | null = null;  // ISO date detected from transactions file
  sessionUploadState:  'idle'|'uploading'|'done'|'error'   = 'idle';
  sessionImportResult: any = null;

  // ── Legacy (kept for compat) ──
  uploadStates:  Record<string, boolean> = { pn: false, rv: false, fam: false };
  uploadNames:   Record<string, string>  = {};
  uploadResults: Record<string, boolean> = {};

  // ── Swift ──
  swiftList: any[] = [];
  swiftPrereqOk    = false;   // true when BanqueEtat exists for current seance
  banqueEtatCount  = 0;
  lastImportedDate: string | null = null;  // ISO date of the last successfully imported session

  // ── Parametrage ──
  parametrage: any = null;

  // ── TMM ──
  tmmList: any[] = [];
  newTmm: any = { MOIS: 'Janvier', ANNEE: new Date().getFullYear(), TMM: 0 };
  moisOptions = [
    {v:'Janvier',l:'Janvier'},{v:'Février',l:'Février'},{v:'Mars',l:'Mars'},
    {v:'Avril',l:'Avril'},{v:'Mai',l:'Mai'},{v:'Juin',l:'Juin'},
    {v:'Juillet',l:'Juillet'},{v:'Août',l:'Août'},{v:'Septembre',l:'Septembre'},
    {v:'Octobre',l:'Octobre'},{v:'Novembre',l:'Novembre'},{v:'Décembre',l:'Décembre'}
  ];

  // ── Computed getters — derive everything from positions & feuilleAppelMarge ──

  get topPositions(): any[] {
    return [...this.positions]
      .filter(p => p.typeRisque !== 'AUCUN' && p.risqueJ > 0)
      .sort((a, b) => (b.risqueJ ?? 0) - (a.risqueJ ?? 0))
      .slice(0, 10);
  }

  get nbPositionsARisque(): number {
    return this.positions.filter(p => p.typeRisque !== 'AUCUN' && p.risqueJ > 0).length;
  }

  get defaillants(): any[] {
    return this.feuilleAppelMarge.filter(f => f.defaillant);
  }

  get nbIntermedActifs(): number {
    // Intermédiaires actifs = uniquement ceux ayant réalisé des transactions cette séance
    // Source : transactionsByIntermed rempli lors de l'import (clé = 1 IB actif)
    // Avant import → 0 (pas de données de séance chargées)
    return Object.keys(this._transactionsByIntermed).length;
  }

  /** Libellé date pour SWIFT (seanceCourante garanti non null dans le template) */
  get swiftSeanceLabel(): string {
    return this.lastImportedDate || this.seanceCourante?.dateSeance || '—';
  }

  /** R_val total = sum of risqueJ across all current positions */
  /** Risque Total R = Σ(risqueJ + risqueJ1) = RM global (doc §5: RM = RmJ(J) + RmJ(J-1)) */
  get computedRisqueTotal(): number {
    if (this.feuilleAppelMarge.length > 0) {
      // feuilleAppelMarge.total = rmJ + rmJ1 per broker (from MouvementBancaire batch)
      return this.feuilleAppelMarge.reduce((s, f) => s + (f.total ?? 0), 0);
    }
    // Fallback: sum risqueJ + risqueJ1 from positions (document §5: RM covers last P=2 sessions)
    return this.positions.reduce((s, p) => s + (p.risqueJ ?? 0) + (p.risqueJ1 ?? 0), 0);
  }

  /** RM = Risque Total (document §5: RM = Σ sur les P dernières journées non dénouées) */
  get computedRM(): number {
    if (this.risque?.rm) return Number(this.risque.rm);
    return this.computedRisqueTotal;
  }

  get computedTauxCouverture(): number {
    const provision = this.risque?.provision ?? 0;
    const rTotal = this.computedRisqueTotal;
    if (rTotal <= 0) return 0;
    return Math.min(9999, (provision / rTotal) * 100);
  }

  get coverageBarPct(): number {
    return Math.min(100, this.computedTauxCouverture / 4);
  }

  /**
   * Top 3 intermédiaires by transactions per hour (08h30–15h00 = 6.5h)
   * PRIMARY source: _transactionsByIntermed from import API (exact trade counts)
   * FALLBACK: position count per broker
   */
  /**
   * Session time slots: 08h30 → 15h00 split into 13 half-hour slots
   * Used for the X-axis of the mini line charts
   */
  private readonly SESSION_SLOTS = [
    '08:30','09:00','09:30','10:00','10:30','11:00','11:30',
    '12:00','12:30','13:00','13:30','14:00','14:30','15:00'
  ];

  get top3IntermedByTransRate(): Array<{
    nom: string; nbTx: number; txParHeure: number; barPct: number;
    hourlyData: number[];  // real tx count per half-hour slot (14 points) from API
  }> {
    const SESSION_HOURS = 6.5;
    const N_SLOTS = this.SESSION_SLOTS.length; // 14 half-hour slots

    type BrokerEntry = { nom: string; nbTx: number; hourlyData: number[] };
    let brokerList: BrokerEntry[] = [];

    const txEntries = Object.values(this._transactionsByIntermed);
    if (txEntries.length > 0) {
      // Use REAL per-slot data from the API (slots array built from txIndex in the file)
      brokerList = txEntries.map((e: any) => {
        const total  = Number(e.nbTransactions ?? 0);
        // API returns a 14-element slots array with real counts
        const raw: number[] = Array.isArray(e.slots) ? e.slots.map(Number) : [];
        let hourlyData: number[];
        if (raw.length === N_SLOTS) {
          hourlyData = raw;
        } else if (raw.length > 0) {
          // Resize to N_SLOTS by interpolation
          hourlyData = new Array(N_SLOTS).fill(0);
          raw.forEach((v, i) => {
            const idx = Math.min(N_SLOTS - 1, Math.round(i / raw.length * N_SLOTS));
            hourlyData[idx] = (hourlyData[idx] ?? 0) + v;
          });
        } else {
          // No slot data yet — distribute evenly (first import, legacy data)
          hourlyData = new Array(N_SLOTS).fill(Math.floor(total / N_SLOTS));
          const rem = total - hourlyData.reduce((a, b) => a + b, 0);
          hourlyData[0] = (hourlyData[0] ?? 0) + rem;
        }
        return { nom: e.nom, nbTx: total, hourlyData };
      });
    } else if (this.positions.length > 0) {
      // Fallback: count positions per broker, distributed evenly
      const txByCode = new Map<string, BrokerEntry>();
      for (const p of this.positions) {
        const key = String(p.codeIntermediaire);
        if (!txByCode.has(key)) {
          txByCode.set(key, { nom: p.nomIntermediaire || '#' + key, nbTx: 0, hourlyData: new Array(N_SLOTS).fill(0) });
        }
        const entry = txByCode.get(key)!;
        entry.nbTx++;
        const slot = Math.min(N_SLOTS - 1, Math.floor(entry.nbTx % N_SLOTS));
        entry.hourlyData[slot]++;
      }
      brokerList = [...txByCode.values()];
    }

    if (brokerList.length === 0) return [];

    const sorted = brokerList.sort((a, b) => b.nbTx - a.nbTx).slice(0, 3);
    const maxTx  = sorted[0]?.nbTx ?? 1;

    return sorted.map(b => ({
      nom: b.nom,
      nbTx: b.nbTx,
      txParHeure: +(b.nbTx / SESSION_HOURS).toFixed(1),
      barPct: Math.round((b.nbTx / maxTx) * 100),
      hourlyData: b.hourlyData
    }));
  }

  /** Map bank code to bank name (Tunisian banking system codes) */
  bankName(code: string | null | undefined): string {
    if (!code) return '—';
    const BANKS: Record<string, string> = {
      '1':  'STB',
      '3':  'BNA',
      '4':  'Attijari Bank',
      '5':  'BH Bank',
      '7':  'Amen Bank',
      '8':  'BMCE Capital',
      '10': 'STB Finance',
      '11': 'Attijari Bank',
      '12': 'UIB',
      '14': 'BH Bank',
      '20': 'Arab Tunisian Bank',
      '21': 'UBCI',
      '22': 'BT',
      '23': 'ABC',
      '25': 'Zitouna Bank',
      '26': 'Wifak Bank',
      '27': 'QNB',
      '28': 'Al Baraka',
      '32': 'BIAT',
      '33': 'BFT',
      '35': 'Banque de Tunisie',
      '38': 'CIB',
      '99': 'Compte Propre',
    };
    return BANKS[String(code).trim()] ?? (code ? String(code) : '—');
  }

  get topRiskBars() {
    // Aggregate by intermediaire from feuilleAppelMarge first (most accurate)
    if (this.feuilleAppelMarge.length > 0) {
      const sorted = [...this.feuilleAppelMarge]
        .sort((a, b) => b.total - a.total)
        .slice(0, 6);
      const max = sorted[0]?.total || 1;
      return sorted.map(f => ({
        name: f.nomIntermediaire,
        val: f.total,
        pct: Math.round((f.total / max) * 100)
      }));
    }
    // Fallback: aggregate from positions
    const byCode = new Map<string, { name: string; val: number }>();
    for (const p of this.positions) {
      if (p.risqueJ <= 0) continue;
      const key = String(p.codeIntermediaire);
      const existing = byCode.get(key);
      if (existing) existing.val += p.risqueJ;
      else byCode.set(key, { name: p.nomIntermediaire || '#' + p.codeIntermediaire, val: p.risqueJ });
    }
    const sorted = [...byCode.values()].sort((a, b) => b.val - a.val).slice(0, 6);
    const max = sorted[0]?.val || 1;
    return sorted.map(r => ({ ...r, pct: Math.round((r.val / max) * 100) }));
  }

  get contributionsRows() {
    const provision = this.risque?.provision ?? 0;
    if (this.feuilleAppelMarge.length > 0) {
      return this.feuilleAppelMarge
        .filter(f => f.total > 0)
        .map(f => ({
          nom: f.nomIntermediaire,
          rVal: f.rVal ?? 0,
          rSusp: f.rSusp ?? 0,
          total: f.total,
          pct: Math.min(100, Math.round((f.total / provision) * 10000) / 100),
          defaillant: f.defaillant,
          statut: f.defaillant ? 'DÉFAILLANT' : f.total > 50000 ? 'APPEL' : 'OK'
        }));
    }
    // Fallback from positions
    const byCode = new Map<string, any>();
    for (const p of this.positions) {
      if (p.risqueJ <= 0) continue;
      const key = String(p.codeIntermediaire);
      if (!byCode.has(key)) byCode.set(key, { nom: p.nomIntermediaire || '#' + key, rVal: 0, rSusp: 0, total: 0 });
      byCode.get(key).rVal  += p.rVal ?? ((p.risqueJ ?? 0) + (p.risqueJ1 ?? 0));
      byCode.get(key).rSusp += p.rSusp ?? (p.risqueSuspens ?? 0);
      byCode.get(key).total += (p.rVal ?? ((p.risqueJ ?? 0) + (p.risqueJ1 ?? 0))) + (p.rSusp ?? (p.risqueSuspens ?? 0));
    }
    return [...byCode.values()]
      .sort((a, b) => b.total - a.total)
      .map(r => ({
        ...r,
        pct: Math.min(100, Math.round((r.total / provision) * 10000) / 100),
        defaillant: false,
        statut: r.total > 50000 ? 'APPEL' : 'OK'
      }));
  }

  tickerData = [
    { n:'SFBT', p:'13.490', v:'+0.75%', up:true }, { n:'BIAT', p:'142.800', v:'+2.37%', up:true },
    { n:'ATTIJARI BK', p:'71.000', v:'+0.28%', up:true }, { n:'BH BANK', p:'10.300', v:'+0.10%', up:true },
    { n:'MONOPRIX', p:'6.700', v:'-0.15%', up:false }, { n:'SPDIT-SICAF', p:'14.690', v:'-2.07%', up:false },
    { n:'BNA', p:'14.450', v:'+1.05%', up:true }, { n:'AMEN BANK', p:'59.850', v:'+0.59%', up:true },
    { n:'ATB', p:'3.500', v:'+0.57%', up:true }, { n:'STB', p:'3.950', v:'-1.25%', up:false },
  ];

  private charts: any[] = [];
  private subs: Subscription[] = [];
  private positionsByCode = new Map<number, any>();
  private rmChartInst: any = null;
  private _intermedChartInsts: any[] = [];
  private _intermedChartsNeedRebuild = false;
  toastList: { msg: string; type: string }[] = [];

  constructor(
    public  auth: AuthService,
    private api:  ApiService,
    private cd:   ChangeDetectorRef,
    public  ts:   TranslationService,
  ) {}

  ngOnInit(): void  { this.loadInitialData(); }
  ngAfterViewInit(): void {
    setTimeout(() => this.initCharts(), 150);
    // Re-draw intermed charts whenever the QueryList gains new canvas elements
    this.intermedCanvases.changes.subscribe(() => {
      this._intermedChartsNeedRebuild = true;
    });
  }
  ngAfterViewChecked(): void {
    if (this._intermedChartsNeedRebuild) {
      this._intermedChartsNeedRebuild = false;
      this.buildIntermedCharts();
    }
  }

  ngOnDestroy():void { this.subs.forEach(s => s.unsubscribe()); this.charts.forEach(c => c?.destroy()); for (const i of this._intermedChartInsts) { try { i.destroy(); } catch { /**/ } } }

  // ── Load seances + intermediaires from API ─────────────────────────────────
  loadInitialData(): void {
    this.loading    = true;
    this.loadingMsg = this.ts.t('common.loading');
    this.api.getHistoriqueSeances().subscribe({
      next: (historique: Seance[]) => {
        this.historique = historique;
        // Toujours privilégier la séance OUVERTE pour l'import (dateSeance correcte même avant 1er import).
        // Sinon dernière séance avec transactions, puis la plus récente.
        const ouverte = historique.find((s: Seance) => s.statut === 'OUVERTE');
        const derniereAvecData = historique.find((s: Seance) => (s.nbTransactions ?? 0) > 0);
        this.seanceCourante = ouverte ?? derniereAvecData ?? historique[0] ?? null;

        // Load intermediaires for the current session date (falls back to most recent batch)
        const dateForInter = this.seanceCourante?.dateSeance;
        this.api.getIntermediaires(dateForInter).subscribe({
          next: (intermediaires: Intermediaire[]) => {
            this.intermediaires   = intermediaires;
            this.filteredIntermed = intermediaires;
            this.cd.markForCheck();
          },
          error: () => {}
        });

        // Charger dashboard complet (positions, risque, contributions) depuis le backend
        if (this.seanceCourante?.dateSeance) {
          this._loadDashboardForDate(this.seanceCourante.dateSeance);
        }
        this.loading = false;
        this.cd.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cd.markForCheck();
        this.toast(this.ts.t('common.backendOffline'), 'warn');
      },
    });
  }

  // WebSocket removed — data comes from REST API responses only.

  // ── KEY METHOD: inject all calculated data from import response ──────────
  private injectFromImportResult(res: any): void {
    // 1. Positions nettes (toutes)
    const allPositions: any[] = res.positionsARisque ?? [];
    this.injectPositions(allPositions);

    // 2. Feuille d'appel de marge
    this.feuilleAppelMarge = res.feuilleAppelMarge ?? [];

    // 3. Synthetic risque global
    const stats = res.statistiques ?? {};
    const totalR = (stats.totalRval ?? 0) + (stats.totalRsusp ?? 0);
    const totalProv = stats.totalProvision ?? 0;
    this.risque = {
      dateSeance:     res.dateSeance,
      rm:             stats.rmGlobal ?? 0,
      rTotal:         totalR,
      provision:      totalProv,
      tauxCouverture: totalR > 0 && totalProv > 0 ? (totalProv / totalR) * 100 : 0,
      nbPositions:    stats.nbPositions ?? 0,
      nbCritiques:    stats.nbPositionsRisque ?? 0,
      timestamp:      new Date().toLocaleTimeString('fr-FR'),
    } as any;

    this.nbTradesImportes = stats.nbTrades ?? 0;

    // 3b. Store transactionsByIntermed for top3 and nbIntermedActifs
    this._transactionsByIntermed = res.transactionsByIntermed ?? {};
    this._intermedChartsNeedRebuild = true;

    // 3c. Alertes risque (positions à risque)
    this.alertes = (res.alertes ?? []).map((a: any) => ({
      intermediaire: a.intermediaire ?? '',
      isin:          a.isin ?? '',
      valeur:        a.valeur ?? '',
      risqueJ:       a.risqueJ ?? 0,
      type:          a.type ?? '',
      message:       a.message ?? '',
      timestamp:     a.timestamp ?? '',
    }));

    // 4. Update seanceCourante counters
    if (this.seanceCourante) {
      this.seanceCourante = {
        ...this.seanceCourante,
        nbTransactions: stats.nbTrades ?? this.seanceCourante.nbTransactions,
      };
    }

    // 5. Rebuild chart with new data
    this.updateRmChart();
    // Rebuild intermed mini charts after data update (flag triggers in AfterViewChecked)
    this._intermedChartsNeedRebuild = true;
    this.cd.markForCheck();
  }

  /** Recharge tout le dashboard depuis le backend REST */
  private _loadDashboardForDate(dateStr: string): void {
    this.api.getSessionDashboard(dateStr).subscribe({
      next: (res: any) => {
        if (res?.positionsARisque?.length || res?.feuilleAppelMarge?.length || res?.statistiques) {
          this.injectFromImportResult(res);
          this.loadMouvementBancaire(dateStr);
          this.loadMvtBanqueInter(dateStr);
        } else {
          this._loadPositionsForDate(dateStr);
        }
        this.cd.markForCheck();
      },
      error: () => this._loadPositionsForDate(dateStr)
    });
  }

  /** Load positions for a specific date from the backend */
  private _loadPositionsForDate(dateStr: string): void {
    this.api.getPositionsByDate(dateStr).subscribe({
      next: (positions: any[]) => {
        if (positions && positions.length > 0) {
          this.injectPositions(positions);
          this.cd.markForCheck();
        }
      },
      error: () => { /* positions may not be loaded yet */ }
    });
  }

  private injectPositions(positions: any[]): void {
    this.positions = positions;
    this.positionsByCode.clear();
    for (const p of positions) {
      const code = Number(p.codeIntermediaire);
      const existing = this.positionsByCode.get(code);
      if (!existing || (p.risqueJ ?? 0) > (existing.risqueJ ?? 0)) {
        this.positionsByCode.set(code, p);
      }
    }
  }

  // ── Session import ────────────────────────────────────────────────────────
  onSessionFile(event: Event, type: 'tx' | 'val' | 'inter'): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0] ?? null;
    this.sessionFiles = { ...this.sessionFiles, [type]: file };
    if ((type === 'tx' || type === 'val') && this.sessionFiles['tx']) {
      const tx = this.sessionFiles['tx']!;
      const val = this.sessionFiles['val'];
      this.api.detectImportDate(tx, val).subscribe({
        next: (res: any) => {
          this.detectedFileDate = res.dateSeance ?? null;
          if (this.detectedFileDate) {
            this.toast(`📅 Date détectée depuis le fichier : ${this.detectedFileDate}`, 'ok');
          }
          this.cd.markForCheck();
        },
        error: () => { this.detectedFileDate = null; this.cd.markForCheck(); }
      });
    }
    this.cd.markForCheck();
  }

  resetSessionFiles(): void {
    this.sessionFiles        = { tx: null, val: null, inter: null };
    this.sessionUploadState  = 'idle';
    this.sessionImportResult = null;
    this.detectedFileDate    = null;
    // Note: keep lastImportedDate so SWIFT tab still works after reset
    this.cd.markForCheck();
  }

  submitSessionImport(): void {
    if (!this.sessionFiles['tx'] || !this.sessionFiles['val'] || !this.sessionFiles['inter']) return;

    // Use file-detected date first, then fall back to current seance date
    const effectiveDate = this.detectedFileDate ?? this.seanceCourante?.dateSeance ?? null;
    if (!effectiveDate) {
      this.toast('Aucune date détectable — chargez d\'abord le fichier transactions', 'warn');
      return;
    }

    this.sessionUploadState  = 'uploading';
    this.sessionImportResult = null;
    this.loading    = true;
    this.loadingMsg = 'Calcul des positions et risques en cours…';
    this.cd.markForCheck();

    const formData = new FormData();
    formData.append('transactionsFile',   this.sessionFiles['tx']!);
    formData.append('valeursFile',        this.sessionFiles['val']!);
    formData.append('intermediairesFile', this.sessionFiles['inter']!);
    formData.append('dateSeance',         effectiveDate);

    this.api.importSession(formData).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.sessionUploadState  = 'done';
        this.sessionImportResult = res;

        // ★ INJECT RESULTS IMMEDIATELY INTO ALL DASHBOARD COMPONENTS ★
        this.injectFromImportResult(res);

        const importedDate: string = res.dateSeance ?? effectiveDate;
        this.lastImportedDate = importedDate;
        this.loadMouvementBancaire(importedDate);
        this.loadMvtBanqueInter(importedDate);

        this.api.getIntermediaires(importedDate).subscribe({
          next: (intermediaires: Intermediaire[]) => {
            this.intermediaires = intermediaires;
            this.filteredIntermed = intermediaires;
            this.cd.markForCheck();
          },
          error: () => {}
        });

        if (this.seanceCourante) {
          this.seanceCourante = { ...this.seanceCourante, dateSeance: importedDate };
        } else {
          this.seanceCourante = { dateSeance: importedDate, statut: 'OUVERTE' } as any;
        }

        // Reload seance metadata — new seance now appears in the list
        // After reload, keep the imported date as current seance
        this.api.getHistoriqueSeances().subscribe({
          next: (historique: any[]) => {
            this.historique = historique;
            const importedSeance = historique.find((s: any) => s.dateSeance === importedDate);
            if (importedSeance) {
              this.seanceCourante = importedSeance;
            }
            // Re-run the prereq check now that seanceCourante is correct
            this.swiftPrereqOk = false;
            this.checkSwiftPrereq();
            this.cd.markForCheck();
          },
          error: () => {}
        });

        const stats = res.statistiques ?? {};
        this.toast(
          `✓ ${stats.nbTrades ?? 0} transactions · ${stats.nbPositions ?? 0} positions · ${stats.nbPositionsRisque ?? 0} à risque`,
          'ok'
        );
      },
      error: (err: any) => {
        this.loading = false;
        this.sessionUploadState = 'error';
        const msg = err?.error?.erreur || err?.error?.error || err?.message || 'Import échoué';
        this.toast(`✗ ${msg}`, 'danger');
        this.cd.markForCheck();
      },
    });
  }

  // ── Legacy per-file upload ────────────────────────────────────────────────
  onFileChange(event: Event, type: 'pn' | 'rv' | 'fam'): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file || !this.seanceCourante) return;
    this.uploadNames[type]   = file.name;
    this.uploadStates[type]  = true;
    this.uploadResults[type] = false;
    this.cd.markForCheck();
    const formData = new FormData();
    formData.append('file', file);
    formData.append('dateSeance', this.seanceCourante.dateSeance);
    const endpointMap: Record<string, string> = {
      pn: 'position-nette', rv: 'risque-par-valeur', fam: 'feuille-appel-marge',
    };
    this.api.uploadImportFile(endpointMap[type], formData).subscribe({
      next: () => { this.uploadStates[type] = false; this.uploadResults[type] = true; this.cd.markForCheck(); },
      error: () => { this.uploadStates[type] = false; this.cd.markForCheck(); },
    });
  }

  // ── Actions ───────────────────────────────────────────────────────────────
  showPage(page: string): void {
    this.activePage = page;
    if (page === 'swift') { this.loadSwift(); this.checkSwiftPrereq(); }
    if (page === 'parametrage') { this.loadParametrage(); this.loadTmm(); }
    if (page === 'contributions') {
      this.loadMouvementBancaire();
      this.loadMvtBanqueInter();
    }
  }

  // ── Swift ─────────────────────────────────────────────────────────────────
  checkSwiftPrereq(): void {
    // Use the last imported file date if available, otherwise fall back to current seance
    const effectiveIso = this.lastImportedDate ?? this.seanceCourante?.dateSeance ?? null;
    if (!effectiveIso) return;
    const date = effectiveIso.replace(/-/g, '');
    this.api.getBanqueEtat(date).subscribe({
      next: (list: any[]) => {
        this.banqueEtatCount = list.length;
        this.swiftPrereqOk = list.length > 0;
        this.cd.markForCheck();
      },
      error: () => { this.swiftPrereqOk = false; this.cd.markForCheck(); }
    });
  }

  loadSwift(): void {
    this.api.getAllSwift().subscribe({
      next: (list: any[]) => { this.swiftList = list; this.cd.markForCheck(); },
      error: () => { this.swiftList = []; this.cd.markForCheck(); }
    });
  }

  generateSwift(): void {
    const effectiveIso = this.lastImportedDate ?? this.seanceCourante?.dateSeance ?? null;
    if (!effectiveIso) return;
    const date = effectiveIso.replace(/-/g, '');
    this.loading = true;
    this.api.generateSwift(date).subscribe({
      next: (list: any[]) => {
        this.loading = false;
        this.swiftList = list;
        this.swiftPrereqOk = true;
        this.toast('✓ ' + list.length + ' message(s) SWIFT générés', 'ok');
        this.cd.markForCheck();
      },
      error: (err: any) => {
        this.loading = false;
        // Extract the server's descriptive error message
        const msg = err?.error?.error || err?.message || 'Erreur lors de la génération SWIFT';
        this.toast('✗ ' + msg, 'danger');
        this.cd.markForCheck();
      }
    });
  }

  loadMouvementBancaire(dateIso?: string): void {
    const effectiveIso = dateIso ?? this.lastImportedDate ?? this.seanceCourante?.dateSeance ?? null;
    if (!effectiveIso) return;
    this.api.getMouvementBancaireBySeance(effectiveIso).subscribe({
      next: (list: any[]) => {
        this.mouvementBancaireList = list ?? [];
        this.cd.markForCheck();
      },
      error: () => {
        this.mouvementBancaireList = [];
        this.cd.markForCheck();
      }
    });
  }

  loadMvtBanqueInter(dateIso?: string): void {
    const effectiveIso = dateIso ?? this.lastImportedDate ?? this.seanceCourante?.dateSeance ?? null;
    if (!effectiveIso) return;
    this.api.getMvtBanqueInter(effectiveIso).subscribe({
      next: (list: any[]) => {
        this.mvtBanqueInterList = list ?? [];
        this.cd.markForCheck();
      },
      error: () => {
        this.mvtBanqueInterList = [];
        this.cd.markForCheck();
      }
    });
  }

  // ── Paramétrage ───────────────────────────────────────────────────────────
  loadParametrage(): void {
    this.api.getParametrage().subscribe({
      next: (p: any) => { this.parametrage = Array.isArray(p) ? p[0] : p; this.cd.markForCheck(); },
      error: () => { this.cd.markForCheck(); }
    });
  }

  saveParametrage(): void {
    if (!this.parametrage) return;
    this.loading = true;
    this.api.updateParametrage(this.parametrage).subscribe({
      next: () => { this.loading = false; this.toast('✓ Paramétrage enregistré', 'ok'); this.cd.markForCheck(); },
      error: (err: any) => { this.loading = false; this.toast('✗ ' + err.message, 'danger'); this.cd.markForCheck(); }
    });
  }

  // ── TMM ───────────────────────────────────────────────────────────────────
  loadTmm(): void {
    this.api.getAllTmm().subscribe({
      next: (list: any[]) => { this.tmmList = list; this.cd.markForCheck(); },
      error: () => { this.tmmList = []; this.cd.markForCheck(); }
    });
  }

  saveTmm(): void {
    if (!this.newTmm.TMM || !this.newTmm.MOIS || !this.newTmm.ANNEE) return;
    this.loading = true;
    this.api.saveTmm(this.newTmm).subscribe({
      next: () => { this.loading = false; this.newTmm = { MOIS: 'Janvier', ANNEE: new Date().getFullYear(), TMM: 0 }; this.loadTmm(); this.toast('✓ TMM ajouté', 'ok'); },
      error: (err: any) => { this.loading = false; this.toast('✗ ' + err.message, 'danger'); this.cd.markForCheck(); }
    });
  }

  deleteTmm(id: string): void {
    if (!confirm('Supprimer ce TMM ?')) return;
    this.api.deleteTmm(id).subscribe({
      next: () => { this.loadTmm(); this.toast('✓ TMM supprimé', 'ok'); },
      error: (err: any) => { this.toast('✗ ' + err.message, 'danger'); this.cd.markForCheck(); }
    });
  }

  actionPreparer(): void {
    const date = prompt('Date de la séance (YYYY-MM-DD) :', new Date().toISOString().slice(0, 10));
    if (!date) return;
    this.loading = true; this.loadingMsg = this.ts.t('common.loading');
    this.api.preparerSeance(date).subscribe({
      next: (res: any) => { this.loading = false; this.toast('✓ ' + res.message, 'ok'); this.loadInitialData(); },
      error: (err: any) => { this.loading = false; this.toast('✗ ' + err.message, 'danger'); this.cd.markForCheck(); },
    });
  }

  actionCloturer(): void {
    if (!this.seanceCourante) return;
    if (!confirm('Clôturer la séance du ' + this.seanceCourante.dateSeance + ' ?')) return;
    this.actionCloturerDate(this.seanceCourante.dateSeance);
  }

  actionCloturerDate(date: string): void {
    this.loading = true; this.loadingMsg = this.ts.t('common.loading');
    this.api.cloturerSeance(date).subscribe({
      next: () => { this.loading = false; this.toast('✓ Séance ' + date + ' clôturée', 'ok'); this.loadInitialData(); },
      error: (err: any) => { this.loading = false; this.toast('✗ ' + err.message, 'danger'); this.cd.markForCheck(); },
    });
  }

  actionAnnuler(): void {
    if (!this.seanceCourante) { this.toast('Aucune séance ouverte', 'warn'); return; }
    const motif = prompt('Motif d\'annulation :', 'Anomalie détectée');
    if (!motif) return;
    const date = this.seanceCourante.dateSeance;
    this.loading = true;
    this.api.annulerSeance(date, motif).subscribe({
      next: () => { this.loading = false; this.toast('⚠ Séance annulée — ' + motif, 'warn'); this.loadInitialData(); },
      error: (err: any) => { this.loading = false; this.toast('✗ ' + err.message, 'danger'); this.cd.markForCheck(); },
    });
  }

  actionDetecterAnomalies(): void {
    if (!this.seanceCourante) { this.toast('Aucune séance ouverte', 'warn'); return; }
    const date = this.seanceCourante.dateSeance;
    this.loading = true;
    this.api.detecterAnomalies(date).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.toast(res.nbAnomalies === 0 ? '✓ Aucune anomalie' : '⚠ ' + res.nbAnomalies + ' anomalie(s)', res.nbAnomalies === 0 ? 'ok' : 'danger');
        this.cd.markForCheck();
      },
      error: (err: any) => { this.loading = false; this.toast('✗ ' + err.message, 'danger'); this.cd.markForCheck(); },
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  filterIntermed(): void {
    const q = this.searchQuery.toLowerCase();
    this.filteredIntermed = this.intermediaires.filter(m =>
      m.libelleCourtIntermediaire?.toLowerCase().includes(q) ||
      m.libelleLongIntermediaire?.toLowerCase().includes(q) ||
      m.adresseIntermediaire?.toLowerCase().includes(q) ||
      String(m.codeIntermediaire).includes(q)
    );
  }

  intermedRisk(code: number): number   { return this.positionsByCode.get(code)?.risqueJ ?? 0; }
  intermedRM(code: number):   number   { return this.positionsByCode.get(code)?.rm ?? this.positionsByCode.get(code)?.risqueJ ?? 0; }
  intermedRiskStatus(code: number): string {
    const p = this.positionsByCode.get(code);
    if (!p) return 'NORMAL';
    return p.typeRisque && p.typeRisque !== 'AUCUN' && p.risqueJ > 0 ? 'CRITICAL' : 'NORMAL';
  }

  fmt(val: number | undefined | null): string {
    if (val == null || isNaN(Number(val))) return '—';
    return Number(val).toLocaleString(this.ts.lang === 'fr' ? 'fr-FR' : 'en-US', { maximumFractionDigits: 0 });
  }

  typeRisqueLabel(t: string): string {
    return ({ AUCUN:'Normal', DEFAUT_TITRES:'Susp. titres', DEFAUT_ESPECES:'Susp. espèces' } as any)[t] ?? t;
  }

  seanceBg(s: string): string { return ({ OUVERTE:'#1a3a2a', CLOTUREE:'#1a2a4a', ANNULEE:'#4a1a1a', PREPAREE:'#2a2a1a' } as any)[s] ?? '#2a2a3a'; }
  statutBg(s: string): string { return ({ OUVERTE:'#1a2a4a', CLOTUREE:'#566', ANNULEE:'#8a1a1a', PREPAREE:'#234' } as any)[s] ?? '#566'; }
  statutColor(s: string): string { return ({ OUVERTE:'#d4f0dd', CLOTUREE:'#d4e8fc', ANNULEE:'#fcd4d4', PREPAREE:'#fff3d4' } as any)[s] ?? '#fff'; }
  statutIcon(s: string): string { return ({ OUVERTE:'●', CLOTUREE:'✓', ANNULEE:'⚠', PREPAREE:'○' } as any)[s] ?? '?'; }

  monthLabel(date: string): string {
    if (!date) return '';
    const months = this.ts.lang === 'fr'
      ? ['JAN','FÉV','MAR','AVR','MAI','JUN','JUL','AOÛ','SEP','OCT','NOV','DÉC']
      : ['JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'];
    return months[parseInt(date.slice(5, 7)) - 1] ?? '';
  }

  get canWriteActions(): boolean { return this.auth.hasAnyRole('ADMIN', 'ADMIN_FGM', 'USER', 'SUPERVISEUR'); }
  get canViewContributions(): boolean { return this.auth.hasAnyRole('ADMIN', 'ADMIN_FGM', 'SUPERVISEUR'); }
  get isAdmin(): boolean { return this.auth.hasAnyRole('ADMIN', 'ADMIN_FGM'); }

  get swiftTotalAmount(): number { return this.swiftList.reduce((s, x) => s + Math.abs(x.soldenette || 0), 0); }
  get swiftNetBalance(): number { return this.swiftList.reduce((s, x) => s + (x.soldenette || 0), 0); }

  toast(msg: string, type = 'ok'): void {
    const t = { msg, type };
    this.toastList.push(t);
    this.cd.markForCheck();
    setTimeout(() => { this.toastList = this.toastList.filter(x => x !== t); this.cd.markForCheck(); }, 5000);
  }

  // ── Charts ────────────────────────────────────────────────────────────────
  private initCharts(): void {
    if (this.rmChartRef) this.buildRmChart();
    this._intermedChartsNeedRebuild = true;
    this.cd.markForCheck();
  }

  /** Build (or rebuild) the 3 BVMT-style line charts for top intermédiaires */
  buildIntermedCharts(): void {
    // Use rAF so Angular has flushed the *ngFor DOM before Chart.js reads canvas dimensions
    requestAnimationFrame(() => this._doDrawIntermedCharts());
  }

  private _doDrawIntermedCharts(): void {
    const data = this.top3IntermedByTransRate;
    // Same colors as BVMT indices: navy / gold / red
    const colors = ['#1a3a6a', '#c8922a', '#d63333'];

    for (const inst of this._intermedChartInsts) {
      try { inst.destroy(); } catch { /* ignore */ }
    }
    this._intermedChartInsts = [];

    const canvasRefs = this.intermedCanvases?.toArray() ?? [];
    if (canvasRefs.length === 0) return;

    data.forEach((item, rank) => {
      const ref = canvasRefs[rank];
      if (!ref) return;
      const canvas = ref.nativeElement;

      const color = colors[rank] ?? '#1a3a6a';

      // X-axis labels: show only every 2nd slot to avoid crowding (7 labels for 14 points)
      const xLabels = this.SESSION_SLOTS;

      const inst = new Chart(canvas, {
        type: 'line',
        data: {
          labels: xLabels,
          datasets: [{
            data: item.hourlyData,
            borderColor: color,
            borderWidth: 1.6,
            pointRadius: 0,
            pointHoverRadius: 3,
            fill: true,
            tension: 0.4,
            backgroundColor: (ctx: any) => {
              const chart = ctx.chart;
              const { ctx: c2d, chartArea } = chart;
              if (!chartArea) return color + '20';
              const g = c2d.createLinearGradient(0, chartArea.top, 0, chartArea.bottom);
              g.addColorStop(0, color + '40');
              g.addColorStop(1, color + '04');
              return g;
            }
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          animation: { duration: 500, easing: 'easeInOutQuart' },
          layout: { padding: { left: 0, right: 4, top: 4, bottom: 0 } },
          plugins: {
            legend: { display: false },
            tooltip: {
              mode: 'index' as const,
              intersect: false,
              backgroundColor: 'rgba(20,40,60,.88)',
              titleFont: { size: 10 },
              bodyFont:  { size: 10 },
              callbacks: {
                title: (items: any[]) => xLabels[items[0]?.dataIndex ?? 0] ?? '',
                label: (item: any)    => ' ' + item.raw + ' tx'
              }
            }
          },
          scales: {
            x: {
              grid:  { display: false },
              border:{ display: false },
              ticks: {
                font:          { size: 9, family: 'monospace' },
                color:         '#9aaa99',
                maxRotation:   0,
                maxTicksLimit: 5,
                callback: (_v: any, i: number) => xLabels[i]
              }
            },
            y: {
              position: 'left' as const,
              grid:  { color: 'rgba(0,0,0,.05)', lineWidth: 1 },
              border:{ display: false, dash: [3, 3] },
              ticks: {
                font:          { size: 9, family: 'monospace' },
                color:         '#9aaa99',
                maxTicksLimit: 4,
                padding:       4
              },
              beginAtZero: true
            }
          }
        }
      });
      this._intermedChartInsts.push(inst);
    });
  }

  buildRmChart(): void {
    if (!this.rmChartRef) return;
    const data = this.topRiskBars;
    const labels = data.map(r => r.name?.substring(0, 12) ?? '');
    const values = data.map(r => r.val);
    if (this.rmChartInst) { try { this.rmChartInst.destroy(); } catch { /**/ } this.charts = this.charts.filter(c => c !== this.rmChartInst); }
    if (values.length === 0) return;
    this.rmChartInst = new Chart(this.rmChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          data: values,
          backgroundColor: values.map(v => v > 200000 ? '#d63333' : v > 100000 ? '#c8922a' : '#2e9e4f'),
          borderRadius: 4,
          label: 'Risque (TND)'
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: (ctx: any) => `${Number(ctx.raw).toLocaleString('fr-FR')} TND` } }
        },
        scales: {
          x: { ticks: { font: { size: 10 }, color: '#666' }, grid: { display: false } },
          y: { ticks: { font: { size: 10 }, color: '#999', callback: (v: number) => (v / 1000).toFixed(0) + 'K' }, grid: { color: '#f5f5f5' } }
        }
      }
    });
    this.charts.push(this.rmChartInst);
  }

  updateRmChart(): void { setTimeout(() => this.buildRmChart(), 0); }

  private miniLine(canvas: HTMLCanvasElement, data: number[], color: string): any {
    return new Chart(canvas, {
      type: 'line',
      data: {
        labels: this.labels55(),
        datasets: [{
          data, borderColor: color, borderWidth: 1.5, pointRadius: 0, fill: true, tension: 0.3,
          backgroundColor: (ctx: any) => {
            const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 120);
            g.addColorStop(0, color + '33'); g.addColorStop(1, color + '00'); return g;
          }
        }]
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { ticks: { font: { size: 9 }, maxTicksLimit: 4, color: '#999' }, grid: { display: false } }, y: { ticks: { font: { size: 9 }, color: '#999' }, grid: { color: '#f5f5f5' } } } }
    });
  }

  private intra(base: number, vol: number, trend: number): number[] {
    const pts = []; let v = base;
    for (let i = 0; i < 55; i++) { v += (Math.random() - 0.48) * vol + trend; pts.push(+v.toFixed(3)); }
    return pts;
  }

  private labels55(): string[] {
    const l: string[] = [];
    for (let h = 9; h <= 13; h++) for (let m = 0; m < 60; m += 5) { if (h === 13 && m > 30) break; l.push(`${h}:${String(m).padStart(2, '0')}`); }
    return l;
  }
}