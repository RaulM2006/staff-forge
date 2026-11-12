import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';

/**
 * Generic placeholder component used for routes whose feature pages
 * have not been implemented yet. Reads the page title from route data.
 */
@Component({
  selector: 'app-placeholder',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="flex flex-col items-center justify-center py-24 text-center">
      <mat-icon class="!text-6xl !w-16 !h-16 mb-4"
                style="color: var(--sf-color-text-subtle)">
        construction
      </mat-icon>
      <h2 class="text-xl font-semibold mb-2"
          style="color: var(--sf-color-text-main)">
        {{ title() }}
      </h2>
      <p style="color: var(--sf-color-text-muted)">
        This page is under construction and will be available soon.
      </p>
    </div>
  `
})
export class PlaceholderComponent {
  private readonly route = inject(ActivatedRoute);

  title = toSignal(
    this.route.data.pipe(map(data => data['title'] ?? 'Coming Soon')),
    { initialValue: 'Coming Soon' }
  );
}
