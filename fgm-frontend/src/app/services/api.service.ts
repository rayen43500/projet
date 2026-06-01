import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

const BASE = `${environment.apiUrl}/api`;

@Injectable({ providedIn: 'root' })
export class ApiService {

  constructor(private http: HttpClient, private auth: AuthService) {}

  private h(): HttpHeaders {
    const t = this.auth.currentUser?.token;
    return t ? new HttpHeaders({ Authorization: `Bearer ${t}` }) : new HttpHeaders();
  }

  private authHeaders(): HttpHeaders {
    const token = this.auth.currentUser?.token;
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }

  // ── Séances ──────────────────────────────────────────────────────────────
  getHistoriqueSeances(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/seances`, { headers: this.h() }).pipe(
      catchError(() => this.http.get<any[]>(`${BASE}/seance/all`, { headers: this.h() }).pipe(catchError(() => of([]))))
    );
  }

  preparerSeance(dateSeance: string): Observable<any> {
    return this.http.post<any>(`${BASE}/seances/preparer`, { dateSeance }, { headers: this.h() }).pipe(
      catchError(() => this.http.post<any>(`${BASE}/seance/create`, { seance: dateSeance.replace(/-/g, '') }, { headers: this.h() }).pipe(catchError(this.err)))
    );
  }

  cloturerSeance(date: string): Observable<any> {
    return this.http.post<any>(`${BASE}/seances/${encodeURIComponent(date)}/cloturer`, {}, { headers: this.h() }).pipe(
      catchError(() => this.http.post<any>(`${BASE}/seance/cloturer`, { seance: date.replace(/-/g, '') }, { headers: this.h() }).pipe(catchError(this.err)))
    );
  }

  annulerSeance(date: string, motif: string): Observable<any> {
    return this.http.post<any>(`${BASE}/seances/${encodeURIComponent(date)}/annuler`, { motif }, { headers: this.h() }).pipe(
      catchError(() => this.http.post<any>(`${BASE}/seance/annuler`, { seance: date.replace(/-/g, ''), motif }, { headers: this.h() }).pipe(catchError(this.err)))
    );
  }

  detecterAnomalies(date: string): Observable<any> {
    return this.http.post<any>(`${BASE}/seances/${encodeURIComponent(date)}/detecter-anomalies`, {}, { headers: this.h() }).pipe(
      catchError(() => this.http.post<any>(`${BASE}/seance/anomalies`, { seance: date.replace(/-/g, '') }, { headers: this.h() }).pipe(catchError(this.err)))
    );
  }

  // ── Global run-all ────────────────────────────────────────────────────────
  runAll(file1: File, file2: File, file3: File, seance: string): Observable<any> {
    const form = new FormData();
    form.append('file1', file1);
    form.append('file2', file2);
    form.append('file3', file3);
    form.append('seance', seance);
    return this.http.post<any>(`${BASE}/global/run-all`, form, { headers: this.authHeaders() }).pipe(catchError(this.err));
  }

  cancelSeanceBatch(seance: string): Observable<any> {
    return this.http.delete<any>(`${BASE}/global/cancel?seance=${seance}`, { headers: this.h() }).pipe(catchError(this.err));
  }

  // ── Positions / Positionnette ─────────────────────────────────────────────
  getPositionnettesBySeance(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/positionnette/affiche?seance=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  getPositionsByDate(date: string): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/positions?dateSeance=${encodeURIComponent(date)}`, { headers: this.h() }).pipe(
      catchError(() => {
        const compact = date.replace(/-/g, '');
        return this.getPositionnettesBySeance(compact);
      })
    );
  }

  getPositions(seance: string): Observable<any[]> {
    return this.getPositionsByDate(seance);
  }

  runPositionnetteBatch(seance: string): Observable<any> {
    return this.http.post<any>(`${BASE}/positionnette/generate?seance=${seance}`, {}, { headers: this.h() }).pipe(catchError(this.err));
  }

  generatePositionnettePdf(seance: string): Observable<Blob> {
    return this.http.get(`${BASE}/positionnette/pdf?seance=${seance}`, { headers: this.h(), responseType: 'blob' }).pipe(catchError(this.err));
  }

  // ── Risque ────────────────────────────────────────────────────────────────
  getRisquesBySeance(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/risque/affiche?seance=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  runRisqueBatch(seance: string): Observable<any> {
    return this.http.post<any>(`${BASE}/risque/generate?seance=${seance}`, {}, { headers: this.h() }).pipe(catchError(this.err));
  }

  // ── Mouvement Bancaire ────────────────────────────────────────────────────
  getMouvementBancaireBySeance(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/mouvementbancaire/affiche?seance=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  runMouvementBancaireBatch(seance: string): Observable<any> {
    return this.http.post<any>(`${BASE}/mouvementbancaire/run?seance=${seance}`, {}, { headers: this.h() }).pipe(catchError(this.err));
  }

  // ── Provisions ────────────────────────────────────────────────────────────
  getProvisionsByDate(date: string): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/provisions?date=${date}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  // ── Apport Initial ────────────────────────────────────────────────────────
  getApportInitialBySeance(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/apport-initial/seance?seance=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  genererApportInitial(debut: string, fin: string): Observable<any> {
    return this.http.post<any>(`${BASE}/apport-initial/generate?debut=${debut}&fin=${fin}`, {}, { headers: this.h() }).pipe(catchError(this.err));
  }

  getApportInitialAll(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/apport-initial`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  // ── TMM ───────────────────────────────────────────────────────────────────
  getAllTmm(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/tmm`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  saveTmm(tmm: any): Observable<any> {
    return this.http.post<any>(`${BASE}/tmm`, tmm, { headers: this.h() }).pipe(catchError(this.err));
  }

  deleteTmm(id: string): Observable<any> {
    return this.http.delete<any>(`${BASE}/tmm/${id}`, { headers: this.h() }).pipe(catchError(this.err));
  }

  // ── Paramétrage ───────────────────────────────────────────────────────────
  getParametrage(): Observable<any> {
    return this.http.get<any>(`${BASE}/parametrage`, { headers: this.h() }).pipe(catchError(this.err));
  }

  updateParametrage(params: any): Observable<any> {
    return this.http.put<any>(`${BASE}/parametrage`, params, { headers: this.h() }).pipe(catchError(this.err));
  }

  // ── Intermédiaires ────────────────────────────────────────────────────────
  getIntermediaires(dateSeance?: string): Observable<any[]> {
    const q = dateSeance ? `?dateSeance=${encodeURIComponent(dateSeance)}` : '';
    return this.http.get<any[]>(`${BASE}/intermediaires${q}`, { headers: this.h() }).pipe(
      catchError(() => {
        if (dateSeance) {
          const compact = dateSeance.replace(/-/g, '');
          return this.http.get<any[]>(`${BASE}/intermediaire/seance/${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
        }
        return this.http.get<any[]>(`${BASE}/intermediaire/all`, { headers: this.h() }).pipe(catchError(() => of([])));
      })
    );
  }

  // ── Banques ───────────────────────────────────────────────────────────────
  getBanques(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/banque`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  getBanqueByCode(code: number): Observable<any> {
    return this.http.get<any>(`${BASE}/banque/byCode?code=${code}`, { headers: this.h() }).pipe(catchError(() => of(null)));
  }

  getBanqueEtat(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/banqueetat?seance=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  detectImportDate(txFile: File, valeursFile?: File | null): Observable<{ dateSeance: string }> {
    const form = new FormData();
    form.append('transactionsFile', txFile);
    if (valeursFile) form.append('valeursFile', valeursFile);
    return this.http.post<{ dateSeance: string }>(`${BASE}/import/detect-date`, form, { headers: this.authHeaders() }).pipe(catchError(this.err));
  }

  // ── Appel Restitution ─────────────────────────────────────────────────────
  getAppelRestitutionSem(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/appel-restitution-sem?date=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  getAppelRestitutionBySeance(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/appel-restitution?date=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  // ── Placements ────────────────────────────────────────────────────────────
  getPlacements(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/placements`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  getPlacementsByDate(date: string): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/placements/date/${date}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  // ── MvtBanqueInter ────────────────────────────────────────────────────────
  getMvtBanqueInter(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/mvt-banque/seance?date=${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  // ── Transactions / Valeurs ────────────────────────────────────────────────
  getTransactionsBySeance(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/transaction/seance/${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  getValeursBySeance(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.get<any[]>(`${BASE}/valeur/seance/${compact}`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  // ── Swift ─────────────────────────────────────────────────────────────────
  getAllSwift(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/swift`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  generateSwift(seance: string): Observable<any[]> {
    const compact = seance.replace(/-/g, '');
    return this.http.post<any[]>(`${BASE}/swift/generate/${compact}`, {}, { headers: this.h() }).pipe(catchError(this.err));
  }

  // ── Historique ────────────────────────────────────────────────────────────
  getHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/history/all`, { headers: this.h() }).pipe(catchError(() => of([])));
  }

  // ── Séance courante ───────────────────────────────────────────────────────
  getSeanceCourante(): Observable<any> {
    return this.http.get<any>(`${BASE}/seances/courante`, { headers: this.h() }).pipe(
      catchError(() => this.http.get<any>(`${BASE}/seance/courante`, { headers: this.h() }).pipe(catchError(() => of(null))))
    );
  }

  /** Recharge positions, feuille marge et stats pour une séance (après import ou au refresh) */
  getSessionDashboard(dateSeance: string): Observable<any> {
    return this.http.get<any>(`${BASE}/seances/${encodeURIComponent(dateSeance)}/dashboard`, { headers: this.h() }).pipe(
      catchError(this.err)
    );
  }

  getMyPositions(date: string): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/intermediaires/me/positions?date=${encodeURIComponent(date)}`, { headers: this.h() }).pipe(
      catchError(() => this.getPositionsByDate(date))
    );
  }

  // ── Import session ────────────────────────────────────────────────────────
  importSession(formData: FormData): Observable<any> {
    return this.http.post<any>(`${BASE}/import/session`, formData, { headers: this.authHeaders() }).pipe(catchError(this.err));
  }

  uploadImportFile(endpoint: string, formData: FormData): Observable<any> {
    return this.http.post<any>(`${BASE}/import/${endpoint}`, formData, { headers: this.authHeaders() }).pipe(catchError(this.err));
  }

  private err(error: any): Observable<never> {
    const body = error?.error;
    let msg = 'Erreur serveur';
    if (typeof body === 'string') msg = body;
    else if (body) msg = body.erreur ?? body.error ?? body.message ?? error?.message ?? msg;
    else msg = error?.message ?? msg;
    console.error('[ApiService]', msg, error);
    return throwError(() => new Error(String(msg)));
  }
}
