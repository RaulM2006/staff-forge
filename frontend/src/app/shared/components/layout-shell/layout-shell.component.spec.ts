import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { LayoutShellComponent } from './layout-shell.component';

describe('LayoutShellComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LayoutShellComponent],
      providers: [
        provideRouter([]),
        provideAnimationsAsync()
      ]
    }).compileComponents();
  });

  it('should create layout shell', () => {
    const fixture = TestBed.createComponent(LayoutShellComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should toggle sidebar state', () => {
    const fixture = TestBed.createComponent(LayoutShellComponent);
    const component = fixture.componentInstance;
    
    const initial = component.isSidebarCollapsed();
    component.toggleSidebar();
    expect(component.isSidebarCollapsed()).toBe(!initial);
    
    component.toggleSidebar();
    expect(component.isSidebarCollapsed()).toBe(initial);
  });
});
