import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export type FgmRole = 'ADMIN_FGM' | 'SUPERVISEUR' | 'INTERMEDIAIRE' | 'ADMIN' | 'USER';

export interface FgmUser {
  username: string;
  email: string;
  fullName: string;
  roles: FgmRole[];
  token: string;
  /** Code intermédiaire — renseigné seulement pour le rôle INTERMEDIAIRE */
  codeIntermediaire?: number;
}

const TOKEN_STORAGE_KEY = 'fgm_access_token';
const JWT_CLIENT_ID = 'fgm-frontend';

/**
 * Authentification via le backend Spring (POST /api/auth/login) — JWT HS256 aligné sur les claims Keycloak-like (realm_access.roles).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private _user = new BehaviorSubject<FgmUser | null>(null);
  private _initialized = new BehaviorSubject<boolean>(false);

  user$ = this._user.asObservable();
  initialized$ = this._initialized.asObservable();

  constructor(private router: Router) {}

  get currentUser(): FgmUser | null { return this._user.value; }
  get isAuthenticated(): boolean    { return this._user.value !== null; }

  async init(): Promise<void> {
    try {
      const token = sessionStorage.getItem(TOKEN_STORAGE_KEY);
      if (token && !this._isJwtExpired(token)) {
        this._setUserFromRawToken(token);
      } else if (token) {
        sessionStorage.removeItem(TOKEN_STORAGE_KEY);
      }
    } catch (err) {
      console.warn('[AuthService] init', err);
      sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    } finally {
      this._initialized.next(true);
    }
  }

  /**
   * @returns null si succès, sinon clé d’erreur (errorInvalid, errorServer)
   */
  async loginWithCredentials(address: string, password: string): Promise<string | null> {
    if (!address?.trim()) return 'errorInvalid';
    if (!password?.trim()) return 'errorInvalid';

    try {
      const res = await fetch(`${environment.apiUrl}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ username: address.trim(), password }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        if (res.status === 401 || data.error === 'invalid_grant') {
          return 'errorInvalid';
        }
        return 'errorServer';
      }

      const data = await res.json();
      const accessToken = data.access_token as string | undefined;
      if (!accessToken) {
        return 'errorServer';
      }
      sessionStorage.setItem(TOKEN_STORAGE_KEY, accessToken);
      this._setUserFromRawToken(accessToken);
      this._navigateByRole();
      return null;
    } catch (err) {
      console.error('[AuthService] loginWithCredentials', err);
      return 'errorServer';
    }
  }

  async logout(): Promise<void> {
    this._user.next(null);
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    await this.router.navigateByUrl('/');
  }

  hasRole(role: FgmRole): boolean {
    return this._user.value?.roles.includes(role) ?? false;
  }

  hasAnyRole(...roles: FgmRole[]): boolean {
    return roles.some(r => this.hasRole(r));
  }

  private _navigateByRole(): void {
    if (this.hasRole('INTERMEDIAIRE')) {
      this.router.navigate(['/intermediaire']);
    } else {
      this.router.navigate(['/dashboard']);
    }
  }

  private _isJwtExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload?.exp;
      if (typeof exp !== 'number') return true;
      return Date.now() / 1000 >= exp - 30;
    } catch {
      return true;
    }
  }

  private _setUserFromRawToken(accessToken: string): void {
    try {
      const payload = JSON.parse(atob(accessToken.split('.')[1]));
      const realmRoles: string[] = payload?.realm_access?.roles ?? [];
      const clientRoles: string[] = payload?.resource_access?.[JWT_CLIENT_ID]?.roles ?? [];
      const allRoles = [...realmRoles, ...clientRoles];
      const fgmRoles = allRoles.filter(
        r => ['ADMIN_FGM', 'SUPERVISEUR', 'INTERMEDIAIRE'].includes(r)
      ) as FgmRole[];

      this._user.next({
        username: payload.preferred_username ?? '',
        email:    payload.email ?? '',
        fullName: payload.name ?? payload.preferred_username ?? '',
        roles:    fgmRoles,
        token:    accessToken,
        codeIntermediaire: payload.intermediaire_code != null ? Number(payload.intermediaire_code) : undefined,
      });
    } catch (err) {
      console.error('[AuthService] parse token', err);
    }
  }
}
