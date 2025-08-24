import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoanProcessResponse {
  processInstanceKey: number;
  bpmnProcessId: string;
  version: number;
  variables: any;
}

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private apiUrl = environment.apiURL + '/api';

  constructor(private http: HttpClient) {}

  startLoanProcess(creditScore?: number): Observable<LoanProcessResponse> {
    const options = creditScore 
      ? { params: { creditScore: creditScore.toString() } }
      : {};
    return this.http.post<LoanProcessResponse>(`${this.apiUrl}/loans/start`, null, options);
  }
}
