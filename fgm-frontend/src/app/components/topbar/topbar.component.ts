import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Lang } from '../../services/translation.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  template: `
<!-- ══════════════════════════════════ WELCOME TOPBAR ══════════════════════════════════ -->
<ng-container *ngIf="isWelcome">
  <header class="wt" [class.wt--scrolled]="scrolled">
    <div class="wt__inner">
      <!-- Brand -->
      <a class="wt__brand" href="#">
        <img class="wt__logo" src="assets/bvmt-logo.png" alt="BVMT" />
        <div>
          <div class="wt__name">FGM <span class="wt__dot">·</span> BVMT</div>
          <div class="wt__sub">Fonds de Garantie de Marché</div>
        </div>
      </a>
      <!-- Nav -->
      <nav class="wt__nav">
        <a href="#hero">Accueil</a>
        <a href="#carousel">BVMT</a>
        <a href="#about">Fonds de Garantie</a>
        <a href="#market">Marché</a>
        <a href="#footer">À propos</a>
      </nav>
      <!-- Right -->
      <div class="wt__right">
        <span class="wt__badge"><span class="wt__badge-dot"></span>Marché ouvert</span>
        <span class="wt__date">{{ dateStr }}</span>
        <button class="wt__btn" (click)="connectClick.emit()">Connexion</button>
      </div>
    </div>
  </header>
</ng-container>

<!-- ══════════════════════════════════ DASHBOARD TOPBAR ══════════════════════════════════ -->
<ng-container *ngIf="!isWelcome">
  <!-- TOP STRIP: logo · title · bell · user only -->
  <div class="dt">
    <!-- Brand (left) -->
    <a class="dt__brand" href="/welcome">
      <div class="dt__logo-box">
        <img src="assets/bvmt-logo.png" alt="BVMT" class="dt__logo-img" />
      </div>
      <div class="dt__brand-text">
        <div class="dt__brand-name">FGM BVMT</div>
        <div class="dt__brand-sub">Fonds de Garantie de Marché</div>
      </div>
    </a>

    <!-- Right icons -->
    <div class="dt__right">
      <!-- Market badge -->
      <div class="dt__market"><span class="dt__market-dot"></span>Séance ouverte</div>

      <!-- Bell -->
      <div class="dt__bell-wrap">
        <button class="dt__bell" (click)="toggleBell($event)" [class.dt__bell--active]="unreadCount > 0">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          <span *ngIf="unreadCount > 0" class="dt__bell-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
        </button>

        <!-- Alerts panel -->
        <div *ngIf="bellOpen" class="dt__bell-panel" (click)="$event.stopPropagation()">
          <div class="dt__bell-header">
            <span>Alertes <span class="dt__bell-count">{{ alerts.length }}</span></span>
            <button *ngIf="alerts.length > 0" (click)="clearAlerts($event)" class="dt__bell-clear">Effacer</button>
          </div>
          <div *ngIf="alerts.length === 0" class="dt__bell-empty">✓ Aucune alerte active</div>
          <div *ngFor="let a of alerts" class="dt__bell-item"
               [class.dt__bell-item--unread]="!a.read"
               [class.dt__bell-item--danger]="a.severity === 'danger'"
               (click)="markRead(a)">
            <span class="dt__bell-ico">{{ a.severity === 'danger' ? '⚠' : 'ℹ' }}</span>
            <div>
              <div class="dt__bell-type">{{ a.type }}</div>
              <div class="dt__bell-msg">{{ a.message }}</div>
              <div class="dt__bell-ts">{{ a.timestamp }}</div>
            </div>
            <span *ngIf="!a.read" class="dt__bell-dot"></span>
          </div>
        </div>
      </div>

      <!-- User chip -->
      <div class="dt__user">
        <div class="dt__avatar">{{ userInitials }}</div>
        <div class="dt__user-info">
          <div class="dt__user-name">{{ userName }}</div>
          <div class="dt__user-role">{{ userRoleLabel }}</div>
        </div>
      </div>
    </div>
  </div>

</ng-container>
  `,
  styles: [`
    /* ── WELCOME TOPBAR ──────────────────────────────────────────── */
    .wt {
      position: sticky; top: 0; z-index: 200; width: 100%;
      background: rgba(28,44,28,0.96);
      border-bottom: 1px solid rgba(61,107,82,.18);
      transition: background .3s, box-shadow .3s, backdrop-filter .3s;
    }
    .wt--scrolled {
      background: rgba(20,34,20,0.78);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      box-shadow: 0 4px 32px rgba(20,40,20,.22);
    }
    .wt__inner {
      max-width: 1440px; margin: 0 auto;
      display: flex; align-items: center;
      height: 60px; padding: 0 32px; gap: 20px;
    }
    .wt__brand { display: flex; align-items: center; gap: 12px; text-decoration: none; flex-shrink: 0; }
    .wt__logo  { height: 34px; width: auto; filter: brightness(0) invert(1) opacity(.9); }
    .wt__name  { font-size: 15px; font-weight: 700; color: #fff; letter-spacing: .02em; }
    .wt__dot   { color: #6bb888; }
    .wt__sub   { font-size: 9px; letter-spacing: .18em; text-transform: uppercase; color: rgba(255,255,255,.4); }
    .wt__nav   { display: flex; flex: 1; justify-content: center; gap: 0; }
    .wt__nav a {
      display: inline-flex; align-items: center; height: 60px;
      padding: 0 14px; font-size: 12px; font-weight: 600;
      text-transform: uppercase; letter-spacing: .08em;
      color: rgba(255,255,255,.65); text-decoration: none;
      border-bottom: 2px solid transparent;
      transition: color .2s, border-color .2s;
    }
    .wt__nav a:hover { color: #fff; border-bottom-color: #6bb888; }
    .wt__right { display: flex; align-items: center; gap: 14px; flex-shrink: 0; }
    .wt__badge {
      display: inline-flex; align-items: center; gap: 6px;
      background: rgba(107,184,136,.15); border: 1px solid rgba(107,184,136,.3);
      color: #88d4a8; font-size: 10px; font-weight: 700;
      padding: 4px 10px; border-radius: 100px;
      text-transform: uppercase; letter-spacing: .08em;
    }
    .wt__badge-dot { width: 6px; height: 6px; border-radius: 50%; background: #88d4a8; animation: blink 1.5s infinite; }
    .wt__date { font-size: 11px; color: rgba(255,255,255,.4); font-family: monospace; }
    .wt__btn {
      height: 32px; padding: 0 16px; border-radius: 100px; border: none; cursor: pointer;
      background: linear-gradient(135deg, #3d6b52, #5a9470);
      color: #fff; font-size: 11px; font-weight: 700;
      text-transform: uppercase; letter-spacing: .1em;
      box-shadow: 0 4px 16px rgba(61,107,82,.3);
      transition: opacity .2s, transform .2s;
    }
    .wt__btn:hover { opacity: .88; transform: translateY(-1px); }

    /* ── DASHBOARD TOP STRIP ─────────────────────────────────────── */
    .dt {
      background: #1c2c1c;
      height: 52px;
      display: flex; align-items: center;
      justify-content: space-between;
      padding: 0 24px;
      position: sticky; top: 0; z-index: 200;
      box-shadow: 0 2px 16px rgba(20,40,20,.18);
    }
    .dt__brand { display: flex; align-items: center; gap: 12px; text-decoration: none; }
    .dt__logo-box {
      width: 34px; height: 34px;
      background: rgba(107,184,136,.15);
      border: 1px solid rgba(107,184,136,.25);
      border-radius: 8px;
      display: flex; align-items: center; justify-content: center;
      padding: 4px;
    }
    .dt__logo-img { width: 100%; height: 100%; object-fit: contain; filter: brightness(0) invert(1) opacity(.85); }
    .dt__brand-name { font-size: 15px; font-weight: 700; color: #fff; letter-spacing: .02em; }
    .dt__brand-sub  { font-size: 9px; text-transform: uppercase; letter-spacing: .16em; color: rgba(255,255,255,.35); }
    .dt__right { display: flex; align-items: center; gap: 16px; }

    .dt__market {
      display: flex; align-items: center; gap: 6px;
      background: rgba(107,184,136,.12); border: 1px solid rgba(107,184,136,.25);
      color: #88d4a8; font-size: 10px; font-weight: 700;
      padding: 4px 10px; border-radius: 100px;
      text-transform: uppercase; letter-spacing: .08em;
    }
    .dt__market-dot { width: 6px; height: 6px; border-radius: 50%; background: #88d4a8; animation: blink 1.5s infinite; }

    /* Bell */
    .dt__bell-wrap { position: relative; }
    .dt__bell {
      width: 36px; height: 36px;
      background: rgba(255,255,255,.06); border: 1px solid rgba(255,255,255,.12);
      border-radius: 8px; cursor: pointer; color: rgba(255,255,255,.7);
      display: flex; align-items: center; justify-content: center;
      position: relative; transition: all .2s;
    }
    .dt__bell:hover, .dt__bell--active { background: rgba(107,184,136,.15); border-color: rgba(107,184,136,.3); color: #88d4a8; }
    .dt__bell-badge {
      position: absolute; top: -4px; right: -4px;
      background: #c84040; color: #fff; border-radius: 50%;
      width: 16px; height: 16px; font-size: 9px; font-weight: 800;
      display: flex; align-items: center; justify-content: center;
      border: 1.5px solid #1c2c1c;
    }
    .dt__bell-panel {
      position: absolute; top: calc(100% + 8px); right: 0;
      width: 340px; background: #fff;
      border: 1px solid #e4e8e4; border-radius: 12px;
      box-shadow: 0 16px 48px rgba(20,40,20,.16); z-index: 500;
      max-height: 440px; overflow-y: auto;
    }
    .dt__bell-header {
      padding: 14px 16px; border-bottom: 1px solid #f0f4f0;
      display: flex; align-items: center; justify-content: space-between;
      font-size: 12px; font-weight: 700; color: #1c2c1c;
    }
    .dt__bell-count {
      display: inline-flex; align-items: center; justify-content: center;
      background: #e4ede8; color: #3d6b52; border-radius: 100px;
      font-size: 10px; font-weight: 700; padding: 1px 7px; margin-left: 6px;
    }
    .dt__bell-clear { background: none; border: none; font-size: 11px; color: #aaa; cursor: pointer; }
    .dt__bell-empty { padding: 24px 16px; text-align: center; color: #aaa; font-size: 12px; }
    .dt__bell-item {
      display: flex; align-items: flex-start; gap: 10px;
      padding: 12px 16px; border-bottom: 1px solid #f5f5f5; cursor: pointer;
      transition: background .15s;
    }
    .dt__bell-item:hover { background: #f9fbf9; }
    .dt__bell-item--unread { background: #f4fbf6; }
    .dt__bell-item--danger.dt__bell-item--unread { background: #fff5f5; }
    .dt__bell-ico { font-size: 14px; margin-top: 1px; flex-shrink: 0; }
    .dt__bell-type { font-size: 11px; font-weight: 700; color: #3d6b52; margin-bottom: 2px; }
    .dt__bell-item--danger .dt__bell-type { color: #c84040; }
    .dt__bell-msg  { font-size: 11px; color: #444; word-break: break-word; }
    .dt__bell-ts   { font-size: 10px; color: #bbb; margin-top: 3px; }
    .dt__bell-dot  { width: 7px; height: 7px; background: #3d6b52; border-radius: 50%; margin-top: 4px; flex-shrink: 0; }
    .dt__bell-item--danger .dt__bell-dot { background: #c84040; }

    /* User */
    .dt__user {
      display: flex; align-items: center; gap: 8px;
      padding: 4px 10px; border-radius: 100px;
      border: 1px solid rgba(255,255,255,.12); cursor: pointer;
      transition: background .2s;
    }
    .dt__user:hover { background: rgba(255,255,255,.06); }
    .dt__avatar {
      width: 26px; height: 26px; border-radius: 50%;
      background: linear-gradient(135deg, #3d6b52, #5a9470);
      display: flex; align-items: center; justify-content: center;
      font-size: 10px; font-weight: 700; color: #fff; flex-shrink: 0;
    }
    .dt__user-name { color: #fff; font-size: 12px; font-weight: 600; line-height: 1.2; }
    .dt__user-role { color: rgba(255,255,255,.4); font-size: 9px; text-transform: uppercase; letter-spacing: .1em; }

    @keyframes blink { 0%,100%{opacity:1}50%{opacity:.3} }
  `],
})
export class TopbarComponent implements OnInit, OnDestroy {
  @Input() isWelcome  = false;
  @Input() activePage = 'dashboard';

  @Input() set newAlerts(alerts: any[]) {
    if (!alerts?.length) return;
    for (const a of alerts) {
      const key = a.type + '|' + a.message;
      if (!this._seen.has(key)) {
        this._seen.add(key);
        this.alerts.unshift({ ...a, read: false, severity: this._sev(a) });
        if (this.alerts.length > 50) this.alerts.pop();
      }
    }
    this._recalc();
  }

  @Input()  lang = 'fr';

  @Output() connectClick = new EventEmitter<void>();
  @Output() pageChange   = new EventEmitter<string>();
  @Output() langChange   = new EventEmitter<Lang>();

  dateStr     = '';
  scrolled    = false;
  bellOpen    = false;
  unreadCount = 0;
  alerts: any[] = [];

  private _timer: any;
  private _seen = new Set<string>();

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void  { this._tick(); this._timer = setInterval(() => this._tick(), 30000); }
  ngOnDestroy(): void { clearInterval(this._timer); }

  @HostListener('window:scroll')
  onScroll(): void { this.scrolled = window.scrollY > 24; }

  @HostListener('document:click')
  onDocClick(): void { if (this.bellOpen) this.bellOpen = false; }

  toggleBell(e: Event): void {
    e.stopPropagation();
    this.bellOpen = !this.bellOpen;
    if (this.bellOpen) { this.alerts.forEach(a => a.read = true); this.unreadCount = 0; }
  }
  markRead(a: any): void { a.read = true; this._recalc(); }
  clearAlerts(e: Event): void { e.stopPropagation(); this.alerts = []; this._seen.clear(); this.unreadCount = 0; }

  get userInitials(): string {
    return (this.auth.currentUser?.fullName ?? 'OP').split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0,2);
  }
  get userName():      string { return this.auth.currentUser?.fullName ?? 'Opérateur'; }
  get userRoleLabel(): string {
    const r = this.auth.currentUser?.roles[0] as string | undefined;
    if (!r) return '';
    return ({ ADMIN:'Administrateur', ADMIN_FGM:'Admin FGM', USER:'Utilisateur', AUDITEUR:'Auditeur', SUPERVISEUR:'Superviseur', INTERMEDIAIRE:'Intermédiaire' } as Record<string, string>)[r] ?? r;
  }
  get canViewContributions(): boolean { return this.auth.hasAnyRole('ADMIN','ADMIN_FGM','SUPERVISEUR'); }

  async logout(): Promise<void> { await this.auth.logout(); }
  toggleUserMenu(): void {}

  private _recalc(): void { this.unreadCount = this.alerts.filter(a => !a.read).length; }
  private _sev(a: any): string { return (a.type?.includes('TITRES') || a.type === 'ANNULATION') ? 'danger' : 'warn'; }
  private _tick(): void {
    this.dateStr = new Date().toLocaleDateString('fr-FR', { weekday:'short', day:'2-digit', month:'short', year:'numeric' })
      + ' · ' + new Date().toLocaleTimeString('fr-FR', { hour:'2-digit', minute:'2-digit' });
  }
}
