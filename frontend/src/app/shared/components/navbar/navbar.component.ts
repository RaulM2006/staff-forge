import { Component, computed, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { Role, User } from '../../../core/models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatMenuModule, MatDividerModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  /** The currently authenticated user. */
  user = input<User | null>(null);

  /** Emitted when the hamburger menu is clicked. */
  toggleSidebar = output<void>();

  /** Emitted when the user clicks "Logout" in the user menu. */
  logout = output<void>();

  /** Emitted when a role is selected from the quick dev role switcher. */
  changeRole = output<Role>();

  readonly availableRoles = [
    Role.ADMIN,
    Role.EMPLOYEE,
    Role.PROJECT_MANAGER,
    Role.RESOURCE_MANAGER,
    Role.HR
  ];

  userName = computed(() => {
    const u = this.user();
    return u ? `${u.firstName} ${u.lastName}` : 'Guest';
  });

  userInitials = computed(() => {
    const u = this.user();
    return u ? `${u.firstName[0]}${u.lastName[0]}` : '?';
  });

  /** Formats the role enum value for display: PROJECT_MANAGER → Project Manager */
  userRoleDisplay = computed(() => {
    const u = this.user();
    if (!u) return 'Not authenticated';
    return this.formatRole(u.role);
  });

  formatRole(role: Role): string {
    return role
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  }
}
