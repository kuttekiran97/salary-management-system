import { useState, type FormEvent } from 'react'
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from '@mui/material'
import { ApiError, signIn } from './api/client'

interface Props {
  onSignedIn: (username: string) => void
}

/** Sign-in for the single HR Manager account. */
export default function LoginPage({ onSignedIn }: Props) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string>()
  const [busy, setBusy] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError(undefined)
    try {
      onSignedIn(await signIn(username.trim(), password))
    } catch (e) {
      setError(e instanceof ApiError && e.status === 401 ? 'Wrong username or password' : `Could not sign in: ${(e as Error).message}`)
      setBusy(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', bgcolor: 'background.default', px: 2 }}>
      <Paper component="form" onSubmit={submit} variant="outlined" sx={{ p: 4, width: '100%', maxWidth: 380 }}>
        <Stack spacing={2.5}>
          <Box>
            <Typography variant="h5" component="h1" sx={{ fontWeight: 600 }}>Salary Management</Typography>
            <Typography color="text.secondary">Sign in as the HR Manager</Typography>
          </Box>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField
            label="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            autoFocus
            required
          />
          <TextField
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
          <Button type="submit" variant="contained" size="large" disabled={busy}>
            {busy ? 'Signing in…' : 'Sign in'}
          </Button>
        </Stack>
      </Paper>
    </Box>
  )
}
