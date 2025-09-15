import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap, catchError, map } from 'rxjs';
import { Router } from '@angular/router';

export interface User {
  id: number;
  username: string;
  firstname: string;
  email: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  firstname: string;
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  username: string;
  firstname: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = '/api/auth';
  private readonly baseUrl = 'http://localhost:8080';

  private readonly _currentUser = new BehaviorSubject<User | null>(null);
  private readonly _isAuthenticated = new BehaviorSubject<boolean>(false);
  private readonly _loading = new BehaviorSubject<boolean>(false);
  private readonly _error = new BehaviorSubject<string | null>(null);

  public readonly currentUser$ = this._currentUser.asObservable();
  public readonly isAuthenticated$ = this._isAuthenticated.asObservable();
  public readonly loading$ = this._loading.asObservable();
  public readonly error$ = this._error.asObservable();

  private tokenKey = 'auth_token';
  private userKey = 'current_user';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Check for existing token on initialization
    this.initializeAuth();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const userData = localStorage.getItem(this.userKey);

    if (token && userData) {
      const user: User = JSON.parse(userData);
      this._currentUser.next(user);
      this._isAuthenticated.next(true);

      // Validate token by making a request to a protected endpoint
      this.validateToken().subscribe({
        next: () => {
          // Token is valid
        },
        error: () => {
          // Token is invalid, clear auth
          this.logout();
        }
      });
    }
  }

  public login(credentials: LoginRequest): Observable<JwtResponse> {
    this._loading.next(true);
    this._error.next(null);

    return this.http.post<JwtResponse>(`${this.baseUrl}${this.apiUrl}/token`, credentials).pipe(
      tap((response) => {
        this.setSession(response);
        this._loading.next(false);
      }),
      catchError((error) => {
        console.error('Login error:', error);
        this._error.next(error.error?.message || 'Login failed');
        this._loading.next(false);
        throw error;
      })
    );
  }

  public register(userData: RegisterRequest): Observable<User> {
    this._loading.next(true);
    this._error.next(null);

    return this.http.post<User>(`${this.baseUrl}${this.apiUrl}/account`, userData).pipe(
      tap((user) => {
        // Auto-login after successful registration
        const loginRequest: LoginRequest = {
          email: userData.email,
          password: userData.password
        };
        this.login(loginRequest).subscribe();
        this._loading.next(false);
      }),
      catchError((error) => {
        console.error('Registration error:', error);
        this._error.next(error.error?.message || 'Registration failed');
        this._loading.next(false);
        throw error;
      })
    );
  }

  public logout(): void {
    this._currentUser.next(null);
    this._isAuthenticated.next(false);
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.router.navigate(['/login']);
  }

  public getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  public isAuthenticated(): boolean {
    return this._isAuthenticated.value;
  }

  public getCurrentUser(): User | null {
    return this._currentUser.value;
  }

  public isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user ? user.email === 'admin@admin.com' : false;
  }

  private setSession(authResult: JwtResponse): void {
    const user: User = {
      id: authResult.id,
      username: authResult.username,
      firstname: authResult.firstname,
      email: authResult.email
    };

    localStorage.setItem(this.tokenKey, authResult.token);
    localStorage.setItem(this.userKey, JSON.stringify(user));

    this._currentUser.next(user);
    this._isAuthenticated.next(true);
  }

  private validateToken(): Observable<boolean> {
    const token = this.getToken();
    if (!token) {
      return of(false);
    }

    // Make a request to a protected endpoint to validate token
    return this.http.get<boolean>(`${this.baseUrl}/api/products`, {
      headers: this.getAuthHeaders()
    }).pipe(
      map(() => true),
      catchError(() => {
        this.logout();
        return of(false);
      })
    );
  }

  // Check if token is expired (basic check)
  public isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000;
      return Date.now() >= exp;
    } catch {
      return true;
    }
  }
}
