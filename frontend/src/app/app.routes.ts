import { Routes } from '@angular/router';
import { LayoutShellComponent } from './shared/components/layout-shell/layout-shell.component';
import { PlaceholderComponent } from './features/placeholder/placeholder.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutShellComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: PlaceholderComponent, data: { title: 'Dashboard' } },
      { path: 'employees', component: PlaceholderComponent, data: { title: 'Employees' } },
      { path: 'projects', component: PlaceholderComponent, data: { title: 'Projects' } },
      { path: 'allocations', component: PlaceholderComponent, data: { title: 'Allocations' } },
      { path: 'skills', component: PlaceholderComponent, data: { title: 'Skills Matrix' } },
      { path: 'settings', component: PlaceholderComponent, data: { title: 'Settings' } },
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
