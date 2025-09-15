import { Injectable, inject, signal } from "@angular/core";
import { Product } from "./product.model";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {catchError, map, Observable, of, tap, throwError} from "rxjs";
import {AuthService} from "./auth.service";


export interface ApiResponse<T> {
  data: T;
  message?: string;
  error?: string;
}

@Injectable({
    providedIn: "root"
}) export class ProductsService {

    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    private readonly apiUrl = '/api/products';
    private readonly baseUrl = 'http://localhost:8080';

    private readonly _products = signal<Product[]>([]);
    private readonly _loading = signal<boolean>(false);
    private readonly _error = signal<string | null>(null);

    public readonly products = this._products.asReadonly();
    public readonly loading = this._loading.asReadonly();
    public readonly error = this._error.asReadonly();

    //private readonly path = "http://localhost:8080/api/products";

    constructor() {
      this.loadProducts();
    }

    private getAuthHeaders(): HttpHeaders {
      const token = this.authService.getToken();
      return new HttpHeaders({
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      });
    }

    public get(): Observable<Product[]> {
      this._loading.set(true);
      this._error.set(null);

      return this.http.get<ApiResponse<Product[]>>(`${this.baseUrl}${this.apiUrl}`, {
        headers: this.getAuthHeaders()
        }).pipe(
          map(response => response.data || []),
          tap((products) => {
            this._products.set(products);
            this._loading.set(false);
          }),
          catchError((error) => {
            console.error('Error fetching products:', error);
            this._error.set(error.error?.message || 'Failed to load products');
            this._loading.set(false);
            // Fallback to local JSON
            return this.http.get<Product[]>('assets/products.json');
          })
      );
    }


    public create(product: Omit<Product, 'id' | 'createdAt' | 'updatedAt'>): Observable<Product> {
      return this.http.post<Product>(`${this.baseUrl}${this.apiUrl}`, product, {
        headers: this.getAuthHeaders()
      }).pipe(
        tap((createdProduct) => {
          this._products.update(products => [createdProduct, ...products]);
        }),
        catchError((error) => {
          console.error('Error creating product:', error);
          return throwError(() => error);
        })
      );
    }

    public update(product: Product): Observable<Product> {
      return this.http.put<Product>(`${this.baseUrl}${this.apiUrl}/${product.id}`, product, {
        headers: this.getAuthHeaders()
      }).pipe(
        tap((updatedProduct) => {
          this._products.update(products =>
            products.map(p => p.id === updatedProduct.id ? updatedProduct : p)
          );
        }),
        catchError((error) => {
          console.error('Error updating product:', error);
          throw error;
        })
      );
    }

    public delete(productId: number): Observable<boolean> {
      return this.http.delete<boolean>(`${this.baseUrl}${this.apiUrl}/${productId}`, {
        headers: this.getAuthHeaders()
      }).pipe(
            catchError((error) => {
              console.error('Error deleting product:', error);
              throw error;
            }),
            tap(() => this._products.update(products => products.filter(product => product.id !== productId))),
        );
    }

    private loadProducts(): void {
      this.get().subscribe({
        error: (error) => console.error('Failed to load initial products:', error)
      });
    }
}
