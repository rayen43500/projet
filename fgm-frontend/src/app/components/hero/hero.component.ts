import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, FgmRole } from '../../services/auth.service';
import { TranslationService } from '../../services/translation.service';

type Step = 'role' | 'credentials';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<section id="hero" class="fgm-hero" aria-labelledby="hero-title">
  <div class="fgm-hero__bg">
    <img src="assets/bvmt-data.webp" alt="" aria-hidden="true" />
    <div class="fgm-hero__bg-overlay"></div>
  </div>
  <div class="fgm-hero__glow"></div>

  <div class="fgm-hero__content">

    <!-- LEFT: text -->
    <div style="display:flex;flex-direction:column;justify-content:center">
      <span class="fgm-hero__eyebrow">
        <span class="fgm-hero__eyebrow-dot"></span>
        BVMT · Bourse des Valeurs Mobilières de Tunis
      </span>
      <h1 id="hero-title">Plateforme Intelligente du Fonds de Garantie de Marché</h1>
      <p class="fgm-hero__desc">
        Surveillance temps réel des positions nettes, calcul automatisé du risque
        de marché et appels de marge — au cœur de l'intégrité du marché tunisien.
      </p>
      <div class="fgm-hero__actions">
        <a class="fgm-hero__btn-outline" href="#about">Découvrir le FGM</a>
        <a class="fgm-hero__btn-outline" href="#market">Aperçu du marché</a>
      </div>
    </div>

    <!-- RIGHT: Login card -->
    <div class="fgm-hero__card">
      <div class="fgm-hero__card-glow"></div>
      <div class="fgm-hero__card-inner hero-login-card">

        <!-- Brand + lang toggle -->
        <div class="hlc-top-row">
          <div class="hlc-brand">
            <div class="hlc-logo-box"><img src="assets/bvmt-logo.png" alt="BVMT" /></div>
            <div>
              <div class="hlc-title">FGM · BVMT</div>
              <div class="hlc-sub">Accès sécurisé</div>
            </div>
          </div>
          <div class="hlc-lang">
            <button [class.active]="ts.lang==='fr'" (click)="ts.setLang('fr')">FR</button>
            <span>|</span>
            <button [class.active]="ts.lang==='en'" (click)="ts.setLang('en')">EN</button>
          </div>
        </div>

        <div class="hlc-divider"></div>

        <!-- Already logged in -->
        <ng-container *ngIf="isLoggedIn; else notLoggedIn">
          <div class="hlc-welcome">
            <div class="hlc-avatar">{{ initials }}</div>
            <div>
              <div class="hlc-username">{{ userName }}</div>
              <div class="hlc-rolelabel">{{ roleLabel }}</div>
            </div>
          </div>
          <button class="hlc-btn-primary" (click)="goToDashboard()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/>
            </svg>
            {{ ts.t('nav.dashboard') }}
          </button>
          <button class="hlc-btn-ghost" (click)="logout()">{{ ts.t('common.logout') }}</button>
        </ng-container>

        <!-- Not logged in -->
        <ng-template #notLoggedIn>
          <h2 class="hlc-heading">{{ ts.t('login.title') }}</h2>

          <!-- Step 1: Role selector -->
          <ng-container *ngIf="step === 'role'">
            <p class="hlc-desc">{{ ts.t('login.selectRole') }}</p>
            <div class="hlc-roles">
              <button class="hlc-role-btn admin"         (click)="selectRole('ADMIN_FGM')">
                <span class="rdot"></span>{{ ts.t('login.roleAdmin') }}
              </button>
              <button class="hlc-role-btn superviseur"   (click)="selectRole('SUPERVISEUR')">
                <span class="rdot"></span>{{ ts.t('login.roleSuperviseur') }}
              </button>
              <button class="hlc-role-btn intermediaire" (click)="selectRole('INTERMEDIAIRE')">
                <span class="rdot"></span>{{ ts.t('login.roleIntermediaire') }}
              </button>
            </div>
          </ng-container>

          <!-- Step 2: Credentials -->
          <ng-container *ngIf="step === 'credentials'">
            <button class="hlc-back" (click)="step='role'; errorKey=null">← {{ selectedRoleLabel }}</button>

            <div class="hlc-field">
              <label>{{ ts.t('login.address') }}</label>
              <input type="text" autocomplete="username"
                [placeholder]="ts.t('login.addressPlaceholder')"
                [(ngModel)]="address" (keydown.enter)="submit()" [disabled]="loading" />
            </div>
            <div class="hlc-field">
              <label>{{ ts.t('login.password') }}</label>
              <input type="password" autocomplete="current-password"
                [placeholder]="ts.t('login.passwordPlaceholder')"
                [(ngModel)]="password" (keydown.enter)="submit()" [disabled]="loading" />
            </div>

            <div *ngIf="errorKey" class="hlc-error">{{ ts.t('login.' + errorKey) }}</div>

            <button class="hlc-btn-primary" (click)="submit()"
              [disabled]="loading || !address || !password">
              <span *ngIf="loading" class="hlc-spinner"></span>
              <svg *ngIf="!loading" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 4l6 2.67V11c0 3.85-2.58 7.45-6 8.74C8.58 18.45 6 14.85 6 11V7.67L12 5z"/>
              </svg>
              {{ loading ? ts.t('login.connecting') : ts.t('login.submit') }}
            </button>
          </ng-container>
        </ng-template>

        <div class="hlc-footer">{{ ts.t('login.footer') }}</div>
      </div>
    </div>

  </div>
</section>
  `,
  styles: [`
    .hero-login-card { display:flex; flex-direction:column; gap:0; padding:28px 32px; }

    .hlc-top-row { display:flex; align-items:flex-start; justify-content:space-between; gap:8px; margin-bottom:4px; }
    .hlc-brand   { display:flex; align-items:center; gap:12px; }
    .hlc-logo-box { width:40px; height:40px; background:rgba(255,255,255,.95); border-radius:6px; padding:4px; flex-shrink:0; }
    .hlc-logo-box img { width:100%; height:100%; object-fit:contain; }
    .hlc-title { font-family:'Roboto Condensed',sans-serif; font-size:1rem; font-weight:700; color:#fff; }
    .hlc-sub   { font-size:10px; text-transform:uppercase; letter-spacing:.15em; color:rgba(255,255,255,.5); }

    .hlc-lang { display:flex; align-items:center; gap:5px; font-size:11px; color:rgba(255,255,255,.35); flex-shrink:0; }
    .hlc-lang button { background:none; border:none; color:rgba(255,255,255,.4); font-size:11px; font-weight:700; cursor:pointer; padding:2px 4px; border-radius:3px; transition:color .15s; }
    .hlc-lang button.active { color:#fff; }
    .hlc-lang button:hover  { color:rgba(255,255,255,.8); }

    .hlc-divider { height:1px; background:rgba(255,255,255,.12); margin:16px 0; }
    .hlc-heading { font-family:'Roboto Condensed',sans-serif; font-size:1.15rem; font-weight:700; color:#fff; margin:0 0 6px 0; }
    .hlc-desc    { font-size:12px; line-height:1.5; color:rgba(255,255,255,.6); margin-bottom:14px; }

    .hlc-roles  { display:flex; flex-direction:column; gap:8px; margin-bottom:4px; }
    .hlc-role-btn {
      display:flex; align-items:center; gap:10px; padding:11px 16px;
      border:1px solid rgba(255,255,255,.15); background:rgba(255,255,255,.06);
      border-radius:8px; color:#fff; font-size:13px; font-weight:700;
      text-transform:uppercase; letter-spacing:.06em;
      cursor:pointer; transition:background .15s, border-color .15s; text-align:left;
    }
    .hlc-role-btn:hover { background:rgba(255,255,255,.13); border-color:rgba(255,255,255,.3); }
    .rdot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
    .admin         .rdot { background:#c8922a; }
    .superviseur   .rdot { background:#3d6b52; }
    .intermediaire .rdot { background:#3a5d9a; }

    .hlc-back { display:inline-flex; align-items:center; gap:6px; background:none; border:none; color:rgba(255,255,255,.5); font-size:11px; cursor:pointer; padding:0; margin-bottom:14px; transition:color .15s; }
    .hlc-back:hover { color:rgba(255,255,255,.9); }

    .hlc-field { margin-bottom:12px; }
    .hlc-field label { display:block; font-size:11px; font-weight:700; color:rgba(255,255,255,.55); margin-bottom:5px; text-transform:uppercase; letter-spacing:.06em; }
    .hlc-field input { width:100%; padding:9px 13px; border:1px solid rgba(255,255,255,.18); border-radius:6px; background:rgba(255,255,255,.07); color:#fff; font-size:13px; outline:none; transition:border-color .15s, background .15s; box-sizing:border-box; }
    .hlc-field input::placeholder { color:rgba(255,255,255,.3); }
    .hlc-field input:focus { border-color:rgba(255,255,255,.4); background:rgba(255,255,255,.1); }
    .hlc-field input:disabled { opacity:.5; cursor:not-allowed; }

    .hlc-error { background:rgba(214,51,51,.18); border:1px solid rgba(214,51,51,.35); border-radius:6px; color:#ff9090; font-size:12px; padding:8px 12px; margin-bottom:12px; }

    .hlc-btn-primary { display:flex; align-items:center; justify-content:center; gap:8px; width:100%; padding:12px 20px; background:var(--gradient-gold); color:#fff; font-family:'Open Sans',sans-serif; font-size:13px; font-weight:700; border:none; border-radius:6px; cursor:pointer; box-shadow:var(--shadow-gold); transition:var(--transition-base); margin-bottom:10px; margin-top:4px; }
    .hlc-btn-primary:hover:not(:disabled) { opacity:.92; transform:translateY(-1px); }
    .hlc-btn-primary:disabled { opacity:.55; cursor:not-allowed; transform:none; }

    .hlc-btn-ghost { display:block; width:100%; padding:9px; background:transparent; border:1px solid rgba(255,255,255,.2); border-radius:6px; color:rgba(255,255,255,.7); font-size:12px; font-weight:600; cursor:pointer; transition:var(--transition-base); }
    .hlc-btn-ghost:hover { background:rgba(255,255,255,.08); }

    .hlc-spinner { width:15px; height:15px; border:2px solid rgba(255,255,255,.3); border-top-color:#fff; border-radius:50%; animation:spin .7s linear infinite; }
    @keyframes spin { to { transform:rotate(360deg); } }

    .hlc-welcome { display:flex; align-items:center; gap:12px; background:rgba(255,255,255,.07); border:1px solid rgba(255,255,255,.12); border-radius:8px; padding:12px 14px; margin-bottom:16px; }
    .hlc-avatar  { width:38px; height:38px; background:var(--gradient-gold); border-radius:50%; display:flex; align-items:center; justify-content:center; font-family:'Roboto Condensed',sans-serif; font-size:14px; font-weight:700; color:#fff; flex-shrink:0; }
    .hlc-username  { font-size:13px; font-weight:700; color:#fff; }
    .hlc-rolelabel { font-size:10px; text-transform:uppercase; letter-spacing:.12em; color:hsl(var(--gold-soft)); margin-top:2px; }

    .hlc-footer { margin-top:16px; font-size:10px; text-align:center; color:rgba(255,255,255,.3); }
  `],
})
export class HeroComponent implements OnInit {
  step: Step = 'role';
  selectedRole: FgmRole | null = null;
  address  = '';
  password = '';
  loading  = false;
  errorKey: string | null = null;
  year = new Date().getFullYear();

  constructor(
    private auth:   AuthService,
    private router: Router,
    public  ts:     TranslationService,
  ) {}

  ngOnInit(): void {}

  get isLoggedIn(): boolean { return this.auth.isAuthenticated; }
  get initials(): string {
    return (this.auth.currentUser?.fullName ?? 'OP')
      .split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2);
  }
  get userName(): string { return this.auth.currentUser?.fullName ?? ''; }
  get roleLabel(): string {
    const r = this.auth.currentUser?.roles[0];
    return r ? ({ ADMIN_FGM: this.ts.t('login.roleAdmin'), SUPERVISEUR: this.ts.t('login.roleSuperviseur'), INTERMEDIAIRE: this.ts.t('login.roleIntermediaire') } as any)[r] ?? r : '';
  }
  get selectedRoleLabel(): string {
    if (!this.selectedRole) return '';
    return ({ ADMIN_FGM: this.ts.t('login.roleAdmin'), SUPERVISEUR: this.ts.t('login.roleSuperviseur'), INTERMEDIAIRE: this.ts.t('login.roleIntermediaire') } as any)[this.selectedRole];
  }

  selectRole(role: FgmRole): void {
    this.selectedRole = role;
    this.step = 'credentials';
    this.errorKey = null;
    this.address = '';
    this.password = '';
  }

  async submit(): Promise<void> {
    if (!this.address || !this.password || this.loading) return;
    this.errorKey = null;
    this.loading  = true;
    try {
      const error = await this.auth.loginWithCredentials(this.address, this.password);
      if (error === 'errorServer') this.errorKey = 'errorServer';
      else if (error !== null)     this.errorKey = 'errorInvalid';
    } catch {
      this.errorKey = 'errorServer';
    } finally {
      this.loading = false;
    }
  }

  goToDashboard(): void {
    this.router.navigate([this.auth.hasRole('INTERMEDIAIRE') ? '/intermediaire' : '/dashboard']);
  }
  async logout(): Promise<void> { await this.auth.logout(); }
}
