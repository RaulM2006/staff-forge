import { Routes } from '@angular/router';
import { LayoutShellComponent } from './shared/components/layout-shell/layout-shell.component';
import { PlaceholderComponent } from './features/placeholder/placeholder.component';
import { LoginComponent } from './features/auth/login/login.component';
import { SkillCatalogComponent } from './features/skills/skill-catalog/skill-catalog.component';
import { ProfileComponent } from './features/profile/profile.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Role } from './core/models/user.model';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    data: { title: 'Login' }
  },
  {
    path: 'register',
    component: PlaceholderComponent,
    data: { title: 'Register' }
  },



  {
    path: '',
    component: LayoutShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        component: PlaceholderComponent,
        canActivate: [roleGuard],
        data: {
          title: 'Dashboard',
          roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER, Role.HR, Role.EMPLOYEE]
        }
      },
      {
        path: 'profile',
        component: ProfileComponent,
        canActivate: [roleGuard],
        data: {
          title: 'My Profile',
          roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER, Role.HR, Role.EMPLOYEE]
        }
      },
      {
        path: 'employees',
        component: PlaceholderComponent,
        canActivate: [roleGuard],
        data: {
          title: 'Employees',
          roles: [Role.ADMIN, Role.HR, Role.RESOURCE_MANAGER]
        }
      },
      {
        path: 'projects',
        component: PlaceholderComponent,
        canActivate: [roleGuard],
        data: {
          title: 'Projects',
          roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER]
        }
      },
      {
        path: 'allocations',
        component: PlaceholderComponent,
        canActivate: [roleGuard],
        data: {
          title: 'Allocations',
          roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER]
        }
      },
      {
        path: 'skills',
        component: PlaceholderComponent,
        canActivate: [roleGuard],
        data: {
          title: 'Skills Matrix',
          roles: [Role.ADMIN, Role.HR, Role.RESOURCE_MANAGER, Role.PROJECT_MANAGER]
        }
      },
      {
        path: 'admin/skills',
        component: SkillCatalogComponent,
        canActivate: [roleGuard],
        data: {
          title: 'Skill Catalog',
          roles: [Role.ADMIN]
        }
      },
      {
        path: 'settings',
        component: PlaceholderComponent,
        canActivate: [roleGuard],
        data: {
          title: 'Settings',
          roles: [Role.EMPLOYEE]
        }
      }
    ]
  },

  { path: '**', redirectTo: 'dashboard' }
];
