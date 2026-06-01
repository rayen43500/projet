import { Component } from '@angular/core';
import { TopbarComponent } from '../topbar/topbar.component';
import { TickerComponent } from '../ticker/ticker.component';
import { HeroComponent } from '../hero/hero.component';
import { CarouselComponent } from '../carousel/carousel.component';
import { AboutComponent } from '../about/about.component';
import { MarketOverviewComponent } from '../market/market-overview.component';
import { FooterComponent } from '../footer/footer.component';
import { TranslationService } from '../../services/translation.service';

@Component({
  selector: 'app-welcome-page',
  standalone: true,
  imports: [
    TopbarComponent, TickerComponent, HeroComponent,
    CarouselComponent, AboutComponent, MarketOverviewComponent, FooterComponent
  ],
  template: `
<div style="min-height:100vh;background:hsl(var(--background))">
  <app-topbar
    [isWelcome]="true"
    [lang]="ts.lang"
    (connectClick)="scrollToLogin()"
    (langChange)="ts.setLang($any($event))"
  />
  <app-ticker />
  <main>
    <app-hero />
    <app-carousel />
    <app-about />
    <app-market-overview />
  </main>
  <app-footer />
</div>
  `,
})
export class WelcomePageComponent {
  constructor(public ts: TranslationService) {}

  scrollToLogin(): void {
    document.getElementById('hero')?.scrollIntoView({ behavior: 'smooth' });
  }
}
