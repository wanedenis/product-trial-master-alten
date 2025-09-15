import {
  Component,
  computed,
  EventEmitter,
  input,
  Output,
  ViewEncapsulation,
} from "@angular/core";
import {FormsModule, NgForm} from "@angular/forms";
import { Product } from "app/products/data-access/product.model";
import { SelectItem } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ProductsService } from '../../data-access/products.service';
import {AuthService} from "../../data-access/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: "app-product-form",
  template: `
    <form #form="ngForm" (ngSubmit)="onSave(form)">
      <div class="form-field">
        <label for="name">Nom</label>
        <input pInputText
          type="text"
          id="name"
          name="name"
          [(ngModel)]="editedProduct().name"
          required>
      </div>
      <div class="form-field">
        <label for="code">Code</label>
        <input
          pInputText
          type="text"
          id="code"
          name="code"
          [(ngModel)]="editedProduct().code"
          required>

      </div>
      <div class="form-field">
        <label for="price">Prix</label>
        <p-inputNumber
          [(ngModel)]="editedProduct().price"
          name="price"
          mode="decimal"
          required/>
      </div>
      <div class="form-field">
        <label for="quantity">Quantité</label>
        <p-inputNumber
          [(ngModel)]="editedProduct().quantity"
          name="quantity"
          mode="decimal"
          [min]="0"
          required
        />
      </div>
      <div class="form-field">
        <label for="description">Description</label>
        <textarea pInputTextarea
          id="description"
          name="description"
          rows="5"
          cols="30"
          [(ngModel)]="editedProduct().description">
        </textarea>
      </div>
      <div class="form-field">
        <label for="description">Catégorie</label>
        <p-dropdown
          [options]="categories"
          [(ngModel)]="editedProduct().category"
          name="category"
          appendTo="body"
        />
      </div>
      <div class="flex justify-content-between">
        <p-button type="button" (click)="onCancel()" label="Annuler" severity="help"/>
        <p-button type="submit" [disabled]="!form.valid" label="Enregistrer" severity="success"/>
      </div>
    </form>
  `,
  styleUrls: ["./product-form.component.scss"],
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    InputTextareaModule,
    DropdownModule,
  ],
  encapsulation: ViewEncapsulation.None
})
export class ProductFormComponent {

  protected readonly NgForm = NgForm;
  public readonly product = input.required<Product>();

  @Output() cancel = new EventEmitter<void>();
  @Output() save = new EventEmitter<Product>();

  public readonly editedProduct = computed(() => ({ ...this.product() }));
  errorMessage: string | null = null;

  public readonly categories: SelectItem[] = [
    { value: "Accessories", label: "Accessories" },
    { value: "Fitness", label: "Fitness" },
    { value: "Clothing", label: "Clothing" },
    { value: "Electronics", label: "Electronics" },
  ];

  constructor(
    public productService: ProductsService,
    public authService: AuthService,
    private router: Router
  ) {}

  onCancel() {
    this.cancel.emit();
  }

  onSaves() {
    this.save.emit(this.editedProduct());
    this.productService.create(this.editedProduct())
  }

  onSave(form: NgForm): void {
    if (form.valid && this.authService.isAdmin()) {

      const product = this.editedProduct();

      this.productService.create(product).subscribe({
        next: (response) => {
          this.save.emit(response);
          this.errorMessage = null;
          this.router.navigate(['/products']);
        },
        error: (err) => {
          this.errorMessage = err.status === 403
            ? 'Access denied: Admin privileges required'
            : 'Failed to create product';
          console.error('Create product error:', err);
        }
      });
    } else if (!this.authService.isAdmin()) {
      this.errorMessage = 'Access denied: Admin privileges required';
    }
  }
}
