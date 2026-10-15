import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { api } from '../api/client'
import { referenceData } from '../testData'
import InsightsPage from './InsightsPage'

vi.mock('../api/client', async (importOriginal) => {
  const original = await importOriginal<typeof import('../api/client')>()
  return {
    ...original,
    api: { overview: vi.fn(), byCountry: vi.fn(), byJobTitle: vi.fn(), referenceData: vi.fn() },
  }
})

const stats = (median: number) => ({ min: median / 2, max: median * 2, average: median, median, total: median * 10 })

describe('InsightsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(api.referenceData).mockResolvedValue(referenceData)
    vi.mocked(api.overview).mockResolvedValue({ headcount: 10000, salaryUsd: { ...stats(67000), average: 74122 } })
    vi.mocked(api.byCountry).mockResolvedValue([
      { countryCode: 'IN', countryName: 'India', currencyCode: 'INR', headcount: 3510, salary: stats(2567000), salaryUsd: stats(30804) },
      { countryCode: 'US', countryName: 'United States', currencyCode: 'USD', headcount: 2471, salary: stats(102000), salaryUsd: stats(102000) },
    ])
    vi.mocked(api.byJobTitle).mockResolvedValue({
      countryCode: null,
      currencyCode: 'USD',
      jobTitles: [{ jobTitle: 'Accountant', department: 'Finance', headcount: 300, salary: stats(55100) }],
    })
  })

  it('shows the headline numbers', async () => {
    render(<InsightsPage />)

    expect(await screen.findByText('10,000')).toBeInTheDocument()
    expect(screen.getByText('$67,000')).toBeInTheDocument()
    expect(screen.getByText('$74,122')).toBeInTheDocument()
  })

  it('shows countries in a table with local currency and USD, highest median first', async () => {
    render(<InsightsPage />)
    const section = screen.getByRole('region', { name: 'Median salary by country' })

    await userEvent.click(within(section).getByRole('button', { name: 'Table' }))

    const rows = await within(section).findAllByRole('row')
    expect(rows[1]).toHaveTextContent('United States')
    expect(rows[2]).toHaveTextContent('India')
    expect(rows[2]).toHaveTextContent('₹25,67,000')
    expect(rows[2]).toHaveTextContent('$30,804')
  })

  it('reloads job-title figures in local currency when a country is chosen', async () => {
    render(<InsightsPage />)
    const section = screen.getByRole('region', { name: 'Median salary by job title' })

    await userEvent.click(within(section).getByRole('combobox', { name: 'Country' }))
    await userEvent.click(await screen.findByRole('option', { name: 'India' }))

    expect(api.byJobTitle).toHaveBeenLastCalledWith('IN')
  })
})
