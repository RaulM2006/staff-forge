/**
 * Mirror of the backend Role enum (com.staffforge.user.Role).
 * Keep in sync with the Java enum values.
 */
export enum Role {
  ADMIN = 'ADMIN',
  PROJECT_MANAGER = 'PROJECT_MANAGER',
  RESOURCE_MANAGER = 'RESOURCE_MANAGER',
  HR = 'HR',
  EMPLOYEE = 'EMPLOYEE'
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}
