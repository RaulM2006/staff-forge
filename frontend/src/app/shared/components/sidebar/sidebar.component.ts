import { Component, computed, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Role } from '../../../core/models/user.model';
import { NavItem, NAVIGATION_ITEMS } from '../../models/navigation.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatTooltipModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  /** Whether the sidebar is in collapsed state (narrow icons-only on desktop, hidden on mobile). */
  collapsed = input(false);

  /** Current user's role — used to filter which navigation items are visible. */
  userRole = input<Role | null>(null);

  /** Whether the viewport is in the mobile breakpoint. */
  isMobile = input(false);

  /** Emitted when the user clicks a navigation item (used to auto-close sidebar on mobile). */
  navItemClicked = output<void>();

  /** Primary nav items filtered by the current user role. */
  primaryItems = computed(() =>
    this.filterByRole(NAVIGATION_ITEMS.filter(item => item.group === 'primary'))
  );

  /** Secondary nav items (bottom section) filtered by the current user role. */
  secondaryItems = computed(() =>
    this.filterByRole(NAVIGATION_ITEMS.filter(item => item.group === 'secondary'))
  );

  onNavItemClick(): void {
    this.navItemClicked.emit();
  }

  private filterByRole(items: NavItem[]): NavItem[] {
    const role = this.userRole();
    if (!role) return items; // Dev fallback: show all items if no role is set
    return items.filter(item => item.roles.includes(role));
  }
}
