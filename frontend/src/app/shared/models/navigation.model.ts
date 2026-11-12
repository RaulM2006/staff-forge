import { Role } from '../../core/models/user.model';

export interface NavItem {
  /** Display label shown next to the icon. */
  label: string;
  /** Material icon name. */
  icon: string;
  /** Router path (absolute). */
  route: string;
  /** Roles that are allowed to see this item. */
  roles: Role[];
  /** Grouping: primary items appear at the top, secondary at the bottom. */
  group: 'primary' | 'secondary';
}

/**
 * Central navigation configuration.
 * Add new pages here — the sidebar and any future nav component
 * will automatically pick them up and filter by user role.
 */
export const NAVIGATION_ITEMS: NavItem[] = [
  // ── Primary ──────────────────────────────────────────
  {
    label: 'Dashboard',
    icon: 'dashboard',
    route: '/dashboard',
    roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER, Role.HR, Role.EMPLOYEE],
    group: 'primary'
  },
  {
    label: 'Employees',
    icon: 'people',
    route: '/employees',
    roles: [Role.ADMIN, Role.HR, Role.RESOURCE_MANAGER],
    group: 'primary'
  },
  {
    label: 'Projects',
    icon: 'folder_open',
    route: '/projects',
    roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER],
    group: 'primary'
  },
  {
    label: 'Allocations',
    icon: 'event_note',
    route: '/allocations',
    roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER],
    group: 'primary'
  },
  {
    label: 'Skills Matrix',
    icon: 'psychology',
    route: '/skills',
    roles: [Role.ADMIN, Role.HR, Role.RESOURCE_MANAGER, Role.PROJECT_MANAGER],
    group: 'primary'
  },
  {
    label: 'Skill Catalog',
    icon: 'category',
    route: '/admin/skills',
    roles: [Role.ADMIN],
    group: 'primary'
  },

  // ── Secondary ────────────────────────────────────────
  {
    label: 'Profile',
    icon: 'person',
    route: '/profile',
    roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER, Role.HR, Role.EMPLOYEE],
    group: 'secondary'
  },
  {
    label: 'Settings',
    icon: 'settings',
    route: '/settings',
    roles: [Role.ADMIN],
    group: 'secondary'
  }
];
