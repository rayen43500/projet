# Fichiers BVMT de test — exemple ARTES / FINAC

Ces 3 fichiers simulent une séance du **15/01/2026** avec l'exemple du document explicatif FGM :

| Intermédiaire | Rôle | Valeur | Position |
|---------------|------|--------|----------|
| **101** ARTES LE FINANCIER | Vendeur | FINAC | PNT− / PNE+ (366 titres, 5 585,5 TND) |
| **102** BIAT | Acheteur | FINAC | Contrepartie |

## Résultats attendus (paramétrage 6 %, J+2)

| Indicateur | Formule | Valeur |
|------------|---------|--------|
| **PNT** | titres vendus − achetés | −366 → signe `−` |
| **PNE** | versé − reçu | +5 585,5 TND → signe `+` |
| **R_val (RV J)** | 15,110 × 366 × (1,06)² − 5 585,5 | **628 DT** |
| **RS (suspens)** | \|15,110 × 366 − 5 585,5\| | **56 DT** |

## Import

1. Démarrer le backend (`mvn spring-boot:run`) et MongoDB
2. Se connecter en admin (`admin@fgm.local` / `Admin123!`)
3. **Séances → Calculer & Importer** : charger les 3 fichiers `.txt`
4. Date séance : `2026-01-15`

Ou via script :

```powershell
.\scripts\test-import-bvmt.ps1
```

## Format

| **CSV point-virgule** | `CODE;LIBELLESCOURT;...` et `DATE;...;LIBELLE;...;CLOTURE` (fichiers `.txt`) |

## Tests automatisés

```powershell
cd backend
mvn test -Dtest=BvmtSampleFilesTest,PositionNetteCalculatorTest
```
