import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule],
  template: `<div style="text-align:center;padding:80px 24px">
    <div style="font-size:48px">🔒</div>
    <h2 style="color:#1c2c1c">Accès non autorisé</h2>
    <p style="color:#6a7a6a">Vous n'avez pas les droits nécessaires.</p>
    <button (click)="go()" style="padding:10px 24px;background:#3d6b52;color:#fff;border:none;border-radius:8px;cursor:pointer">Retour</button>
  </div>`
})
export class UnauthorizedComponent {
  constructor(private router: Router) {}
  go() { this.router.navigate(['/dashboard']); }
}
