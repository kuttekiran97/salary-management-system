import { useState, type ReactNode } from 'react'
import {
  Alert,
  Box,
  MenuItem,
  Paper,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material'
import { api } from '../api/client'
import type { SalaryStats } from '../api/types'
import { formatCount, formatMoney } from '../format'
import { useApi } from '../useApi'
import SalaryBarChart from './SalaryBarChart'
import StatTile from './StatTile'

type View = 'chart' | 'table'

export default function InsightsPage() {
  const [jobCountry, setJobCountry] = useState('')
  const overview = useApi(api.overview, [])
  const byCountry = useApi(api.byCountry, [])
  const byJobTitle = useApi(() => api.byJobTitle(jobCountry), [jobCountry])
  const referenceData = useApi(api.referenceData, [])

  const error = overview.error ?? byCountry.error ?? byJobTitle.error
  const usd = overview.data?.salaryUsd
  const countries = [...(byCountry.data ?? [])].sort((a, b) => b.salaryUsd.median - a.salaryUsd.median)
  const jobTitles = [...(byJobTitle.data?.jobTitles ?? [])].sort((a, b) => b.salary.median - a.salary.median)
  const jobCurrency = byJobTitle.data?.currencyCode ?? 'USD'

  return (
    <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: 'minmax(0, 1fr)' }}>
      <Box>
        <Typography variant="h5" component="h2">Insights</Typography>
        <Typography color="text.secondary">
          How ACME pays people. Cross-country figures are converted to USD at fixed rates.
        </Typography>
      </Box>

      {error && <Alert severity="error">Could not load insights: {error.message}</Alert>}

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(4, 1fr)' } }}>
        <StatTile label="Employees" value={overview.data ? formatCount(overview.data.headcount) : '…'} />
        <StatTile label="Annual payroll" value={usd ? formatMoney(usd.total, 'USD') : '…'} note="All countries, in USD" />
        <StatTile label="Median salary" value={usd ? formatMoney(usd.median, 'USD') : '…'} note="Half earn more, half less" />
        <StatTile label="Average salary" value={usd ? formatMoney(usd.average, 'USD') : '…'} />
      </Box>

      <Section
        title="Median salary by country"
        subtitle="In USD for comparison. The table also shows local currency."
        loading={byCountry.loading}
        chart={(
          <SalaryBarChart
            rows={countries.map((c) => ({ label: c.countryName, value: c.salaryUsd.median }))}
            currency="USD"
            measure="Median salary"
          />
        )}
        table={(
          <StatsTable
            firstColumn="Country"
            extraColumns={['Median (USD)', 'Payroll (USD)']}
            rows={countries.map((c) => ({
              key: c.countryCode,
              name: c.countryName,
              headcount: c.headcount,
              currency: c.currencyCode,
              stats: c.salary,
              extra: [formatMoney(c.salaryUsd.median, 'USD'), formatMoney(c.salaryUsd.total, 'USD')],
            }))}
          />
        )}
      />

      <Section
        title="Median salary by job title"
        subtitle={jobCountry ? `In ${jobCurrency}, the local currency` : 'All countries, in USD'}
        loading={byJobTitle.loading}
        filter={(
          <TextField
            label="Country"
            select
            size="small"
            value={jobCountry}
            onChange={(e) => setJobCountry(e.target.value)}
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="">All countries (USD)</MenuItem>
            {referenceData.data?.countries.map((c) => <MenuItem key={c.code} value={c.code}>{c.name}</MenuItem>)}
          </TextField>
        )}
        chart={(
          <SalaryBarChart
            rows={jobTitles.map((t) => ({ label: t.jobTitle, value: t.salary.median }))}
            currency={jobCurrency}
            measure="Median salary"
          />
        )}
        table={(
          <StatsTable
            firstColumn="Job title"
            rows={jobTitles.map((t) => ({
              key: t.jobTitle,
              name: t.jobTitle,
              detail: t.department,
              headcount: t.headcount,
              currency: jobCurrency,
              stats: t.salary,
            }))}
          />
        )}
      />
    </Box>
  )
}

interface SectionProps {
  title: string
  subtitle: string
  loading: boolean
  filter?: ReactNode
  chart: ReactNode
  table: ReactNode
}

/** A titled card that shows its data as a chart or, for exact figures and screen readers, a table. */
function Section({ title, subtitle, loading, filter, chart, table }: SectionProps) {
  const [view, setView] = useState<View>('chart')

  return (
    <Paper variant="outlined" sx={{ p: 2 }} component="section" aria-label={title}>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Box>
          <Typography variant="h6" component="h3">{title}</Typography>
          <Typography variant="body2" color="text.secondary">{subtitle}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          {filter}
          <ToggleButtonGroup
            size="small"
            exclusive
            value={view}
            onChange={(_, next: View | null) => next && setView(next)}
            aria-label={`${title} view`}
          >
            <ToggleButton value="chart">Chart</ToggleButton>
            <ToggleButton value="table">Table</ToggleButton>
          </ToggleButtonGroup>
        </Box>
      </Box>
      {loading ? <Skeleton variant="rounded" height={260} /> : view === 'chart' ? chart : table}
    </Paper>
  )
}

interface StatsRow {
  key: string
  name: string
  detail?: string
  headcount: number
  currency: string
  stats: SalaryStats
  extra?: string[]
}

function StatsTable({ firstColumn, extraColumns = [], rows }: { firstColumn: string; extraColumns?: string[]; rows: StatsRow[] }) {
  const numeric = ['Employees', 'Min', 'Median', 'Average', 'Max', ...extraColumns]
  return (
    <TableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>{firstColumn}</TableCell>
            {numeric.map((column) => <TableCell key={column} align="right">{column}</TableCell>)}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.key}>
              <TableCell>
                {row.name}
                {row.detail && <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{row.detail}</Typography>}
              </TableCell>
              <TableCell align="right">{formatCount(row.headcount)}</TableCell>
              <TableCell align="right">{formatMoney(row.stats.min, row.currency)}</TableCell>
              <TableCell align="right">{formatMoney(row.stats.median, row.currency)}</TableCell>
              <TableCell align="right">{formatMoney(row.stats.average, row.currency)}</TableCell>
              <TableCell align="right">{formatMoney(row.stats.max, row.currency)}</TableCell>
              {row.extra?.map((value, i) => <TableCell key={i} align="right">{value}</TableCell>)}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
