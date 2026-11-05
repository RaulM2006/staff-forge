/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      colors: {
        sf: {
          primary: {
            50: '#EFF6FF',
            100: '#DBEAFE',
            200: '#BFDBFE',
            500: '#3B82F6',
            600: '#2563EB',
            700: '#1D4ED8',
            800: '#1E40AF',
            900: '#1E3A8A',
            DEFAULT: 'var(--sf-color-primary, #1E40AF)'
          },
          accent: {
            50: '#F0FDFA',
            100: '#CCFBF1',
            500: '#14B8A6',
            600: '#0D9488',
            700: '#0F766E',
            DEFAULT: 'var(--sf-color-accent, #0D9488)'
          },
          surface: {
            DEFAULT: 'var(--sf-color-surface, #FFFFFF)',
            muted: 'var(--sf-color-surface-muted, #F8FAFC)',
            card: 'var(--sf-color-surface-card, #FFFFFF)'
          },
          border: {
            DEFAULT: 'var(--sf-color-border, #E2E8F0)',
            strong: 'var(--sf-color-border-strong, #CBD5E1)'
          },
          text: {
            main: 'var(--sf-color-text-main, #0F172A)',
            muted: 'var(--sf-color-text-muted, #64748B)',
            inverse: 'var(--sf-color-text-inverse, #FFFFFF)'
          },
          status: {
            success: 'var(--sf-color-success, #16A34A)',
            warning: 'var(--sf-color-warning, #D97706)',
            danger: 'var(--sf-color-danger, #DC2626)',
            info: 'var(--sf-color-info, #2563EB)'
          }
        }
      },
      fontFamily: {
        sans: ['Inter', 'Roboto', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Helvetica', 'Arial', 'sans-serif'],
        display: ['Roboto', 'Inter', 'sans-serif']
      },
      spacing: {
        'header': 'var(--sf-header-height, 64px)',
        'sidebar': 'var(--sf-sidebar-width, 260px)',
        'sidebar-collapsed': 'var(--sf-sidebar-collapsed-width, 72px)'
      },
      boxShadow: {
        'sf-card': '0 1px 3px 0 rgba(0, 0, 0, 0.07), 0 1px 2px -1px rgba(0, 0, 0, 0.05)',
        'sf-elevated': '0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -4px rgba(0, 0, 0, 0.04)'
      }
    },
  },
  plugins: [],
}
