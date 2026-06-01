# Récapitulatif de toutes les corrections — FGM BVMT

Document de synthèse de **tout ce qui a été corrigé ou complété** sur le backend Spring Boot et le frontend Angular, conformément au document explicatif FGM (PNT, PNE, Risque J, RM, RS, netting, appel de marge).

> **Hors périmètre volontaire :** intégration **Keycloak** (auth JWT maison conservée).

---

## 1. Calculs métier (PNT / PNE / R_val / RS / RM)

### `PositionNetteCalculator`
| Élément | Correction |
|---------|------------|
| **PNT** | `quantité achetée − quantité vendue`, signe `+` / `−` |
| **PNE** | `montant versé − montant reçu`, signe selon document |
| **Règle d'exclusivité** | PNT+ et PNE+ simultanés → risque = 0, position ignorée |
| **Risque J (R_val / RV)** | 4 cas du cahier des charges avec coefficient 6 % et délai J+2 |
| **RS (risque suspens)** | `computeRsSuspens()` — valorisation au dernier cours sans coefficient de volatilité |

**Exemple document ARTES / FINAC (validé en test) :**
- R_val ≈ **628 DT** — `15,110 × 366 × (1,06)² − 5 585,5`
- RS ≈ **56 DT** — `|15,110 × 366 − 5 585,5|`

### `PositionnetteTasklet`
- Utilise `PositionNetteCalculator` pour PNT, PNE et signes
- PNE = versé − reçu (plus l'ancienne formule inversée)
- Ignore les positions en violation de la règle d'exclusivité PNT+/PNE+

### `RisqueTasklet`
- Calcul `risqueJ` et `risqueJ_1` via `computeRisqueJour()`
- Calcul et persistance de **`risqueSuspens`** via `computeRsSuspens()`
- **Exclusion J−1 dénoué** : si `dateLivraison ≤ séance courante` → `risqueJ_1 = 0`

### `MouvementBancaireTasklet`
- Agrégation par intermédiaire : RM J, RM J−1, **RS**
- **`totalRsusp`** enregistré par intermédiaire
- **`total = RM + RS`** (plus seulement RM)
- Création automatique d'un `ApportInitial` à 0 si absent (au lieu de sauter l'intermédiaire)

### `FgmDashboardService`
| Statistique | Avant | Après |
|-------------|-------|-------|
| `totalRval` | = `rmGlobal` ou 0 | = **Σ (risqueJ + risqueJ_1)** = RM global |
| `totalRsusp` | 0 (placeholder) | **Σ risqueSuspens** |
| `rmGlobal` | partiel | **Σ (risqueJ + risqueJ_1)** |
| Feuille marge `rVal` / `rSusp` | `rSusp = 0` | RM et RS séparés par intermédiaire |
| Positions | sans RS | champs **`rVal`**, **`rSusp`**, **`risqueSuspens`** |
| **`alertes[]`** | absent | liste des positions DEFAUT_TITRES / DEFAUT_ESPECES |

---

## 2. Import BVMT et parsing

### Format accepté
- **Uniquement texte à largeur fixe BVMT** (`.txt`)
- **CSV / Excel rejetés** par `BvmtImportValidator` (extension + contenu détecté)

### `BvmtImportValidator` (nouveau)
- Validation des 3 fichiers avant lancement du batch
- Cohérence date transactions / séance attendue
- Avertissements : intermédiaire absent du fichier inter, valeur absente du fichier valeurs
- **Bug corrigé** : suppression du `InputStream.reset()` (échec sur `MultipartFile`)

### `FgmImportService`
- Appel du validateur **avant** l'orchestration batch
- Échec rapide avec message structuré si validation en erreur
- Avertissements enregistrés dans **`Seance.anomalies`**

### `TransactionReader`
- UTF-8
- **Dernière ligne incluse** (plus de boucle qui excluait la dernière transaction)
- Filtrage longueur minimale et date sur 8 chiffres

### `TransactionProcessor`
- **`@StepScope`** + paramètre **`seanceCompact`** : séance Mongo alignée avec l'import UI

### `ValeurReader`
- **`seanceCompact`** pour aligner `Valeur.seance` avec la séance importée
- UTF-8, lignes trop courtes ignorées

### `IntermediaireWriter`
- **`dateImport`** prise depuis le paramètre job `dateSeance` (plus `LocalDate.now()`)

### `IntermediaireJobListener`
- **`deleteByDateImport(date)`** au lieu de `deleteAll()` (évite de vider toute la collection)

### `ApportInitialEnsurerService` (nouveau)
- Crée un apport initial à 0 pour chaque intermédiaire actif dans le fichier transactions
- Appelé dans `GlobalBatchOrchestrationService` **avant** le batch mouvement bancaire

---

## 3. API REST et sécurité

### `FgmApiController`
- **`GET /api/seances/{date}/dashboard`** — snapshot complet (positions, feuille marge, stats, alertes)
- **`POST /api/import/session`** — validation 3 fichiers + date séance

### `SeanceController`
- Alias legacy : `/cloturer`, `/annuler`, `/anomalies`

### `SecurityConfig`
- Rôles : **`ADMIN_FGM`**, **`SUPERVISEUR`**, **`USER`**
- Import réservé admin / superviseur
- **WebSocket `/ws/**` retiré** de la config

### WebSocket supprimé
- `FgmWebSocketPublisher.java` — supprimé
- `WebSocketConfig.java` — supprimé
- Données dashboard uniquement via **REST**

---

## 4. Données de seed et paramétrage

### `FgmMongoDataSeed`
- Paramétrage par défaut : **6 %**, **J+2** (`del_reg_liv = 2`)
- Utilisateurs démo : `admin@fgm.local`, `superviseur@fgm.local`, `inter@fgm.local`
- Apports initiaux démo pour intermédiaires 101 / 102

### `SeanceService`
- Délai par défaut **2 jours** si paramétrage absent (évite crash au `preparer`)

---

## 5. Frontend Angular (`fgm-frontend`)

### `api.service.ts`
- Chemins alignés sur `FgmApiController`
- Fallbacks legacy conservés
- **`getSessionDashboard(date)`**
- **`getMyPositions`**
- Chemin **MvtBanqueInter** corrigé : `/api/mvt-banque/seance?date=`

### `dashboard.component.ts`
| Élément | Correction |
|---------|------------|
| WebSocket | **Supprimé** — rechargement REST uniquement |
| Import session | injection immédiate des résultats dans le dashboard |
| **`alertes[]`** | alimentées depuis `res.alertes` du snapshot |
| Contributions | colonnes **R_val** et **R_susp** depuis feuille d'appel |
| Fallback contributions | utilise `rVal`, `rSusp`, `risqueSuspens` des positions |
| **MvtBanqueInter** | nouvelle table dans l'onglet Contributions |
| **`loadMvtBanqueInter()`** | chargement après import et à l'ouverture de l'onglet |

### `intermediaire.component.ts`
- REST uniquement, **`getMyPositions`**

### Supprimé
- `websocket.service.ts`
- `environment.ts` : `wsUrl` retiré
- Rôles front : **`ADMIN_FGM`** (plus `ADMIN` seul)

---

## 6. Modèles Mongo étendus

| Modèle | Champ ajouté / utilisé |
|--------|------------------------|
| `Risque` | `risqueSuspens` |
| `MouvementBancaire` | `totalRsusp` (+ getter/setter) |
| `Seance` | `anomalies` (liste remplie à l'import) |

---

## 7. Tests et fichiers d'exemple

### Fichiers BVMT de test (`testdata/bvmt/`)
| Fichier | Contenu |
|---------|---------|
| `transactions_20260115.txt` | Vente 366 FINAC, ARTES (101) → BIAT (102) |
| `intermediaires_20260115.txt` | Intermédiaires 101 et 102 |
| `valeurs_20260115.txt` | FINAC, clôture 15,110 |
| `README.md` | Résultats attendus et mode d'emploi |

### Tests unitaires
| Fichier | Vérifie |
|---------|---------|
| `PositionNetteCalculatorTest.java` | R_val 628, RS 56, cas 1–4, exclusivité |
| `BvmtSampleFilesTest.java` | Présence fichiers, validation OK, rejet CSV |
| `BvmtFixedWidthBuilder.java` | Générateur lignes fixe pour tests |

### Scripts
| Script | Rôle |
|--------|------|
| `scripts/validate-bvmt-sample.ps1` | Validation PNT/PNE/R_val/RS ligne par ligne sans backend |
| `scripts/test-import-bvmt.ps1` | Import via API REST (backend + MongoDB requis) |

---

## 8. Ce qui n'a PAS été fait (volontairement ou en attente)

| Sujet | Statut |
|-------|--------|
| **Keycloak** | Non implémenté — JWT maison (`POST /api/auth/login`) |
| **CSV / Excel** | Non supporté — rejet explicite, format BVMT fixe uniquement |
| **Fichiers BVMT réels utilisateur** | Placer vos fichiers dans `testdata/bvmt/` ou importer via le dashboard |
| **Compilation Maven locale** | Nécessite **Java 21+** et Lombok actif (`mvn compile`) |

---

## 9. Commandes utiles

```powershell
# Tests calculs + validation fichiers exemple
cd backend
mvn test "-Dtest=PositionNetteCalculatorTest,BvmtSampleFilesTest"

# Validation ligne par ligne (sans backend)
.\scripts\validate-bvmt-sample.ps1

# Import complet (backend sur :8081 + MongoDB)
.\scripts\test-import-bvmt.ps1

# Frontend
cd fgm-frontend
npm install
npm start
```

**Connexion démo :** voir **section 11** ci-dessous.

---

## 10. Fichiers clés modifiés (index rapide)

```
backend/
  com/fgm/gestion/service/
    PositionNetteCalculator.java      ← formules PNT/PNE/R_val/RS
    BvmtImportValidator.java          ← validation pré-import
    ApportInitialEnsurerService.java  ← apports auto
    GlobalBatchOrchestrationService.java
  org/example/backend/
    positionnettebatch/PositionnetteTasklet.java
    risquebatch/RisqueTasklet.java
    mouvementbancairebatch/MouvementBancaireTasklet.java
    transactionbatch/TransactionReader.java, TransactionProcessor.java
    valeurbatch/ValeurReader.java
    intermediairebatch/IntermediaireWriter.java, IntermediaireJobListener.java
    service/FgmDashboardService.java, FgmImportService.java
    controller/FgmApiController.java, SeanceController.java
    model/Risque.java, MouvementBancaire.java
    config/SecurityConfig.java

fgm-frontend/
  src/app/services/api.service.ts
  src/app/components/dashboard/dashboard.component.ts
  src/app/components/intermediaire/intermediaire.component.ts
  src/environments/environment.ts

testdata/bvmt/          ← fichiers exemple ARTES/FINAC
scripts/                  ← scripts validation et import
```

---

## 11. Comptes et adresses (connexion)

### URLs

| Service | Adresse |
|---------|---------|
| **Frontend** (Angular) | http://localhost:4200 |
| **Backend API** | http://localhost:8081 |
| **Login** | `POST http://localhost:8081/api/auth/login` |

Sur l’écran de connexion, le champ **Adresse** accepte l’**email** ou le **nom d’utilisateur**.

### Comptes démo (seed Mongo au démarrage)

| Rôle | Email | Utilisateur | Mot de passe | Code interm. |
|------|-------|-------------|--------------|--------------|
| Administrateur FGM | `admin@fgm.local` | `admin` | `Admin123!` | — |
| Superviseur | `superviseur@fgm.local` | `superviseur` | `Super123!` | — |
| Intermédiaire | `inter@fgm.local` | `inter101` | `Inter123!` | **101** |

### Droits

| Compte | Import BVMT | Dashboard admin |
|--------|-------------|-----------------|
| admin | Oui | Oui |
| superviseur | Oui | Oui |
| inter101 | Non | Vue intermédiaire 101 |

### Si la connexion échoue

- Backend lancé + MongoDB actif
- `fgm.seed.enabled: true` dans `backend/src/main/resources/application.yml`
- Mots de passe **sensibles à la casse**

---

*Dernière mise à jour : mai 2026 — projet FGM-fixed (backend Java 21, Angular 17, MongoDB).*
