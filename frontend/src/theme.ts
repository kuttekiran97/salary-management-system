import { createTheme, type Theme } from '@mui/material/styles'

// Single-series chart colour, validated for >= 3:1 contrast on each mode's surface
const CHART_BAR = { light: '#2a78d6', dark: '#3987e5' }

export function buildTheme(mode: 'light' | 'dark'): Theme {
  return createTheme({
    palette: {
      mode,
      primary: { main: mode === 'light' ? '#1c5cab' : '#6da7ec' },
      background: mode === 'light'
        ? { default: '#f6f6f4', paper: '#fcfcfb' }
        : { default: '#121211', paper: '#1a1a19' },
    },
    shape: { borderRadius: 8 },
    typography: { fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif' },
  })
}

export function chartBarColor(theme: Theme): string {
  return CHART_BAR[theme.palette.mode]
}
