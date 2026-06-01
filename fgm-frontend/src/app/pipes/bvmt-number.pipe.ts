import { Pipe, PipeTransform } from '@angular/core';

/**
 * Format numérique compatible BVMT :
 * - séparateur de milliers : espace normal
 * - séparateur décimal : virgule
 * - nombre de décimales contrôlé
 *
 * Note: Intl.NumberFormat('fr-FR') utilise souvent des espaces insécables (NBSP).
 * On les normalise en espace simple pour que le copier/coller donne exactement "5 585,500".
 */
@Pipe({
  name: 'bvmtNumber',
  standalone: true,
})
export class BvmtNumberPipe implements PipeTransform {
  transform(value: unknown, decimals: number = 0): string {
    const n =
      typeof value === 'number'
        ? value
        : typeof value === 'string'
          ? Number(value.replace(',', '.'))
          : NaN;

    if (!Number.isFinite(n)) return '0';

    const d = Math.max(0, Math.min(6, Math.trunc(decimals)));
    const fmt = new Intl.NumberFormat('fr-FR', {
      useGrouping: true,
      minimumFractionDigits: d,
      maximumFractionDigits: d,
    });

    // Normalise NBSP / NNBSP en espace.
    return fmt.format(n).replace(/[\u00A0\u202F]/g, ' ');
  }
}

