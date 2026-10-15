import { useMemo } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { CssBaseline, ThemeProvider, useMediaQuery } from '@mui/material'
import App from './App'
import { buildTheme } from './theme'

/** App-wide providers. The theme follows the operating system's light or dark setting. */
export default function Root() {
  const prefersDark = useMediaQuery('(prefers-color-scheme: dark)')
  const theme = useMemo(() => buildTheme(prefersDark ? 'dark' : 'light'), [prefersDark])

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ThemeProvider>
  )
}
