import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoanApplicationComponent } from '../loan-application/loan-application.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, LoanApplicationComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  currentUser: any;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.currentUser = this.authService.currentUserValue;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
