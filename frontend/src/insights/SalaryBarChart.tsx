import { useTheme } from '@mui/material'
import { BarChart } from '@mui/x-charts/BarChart'
import { formatCompactMoney, formatMoney } from '../format'
import { chartBarColor } from '../theme'

export interface BarRow {
  label: string
  value: number
}

interface Props {
  rows: BarRow[]
  currency: string
  /** Names the measure in the tooltip, e.g. "Median salary" */
  measure: string
}

const BAR_HEIGHT = 34

/**
 * One measure across categories as horizontal bars: long category names stay readable,
 * values sit at the bar ends, and a tooltip gives the exact amount.
 */
export default function SalaryBarChart({ rows, currency, measure }: Props) {
  const theme = useTheme()

  return (
    <BarChart
      dataset={rows as unknown as Record<string, string | number>[]}
      layout="horizontal"
      height={rows.length * BAR_HEIGHT + 48}
      margin={{ right: 56 }}
      yAxis={[{ scaleType: 'band', dataKey: 'label', width: 180, disableTicks: true }]}
      xAxis={[{ valueFormatter: (value: number) => formatCompactMoney(value, currency), tickNumber: 5 }]}
      series={[{
        dataKey: 'value',
        label: measure,
        color: chartBarColor(theme),
        valueFormatter: (value) => (value == null ? '' : formatMoney(value, currency)),
        barLabel: (item) => (item.value == null ? '' : formatCompactMoney(item.value, currency)),
        barLabelPlacement: 'outside',
      }]}
      borderRadius={4}
      grid={{ vertical: true }}
      hideLegend
    />
  )
}
