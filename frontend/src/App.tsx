import { lazy, Suspense } from 'react'
import { AppBar, Box, Container, LinearProgress, Tab, Tabs, Toolbar, Typography } from '@mui/material'
import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'

// Each page loads its own code, so the data grid and chart libraries download only when needed
const EmployeesPage = lazy(() => import('./employees/EmployeesPage'))
const InsightsPage = lazy(() => import('./insights/InsightsPage'))

const PAGES = [
  { path: '/employees', label: 'Employees' },
  { path: '/insights', label: 'Insights' },
]

function App() {
  const { pathname } = useLocation()
  const currentTab = PAGES.find((page) => pathname.startsWith(page.path))?.path ?? false

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" color="default" elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar sx={{ columnGap: 3, flexWrap: 'wrap' }}>
          <Typography variant="h6" component="h1" sx={{ fontWeight: 600 }}>
            Salary Management
          </Typography>
          <Tabs value={currentTab} aria-label="Pages">
            {PAGES.map((page) => (
              <Tab key={page.path} label={page.label} value={page.path} component={Link} to={page.path} />
            ))}
          </Tabs>
        </Toolbar>
      </AppBar>
      <Container maxWidth="xl" sx={{ py: 3 }}>
        <Suspense fallback={<LinearProgress aria-label="Loading page" />}>
          <Routes>
            <Route path="/" element={<Navigate to="/employees" replace />} />
            <Route path="/employees" element={<EmployeesPage />} />
            <Route path="/insights" element={<InsightsPage />} />
            <Route path="*" element={<Navigate to="/employees" replace />} />
          </Routes>
        </Suspense>
      </Container>
    </Box>
  )
}

export default App
