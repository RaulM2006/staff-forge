import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BreakpointObserver } from '@angular/cdk/layout';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/user.model';

@Component({
  selector: 'app-layout-shell',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent],
  templateUrl: './layout-shell.component.html',
  styleUrl: './layout-shell.component.scss'
})
export class LayoutShellComponent {
  private readonly authService = inject(AuthService);
  private readonly breakpointObserver = inject(BreakpointObserver);

  readonly currentUser = this.authService.currentUser;

  isSidebarCollapsed = signal(false);
  isMobile = signal(false);

  constructor() {
    this.breakpointObserver
      .observe(['(max-width: 1023px)'])
      .pipe(takeUntilDestroyed())
      .subscribe(result => {
        this.isMobile.set(result.matches);
        this.isSidebarCollapsed.set(result.matches);
      });
  }

  toggleSidebar(): void {
    this.isSidebarCollapsed.update(collapsed => !collapsed);
  }

  closeSidebarOnMobile(): void {
    if (this.isMobile()) {
      this.isSidebarCollapsed.set(true);
    }
  }

  onRoleChange(role: Role): void {
    this.authService.setMockRole(role);
  }

  onLogout(): void {
    this.authService.logout();
  }
}
