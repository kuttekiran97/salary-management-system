import { Paper, Typography } from '@mui/material'

interface Props {
  label: string
  value: string
  note?: string
}

/** One headline number. */
export default function StatTile({ label, value, note }: Props) {
  return (
    <Paper variant="outlined" sx={{ p: 2 }}>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
      <Typography variant="h4" component="p" sx={{ fontWeight: 600, my: 0.5 }}>{value}</Typography>
      {note && <Typography variant="caption" color="text.secondary">{note}</Typography>}
    </Paper>
  )
}
