import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { api } from '../api/client'
import { priya, referenceData } from '../testData'
import EmployeesPage from './EmployeesPage'

vi.mock('../api/client', async (importOriginal) => {
  const original = await importOriginal<typeof import('../api/client')>()
  return { ...original, api: { listEmployees: vi.fn(), referenceData: vi.fn(), deleteEmployee: vi.fn() } }
})

describe('EmployeesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(api.referenceData).mockResolvedValue(referenceData)
    vi.mocked(api.listEmployees).mockResolvedValue({ items: [priya], page: 0, size: 25, totalItems: 10000, totalPages: 400 })
  })

  it('shows a page of employees with salaries in local currency and USD', async () => {
    render(<EmployeesPage />)

    expect(await screen.findByText('Priya Sharma')).toBeInTheDocument()
    expect(screen.getByText('10,000 employees')).toBeInTheDocument()
    expect(screen.getByText('₹18,00,000')).toBeInTheDocument()
    expect(screen.getByText('$21,600')).toBeInTheDocument()
  })

  it('searches once typing pauses, starting from the first page', async () => {
    render(<EmployeesPage />)
    await screen.findByText('Priya Sharma')

    await userEvent.type(screen.getByRole('textbox', { name: 'Search' }), 'sharma')

    await waitFor(() => expect(api.listEmployees).toHaveBeenLastCalledWith(
      expect.objectContaining({ search: 'sharma', page: 0 }),
    ))
    // One request for the initial load and one for the finished search, not one per keystroke
    expect(api.listEmployees).toHaveBeenCalledTimes(2)
  })

  it('asks for confirmation before deleting', async () => {
    vi.mocked(api.deleteEmployee).mockResolvedValue(undefined)
    render(<EmployeesPage />)

    await userEvent.click(await screen.findByRole('button', { name: 'Delete Priya Sharma' }))
    expect(api.deleteEmployee).not.toHaveBeenCalled()

    await userEvent.click(screen.getByRole('button', { name: 'Remove' }))

    expect(api.deleteEmployee).toHaveBeenCalledWith(1)
    expect(await screen.findByText('Priya Sharma was removed')).toBeInTheDocument()
  })

  it('shows an error with a retry option when loading fails', async () => {
    vi.mocked(api.listEmployees).mockRejectedValue(new Error('Network down'))
    render(<EmployeesPage />)

    expect(await screen.findByText(/Could not load employees: Network down/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument()
  })
})
