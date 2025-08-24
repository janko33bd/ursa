import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LoanService } from '../../services/loan.service';

@Component({
  selector: 'app-loan-application',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './loan-application.component.html',
  styleUrl: './loan-application.component.css'
})
export class LoanApplicationComponent {
  loanForm: FormGroup;
  loading = false;
  processResult: any = null;
  error = '';

  constructor(
    private formBuilder: FormBuilder,
    private loanService: LoanService
  ) {
    this.loanForm = this.formBuilder.group({
      creditScore: ['', [Validators.required, Validators.min(300), Validators.max(850)]]
    });
  }

  onSubmit() {
    if (this.loanForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.processResult = null;
    
    const creditScore = this.loanForm.get('creditScore')?.value;
    
    this.loanService.startLoanProcess(creditScore).subscribe({
      next: (result) => {
        this.processResult = result;
        this.loading = false;
        this.loanForm.reset();
      },
      error: (error) => {
        this.error = 'Failed to start loan process. Please try again.';
        this.loading = false;
      }
    });
  }

  getProcessStatus(): string {
    if (!this.processResult) return '';
    
    const creditScore = this.processResult.variables?.creditScore;
    if (creditScore >= 700) {
      return 'Your application will be processed automatically (Auto-approval path)';
    } else {
      return 'Your application requires manual review';
    }
  }
}
