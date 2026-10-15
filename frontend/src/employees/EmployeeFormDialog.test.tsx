import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ApiError, api } from '../api/client'
import { priya, referenceData } from '../testData'
import EmployeeFormDialog from './EmployeeFormDialog'

vi.mock('../api/client', async (importOriginal) => {
  const original = await importOriginal<typeof import('../api/client')>()
  return { ...original, api: { createEmployee: vi.fn(), updateEmployee: vi.fn() } }
})

function renderDialog(props: Partial<React.ComponentProps<typeof EmployeeFormDialog>> = {}) {
  const onSaved = vi.fn()
  render(<EmployeeFormDialog referenceData={referenceData} onClose={vi.fn()} onSaved={onSaved} {...props} />)
  return { onSaved }
}

async function choose(label: string, option: string) {
  await userEvent.click(screen.getByRole('combobox', { name: label }))
  await userEvent.click(within(screen.getByRole('listbox')).getByRole('option', { name: option }))
}

describe('EmployeeFormDialog', () => {
  beforeEach(() => vi.clearAllMocks())

  it('shows required-field errors and does not call the API when the form is empty', async () => {
    renderDialog()

    await userEvent.click(screen.getByRole('button', { name: 'Add employee' }))

    expect(screen.getAllByText('Required')).toHaveLength(8)
    expect(api.createEmployee).not.toHaveBeenCalled()
  })

  it('only offers job titles from the chosen department', async () => {
    renderDialog()
    expect(screen.getByRole('combobox', { name: 'Job title' })).toHaveAttribute('aria-disabled', 'true')

    await choose('Department', 'Finance')
    await userEvent.click(screen.getByRole('combobox', { name: 'Job title' }))

    const options = within(screen.getByRole('listbox')).getAllByRole('option').map((o) => o.textContent)
    expect(options).toEqual(['Accountant'])
  })

  it('shows the local currency once a country is chosen', async () => {
    renderDialog()

    await choose('Country', 'India (INR)')

    expect(screen.getByText('INR')).toBeInTheDocument()
  })

  it('saves changes to an existing employee', async () => {
    vi.mocked(api.updateEmployee).mockResolvedValue({ ...priya, annualSalary: 2400000 })
    const { onSaved } = renderDialog({ employee: priya })

    const salary = screen.getByRole('textbox', { name: 'Annual salary' })
    await userEvent.clear(salary)
    await userEvent.type(salary, '2400000')
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(api.updateEmployee).toHaveBeenCalledWith(1, expect.objectContaining({ annualSalary: 2400000 }))
    expect(onSaved).toHaveBeenCalledWith(expect.objectContaining({ annualSalary: 2400000 }))
  })

  it('shows an error from the API next to the field it belongs to', async () => {
    vi.mocked(api.updateEmployee).mockRejectedValue(
      new ApiError(409, 'Another employee already has this email', { email: 'Another employee already has this email' }),
    )
    const { onSaved } = renderDialog({ employee: priya })

    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByText('Another employee already has this email')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: 'Email' })).toHaveAttribute('aria-invalid', 'true')
    expect(onSaved).not.toHaveBeenCalled()
  })
})
