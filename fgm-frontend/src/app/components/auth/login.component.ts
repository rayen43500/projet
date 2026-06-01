import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, FgmRole } from '../../services/auth.service';
import { TranslationService } from '../../services/translation.service';

type Step = 'role' | 'credentials';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<div class="auth-page">
  <div class="auth-page__glow-1"></div>
  <div class="auth-page__glow-2"></div>

  <div class="auth-card">

    <!-- Language toggle -->
    <div class="auth-lang-toggle">
      <button [class.active]="ts.lang==='fr'" (click)="ts.setLang('fr')">FR</button>
      <span>|</span>
      <button [class.active]="ts.lang==='en'" (click)="ts.setLang('en')">EN</button>
    </div>

    <!-- Logo -->
    <div class="auth-card__logo">
      <div class="auth-card__logo-box">
        <img src="assets/bvmt-logo.png" alt="BVMT Logo" />
      </div>
      <div>
        <div class="auth-card__logo-name">FGM · BVMT</div>
        <div class="auth-card__logo-sub">Fonds de Garantie de Marché</div>
      </div>
    </div>

    <h1>{{ ts.t('login.title') }}</h1>

    <!-- ── STEP 1: Role selector ── -->
    <ng-container *ngIf="step === 'role'">
      <p class="auth-card__desc">{{ ts.t('login.selectRole') }}</p>
      <div class="auth-roles__list" style="margin-top:20px">
        <button class="auth-role-badge admin" (click)="selectRole('ADMIN_FGM')">
          <span class="auth-role-badge__dot"></span>
          Administrateur
        </button>
        <button class="auth-role-badge user" (click)="selectRole('USER')">
          <span class="auth-role-badge__dot"></span>
          Utilisateur
        </button>
      </div>
    </ng-container>

    <!-- ── STEP 2: Credentials ── -->
    <ng-container *ngIf="step === 'credentials'">
      <button class="auth-back-btn" (click)="step = 'role'">← {{ selectedRoleLabel }}</button>

      <div class="auth-field">
        <label>{{ ts.t('login.address') }}</label>
        <input
          type="text"
          autocomplete="username"
          [placeholder]="ts.t('login.addressPlaceholder')"
          [(ngModel)]="address"
          (keydown.enter)="submit()"
          [disabled]="loading"
        />
      </div>

      <div class="auth-field">
        <label>{{ ts.t('login.password') }}</label>
        <input
          type="password"
          autocomplete="current-password"
          [placeholder]="ts.t('login.passwordPlaceholder')"
          [(ngModel)]="password"
          (keydown.enter)="submit()"
          [disabled]="loading"
        />
      </div>

      <!-- Error message -->
      <div *ngIf="errorKey" class="auth-error">
        {{ ts.t('login.' + errorKey) }}
      </div>

      <button
        class="auth-card__connect-btn"
        (click)="submit()"
        [disabled]="loading || !address || !password"
      >
        <span *ngIf="loading" class="auth-card__spinner"></span>
        <span *ngIf="!loading">{{ ts.t('login.submit') }}</span>
        <span *ngIf="loading">{{ ts.t('login.connecting') }}</span>
      </button>
    </ng-container>

    <div class="auth-card__footer">
      {{ ts.t('login.footer') }}
    </div>
  </div>
</div>

<style>
/* Role selector */
.auth-roles__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.auth-role-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border: 1px solid rgba(255,255,255,.15);
  border-radius: 8px;
  background: rgba(255,255,255,.05);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background .15s, border-color .15s;
  text-align: left;
}
.auth-role-badge:hover { background: rgba(255,255,255,.12); border-color: rgba(255,255,255,.3); }
.auth-role-badge__dot  { width: 9px; height: 9px; border-radius: 50%; flex-shrink: 0; }
.auth-role-badge.admin .auth-role-badge__dot         { background: #c8922a; }
.auth-role-badge.user .auth-role-badge__dot          { background: #3d6b52; }

/* Lang toggle */
.auth-lang-toggle {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255,255,255,.5);
}
.auth-lang-toggle button {
  background: none;
  border: none;
  color: rgba(255,255,255,.45);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 3px;
  transition: color .15s;
}
.auth-lang-toggle button.active { color: #fff; }
.auth-lang-toggle button:hover  { color: rgba(255,255,255,.8); }

/* Back button */
.auth-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: rgba(255,255,255,.55);
  font-size: 12px;
  cursor: pointer;
  margin-bottom: 16px;
  padding: 0;
  transition: color .15s;
}
.auth-back-btn:hover { color: rgba(255,255,255,.9); }

/* Fields */
.auth-field {
  margin-bottom: 14px;
}
.auth-field label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: rgba(255,255,255,.65);
  margin-bottom: 6px;
  letter-spacing: .04em;
  text-transform: uppercase;
}
.auth-field input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(255,255,255,.18);
  border-radius: 6px;
  background: rgba(255,255,255,.07);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: border-color .15s, background .15s;
  box-sizing: border-box;
}
.auth-field input::placeholder { color: rgba(255,255,255,.3); }
.auth-field input:focus {
  border-color: rgba(255,255,255,.4);
  background: rgba(255,255,255,.1);
}
.auth-field input:disabled { opacity: .5; cursor: not-allowed; }

/* Error */
.auth-error {
  background: rgba(214,51,51,.18);
  border: 1px solid rgba(214,51,51,.35);
  border-radius: 6px;
  color: #ff9090;
  font-size: 13px;
  padding: 10px 14px;
  margin-bottom: 14px;
}

/* Auth card position fix */
.auth-card { position: relative; }
</style>
  `,
})
export class LoginComponent implements OnInit {
  step: Step = 'role';
  selectedRole: FgmRole | null = null;
  address  = '';
  password = '';
  loading  = false;
  errorKey: string | null = null;

  constructor(
    private auth: AuthService,
    private router: Router,
    public  ts: TranslationService,
  ) {}

  ngOnInit(): void {
    if (this.auth.isAuthenticated) {
      this._navigateByRole();
    }
  }

  get selectedRoleLabel(): string {
    if (!this.selectedRole) return '';
    const map: Record<FgmRole, string> = {
      ADMIN_FGM:     'Administrateur',
      USER:          'Utilisateur',
    };
    return map[this.selectedRole];
  }

  selectRole(role: FgmRole): void {
    this.selectedRole = role;
    this.step = 'credentials';
    this.errorKey = null;
    this.address  = '';
    this.password = '';
  }

  async submit(): Promise<void> {
    if (!this.address || !this.password || this.loading) return;
    this.errorKey = null;
    this.loading  = true;

    try {
      const error = await this.auth.loginWithCredentials(this.address, this.password);
      if (error === null) {
        // success — verify the token actually contains the selected role
        if (this.selectedRole && !this.auth.hasRole(this.selectedRole)) {
          this.auth.logout();
          this.errorKey = 'errorRole'; // wrong role for this account
        }
        // navigation handled inside service
      } else if (error === 'errorServer') {
        this.errorKey = 'errorServer';
      } else {
        this.errorKey = 'errorInvalid';
      }
    } catch {
      this.errorKey = 'errorServer';
    } finally {
      this.loading = false;
    }
  }

  private _navigateByRole(): void {
    if (this.auth.hasRole('INTERMEDIAIRE')) {
      this.router.navigate(['/intermediaire']);
    } else {
      this.router.navigate(['/dashboard']);
    }
  }
}