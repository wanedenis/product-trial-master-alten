import { Injectable, inject, signal } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Observable, BehaviorSubject, of, tap, catchError, map, switchMap } from "rxjs";
import { AuthService } from "./auth.service";
import {Product} from "./product.model";

export interface CartItem {
  id?: number;
  product: Product;
  quantity: number;
  totalPrice: number;
  createdAt?: number;
  updatedAt?: number;
}

export interface CartResponse {
  data: CartItem[];
  totalItems: number;
  totalAmount: number;
}

@Injectable({
  providedIn: "root"
})
export class CartService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private readonly apiUrl = '/api/cart';
  private readonly baseUrl = 'http://localhost:8080';

  private readonly _cartItems = signal<CartItem[]>([]);
  private readonly _cartTotalItems = signal<number>(0);
  private readonly _cartTotalAmount = signal<number>(0);
  private readonly _loading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);

  public readonly cartItems = this._cartItems.asReadonly();
  public readonly cartTotalItems = this._cartTotalItems.asReadonly();
  public readonly cartTotalAmount = this._cartTotalAmount.asReadonly();
  public readonly loading = this._loading.asReadonly();
  public readonly error = this._error.asReadonly();

  constructor() {
    // Load cart on initialization if user is authenticated
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.loadCart();
      } else {
        this.clearCart();
      }
    });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  public loadCart(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this._loading.set(true);
    this._error.set(null);

    this.http.get<CartResponse>(`${this.baseUrl}${this.apiUrl}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      map(response => ({
        items: response.data || [],
        totalItems: response.totalItems || 0,
        totalAmount: response.totalAmount || 0
      })),
      tap(({ items, totalItems, totalAmount }) => {
        this._cartItems.set(items);
        this._cartTotalItems.set(totalItems);
        this._cartTotalAmount.set(totalAmount);
        this._loading.set(false);
      }),
      catchError((error) => {
        console.error('Error loading cart:', error);
        this._error.set(error.error?.message || 'Failed to load cart');
        this._loading.set(false);
        return of({ items: [], totalItems: 0, totalAmount: 0 });
      })
    ).subscribe();
  }

  public addToCart(productId: number, quantity: number = 1): Observable<CartItem> {
    if (!this.authService.isAuthenticated()) {
      throw new Error('User must be authenticated to add items to cart');
    }

    this._loading.set(true);
    this._error.set(null);

    return this.http.post<CartItem>(`${this.baseUrl}${this.apiUrl}/add/${productId}?quantity=${quantity}`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap((cartItem) => {
        // Update local state
        this._cartItems.update(items => {
          const existingIndex = items.findIndex(item => item.product.id === cartItem.product.id);
          if (existingIndex >= 0) {
            // Update existing item
            const updatedItems = [...items];
            updatedItems[existingIndex] = {
              ...updatedItems[existingIndex],
              quantity: updatedItems[existingIndex].quantity + quantity,
              totalPrice: updatedItems[existingIndex].totalPrice + cartItem.totalPrice
            };
            return updatedItems;
          } else {
            // Add new item
            return [...items, cartItem];
          }
        });

        this.updateTotals();
        this._loading.set(false);
      }),
      catchError((error) => {
        console.error('Error adding to cart:', error);
        this._error.set(error.error?.message || 'Failed to add item to cart');
        this._loading.set(false);
        throw error;
      })
    );
  }

  public updateCartItem(cartId: number, quantity: number): Observable<CartItem | null> {
    if (!this.authService.isAuthenticated()) {
      throw new Error('User must be authenticated to update cart');
    }

    this._loading.set(true);
    this._error.set(null);

    return this.http.put<CartItem>(`${this.baseUrl}${this.apiUrl}/${cartId}?quantity=${quantity}`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap((updatedItem) => {
        if (updatedItem) {
          this._cartItems.update(items =>
            items.map(item => item.id === updatedItem.id ? updatedItem : item)
          );
          this.updateTotals();
        }
        this._loading.set(false);
      }),
      catchError((error) => {
        console.error('Error updating cart item:', error);
        this._error.set(error.error?.message || 'Failed to update cart item');
        this._loading.set(false);
        throw error;
      })
    );
  }

  public removeFromCart(cartId: number): Observable<boolean> {
    if (!this.authService.isAuthenticated()) {
      throw new Error('User must be authenticated to remove items from cart');
    }

    this._loading.set(true);
    this._error.set(null);

    return this.http.delete<boolean>(`${this.baseUrl}${this.apiUrl}/${cartId}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(() => {
        this._cartItems.update(items => items.filter(item => item.id !== cartId));
        this.updateTotals();
        this._loading.set(false);
      }),
      catchError((error) => {
        console.error('Error removing from cart:', error);
        this._error.set(error.error?.message || 'Failed to remove item from cart');
        this._loading.set(false);
        throw error;
      })
    );
  }

  public clearCart(): Observable<boolean> {
    if (!this.authService.isAuthenticated()) {
      this.clearLocalCart();
      return of(true);
    }

    this._loading.set(true);
    this._error.set(null);

    return this.http.delete<boolean>(`${this.baseUrl}${this.apiUrl}/clear`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(() => {
        this.clearLocalCart();
        this._loading.set(false);
      }),
      catchError((error) => {
        console.error('Error clearing cart:', error);
        this._error.set(error.error?.message || 'Failed to clear cart');
        this._loading.set(false);
        // Clear local state even if API fails
        this.clearLocalCart();
        return of(true);
      })
    );
  }

  public getCartItemCount(): number {
    return this._cartTotalItems();
  }

  private updateTotals(): void {
    const items = this._cartItems();
    const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
    const totalAmount = items.reduce((sum, item) => sum + item.totalPrice, 0);

    this._cartTotalItems.set(totalItems);
    this._cartTotalAmount.set(totalAmount);
  }

  private clearLocalCart(): void {
    this._cartItems.set([]);
    this._cartTotalItems.set(0);
    this._cartTotalAmount.set(0);
  }

  // Local cart operations (for demo purposes when not authenticated)
  public addToLocalCart(product: Product, quantity: number = 1): void {
    this._cartItems.update(items => {
      const existingIndex = items.findIndex(item => item.product.id === product.id);
      if (existingIndex >= 0) {
        const updatedItems = [...items];
        const currentQuantity = updatedItems[existingIndex].quantity;
        updatedItems[existingIndex] = {
          ...updatedItems[existingIndex],
          quantity: currentQuantity + quantity,
          totalPrice: product.price * (currentQuantity + quantity)
        };
        return updatedItems;
      } else {
        return [...items, {
          product,
          quantity,
          totalPrice: product.price * quantity
        }];
      }
    });
    this.updateTotals();
  }

  public updateLocalCartItem(productId: number, quantity: number): void {
    this._cartItems.update(items => {
      const itemIndex = items.findIndex(item => item.product.id === productId);
      if (itemIndex >= 0 && quantity > 0) {
        const updatedItems = [...items];
        updatedItems[itemIndex] = {
          ...updatedItems[itemIndex],
          quantity,
          totalPrice: updatedItems[itemIndex].product.price * quantity
        };
        return updatedItems;
      } else if (itemIndex >= 0) {
        // Remove item if quantity is 0 or negative
        return items.filter(item => item.product.id !== productId);
      }
      return items;
    });
    this.updateTotals();
  }

  public removeFromLocalCart(productId: number): void {
    this._cartItems.update(items => items.filter(item => item.product.id !== productId));
    this.updateTotals();
  }
}
