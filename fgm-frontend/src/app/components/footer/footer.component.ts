import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
<footer id="footer" class="fgm-footer">
  <div class="fgm-footer__dots"></div>

  <!-- CTA strip -->
  <div class="fgm-footer__cta">
    <div class="fgm-footer__cta-inner">
      <div>
        <h2>Découvrez la BVMT</h2>
        <p>Visitez le site officiel de la Bourse des Valeurs Mobilières de Tunis ou suivez l'actualité du projet sur LinkedIn.</p>
      </div>
      <div class="fgm-footer__cta-actions">
        <a class="fgm-footer__btn-gold" href="https://www.bvmt.com.tn/" target="_blank" rel="noopener">
          🌐 Site officiel BVMT ↗
        </a>
        <a class="fgm-footer__btn-outline" href="https://www.linkedin.com/company/bourse-des-valeurs-mobilieres-de-tunis/" target="_blank" rel="noopener">
          🔗 Suivre sur LinkedIn ↗
        </a>
      </div>
    </div>
  </div>

  <!-- Footer body -->
  <div class="fgm-footer__body">
    <!-- Brand -->
    <div>
      <div class="fgm-footer__brand">
        <div class="fgm-footer__logo-box">
          <img src="assets/bvmt-logo.png" alt="BVMT" style="height:100%;width:100%;object-fit:contain" />
        </div>
        <div>
          <p class="fgm-footer__brand-name">FGM · BVMT</p>
          <p class="fgm-footer__brand-sub">Fonds de Garantie de Marché</p>
        </div>
      </div>
      <p class="fgm-footer__tagline">Conçu avec rigueur pour la stabilité du marché tunisien.</p>
    </div>

    <!-- Marché tunisien -->
    <div>
      <h3 class="fgm-footer__col-title">Marché tunisien</h3>
      <ul class="fgm-footer__col-list">
        <li class="fgm-footer__col-item">
          <span class="fgm-footer__col-icon">📍</span>
          <span>Tunis, Tunisie — Place financière de la Méditerranée</span>
        </li>
        <li class="fgm-footer__col-item">
          <span class="fgm-footer__col-icon">🕐</span>
          <span>Séances : 09h00 – 14h10 (heure de Tunis)</span>
        </li>
        <li class="fgm-footer__col-item">
          <span class="fgm-footer__col-icon">🌐</span>
          <span>Régulé par le Conseil du Marché Financier (CMF)</span>
        </li>
      </ul>
    </div>

    <!-- Contact -->
    <div>
      <h3 class="fgm-footer__col-title">Contact</h3>
      <ul class="fgm-footer__col-list">
        <li class="fgm-footer__col-item">
          <span class="fgm-footer__col-icon">✉️</span>
          <span>BVMT — Bourse des Valeurs Mobilières de Tunis</span>
        </li>
      </ul>
    </div>
  </div>

  <!-- Bottom bar -->
  <div class="fgm-footer__bar">
    <div class="fgm-footer__bar-inner">
      <p>© {{ year }} FGM Platform · BVMT · Tous droits réservés.</p>
      <div class="fgm-footer__version">
        <span class="fgm-footer__version-dot"></span>
        v2.0 · {{ year }}
      </div>
    </div>
  </div>
</footer>
  `,
})
export class FooterComponent {
  year = new Date().getFullYear();
}
