// ── Séance ────────────────────────────────────────────────────────────────────
export type SeanceStatut = 'PREPAREE' | 'OUVERTE' | 'CLOTUREE' | 'ANNULEE' | 'INCONNUE';

export interface Seance {
  id: string;
  dateSeance: string;
  statut: SeanceStatut;
  heureOuverture: string;
  heureCloture: string;
  nbIntermediaires: number;
  nbTransactions: number;
  volumeTND: number;
  motifAnnulation: string | null;
  anomalies: string[];
  createdAt: string;
  updatedAt: string;
}

// ── Intermédiaire ─────────────────────────────────────────────────────────────
export interface Intermediaire {
  id: string;
  codeIntermediaire: string;
  libelleCourtIntermediaire: string;
  libelleLongIntermediaire: string;
  numeroCompteIntermediaire: string;
  adresseIntermediaire: string;
  codeBanque: number;
  typeBanque: number;
  dateImport: string | null;
  dateDernierImport: string | null;
}

// ── Position Nette (Positionnette) ────────────────────────────────────────────
export type TypeRisque = 'AUCUN' | 'DEFAUT_TITRES' | 'DEFAUT_ESPECES';
export type PositionStatut = 'NORMAL' | 'CRITICAL';

export interface PositionNette {
  id: string;
  codeIntermediaire: string;
  nomIntermediaire: string;
  codeIsin?: string;
  libelleValeur: string;
  coursCloture: number;
  cours: number;
  pnt: number;
  pne: number;
  pntSign: string;
  pneSign: string;
  quantiteAchetee: number;
  quantiteVendue: number;
  quantiteNette: number;
  montantNette: number;
  risqueJ: number;
  risqueJ1: number;
  rm: number;
  typeRisque: TypeRisque;
  statut: PositionStatut;
}

// ── Risque ────────────────────────────────────────────────────────────────────
export interface Risque {
  id: string;
  seance: string;
  intermediaire: string;
  valeur: string;
  cloture: number;
  quantitenette: number;
  montantnette: number;
  quantitenettej_1: number;
  montantnettej_1: number;
  pntj: string;
  pntj_1: string;
  risquej: number;
  risquej_1: number;
  codeIntermediaire: string;
}

// ── Transaction ───────────────────────────────────────────────────────────────
export interface Transaction {
  id: string;
  seance: string;
  codeValeur: string;
  libelleValeur: string;
  codeIntermediaireAcheteur: string;
  libelleIntermediaireAcheteur: string;
  codeIntermediaireVendeur: string;
  libelleIntermediaireVendeur: string;
  quantiteNegociee: number;
  coursTransaction: number;
  volume: number;
}

// ── Valeur ────────────────────────────────────────────────────────────────────
export interface Valeur {
  id: string;
  seance: string;
  codeValeur: string;
  libelleValeur: string;
  veille: number;
  cloture: number;
}

// ── Banque ────────────────────────────────────────────────────────────────────
export interface Banque {
  id: string;
  cBque: number;
  cBqueComp: string;
  lCourBque: string;
  lLongBque: string;
  adrBque: string;
  faxBque: string;
  bic: string;
}

// ── Provision ─────────────────────────────────────────────────────────────────
export interface Provision {
  id: string;
  D_CALC_PROV: string;
  C_ID_ADC: number;
  MT_PROV: number;
  MT_APP_INI: number;
  MT_RISQ_TOT: number;
  intermediaire: any;
}

// ── Mouvement Bancaire ────────────────────────────────────────────────────────
export interface MouvementBancaire {
  id: string;
  seance: string;
  intermediaire: string;
  codeIntermediaire: string;
  totalSeance: number;
  totalSeancePrecedent: number;
  total: number;
  provision: number;
  difference: number;
  appel: number;
  restitution: number;
  apportInitial: number;
}

// ── Appel Restitution ─────────────────────────────────────────────────────────
export interface AppelRestitution {
  id: string;
  appel: number;
  intermediaire: string;
  codeIntermediaire: string;
  adresse: string;
  fax: string;
  dateSeance: string;
  dateValeur: string;
  risque: number;
  provision: number;
  restitution: number;
  numeroCompte: string;
}

// ── Apport Initial ────────────────────────────────────────────────────────────
export interface ApportInitial {
  id: string;
  seance: string;
  codeInterm: string;
  intermediaire: string;
  positionAch: number;
  positionVenduEns: number;
  apportInitial: number;
  apportInitialAjuste: number;
  appelContrib: number;
  restitution: number;
  debut: string;
  fin: string;
  moyequot: number;
}

// ── Placement ─────────────────────────────────────────────────────────────────
export interface Placement {
  id: string;
  sessionDate: string;
  codeIntermediaire: number;
  intermediaire: string;
  mtProv: number;
  montantSaisi: number;
  cumule: number;
  soldePlace: number;
  totalProvision: number;
  totalCumule: number;
  totalMontantSaisi: number;
  totalSoldePlace: number;
  divers: number;
  totalGeneral: number;
  interet: number;
}

// ── TMM ───────────────────────────────────────────────────────────────────────
export interface Tmm {
  id: string;
  MOIS: string;
  ANNEE: number;
  TMM: number;
}

// ── Paramétrage ───────────────────────────────────────────────────────────────
export interface Parametrage {
  id: string;
  seuil_var_1: number;
  seuil_var_2: number;
  seuil_var_3: number;
  seuil_dep_pro: number;
  dep_risq: number;
  min_contr_init: number;
  del_reg_liv: number;
  del_reg_DT: number;
  del_reg_DE: number;
  taux: number;
  benefice: number;
}

// ── MvtBanqueInter ────────────────────────────────────────────────────────────
export interface MvtBanqueInter {
  id: string;
  intermediaire: string;
  codeInterm: string;
  banque: string;
  numeroCompte: string;
  dateSeance: string;
  dateValeur: string;
  debit: number;
  credit: number;
  total: number;
  soldeCredit: number;
  CBQUECOMP: string;
}

// ── Risque Global (WebSocket) ──────────────────────────────────────────────────
export interface RisqueGlobal {
  dateSeance: string;
  rm: number;
  rTotal: number;
  provision: number;
  tauxCouverture: number;
  nbPositions: number;
  nbCritiques: number;
  timestamp: string;
}

// ── Alerte (WebSocket) ────────────────────────────────────────────────────────
export interface Alerte {
  intermediaire: string;
  isin: string;
  valeur: string;
  risqueJ: number;
  type: string;
  message: string;
  timestamp: string;
}

// ── Seance WebSocket event ────────────────────────────────────────────────────
export interface SeanceEvent {
  event?: string;
  dateSeance: string;
  statut?: string;
  motif?: string;
  nbTransactions?: number;
  nbIntermediaires?: number;
  heureOuverture?: string;
  heureCloture?: string;
  timestamp?: string;
}

// ── Dashboard KPI ─────────────────────────────────────────────────────────────
export interface DashboardKpi {
  rTotal: number;
  provision: number;
  nbIntermediaires: number;
  nbSuspens: number;
  tauxCouverture: number;
}

// ── Feuille Appel de Marge ────────────────────────────────────────────────────
export interface FeuilleAppelMarge {
  codeIntermediaire?: string;
  nomIntermediaire: string;
  rmSeanceJ?: number;
  rmSeanceJ1?: number;
  rVal: number;
  rSusp: number;
  total: number;
  provision: number;
  difference: number;
  appel: number;
  restitution: number;
  apportInitial?: number;
  defaillant: boolean;
  pct?: number;
}
