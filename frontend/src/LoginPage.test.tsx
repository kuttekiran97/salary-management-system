import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ApiError, signIn } from './api/client'
import LoginPage from './LoginPage'

vi.mock('./api/client', async (importOriginal) => {
  const actual = await importOriginal<typeof import('./api/client')>()
  return { ...actual, signIn: vi.fn() }
})

describe('LoginPage', () => {
  it('signs in with the entered credentials', async () => {
    vi.mocked(signIn).mockResolvedValue('hr')
    const onSignedIn = vi.fn()
    render(<LoginPage onSignedIn={onSignedIn} />)

    await userEvent.type(screen.getByLabelText(/Username/), 'hr')
    await userEvent.type(screen.getByLabelText(/Password/), 'secret')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(signIn).toHaveBeenCalledWith('hr', 'secret')
    expect(onSignedIn).toHaveBeenCalledWith('hr')
  })

  it('says so when the username or password is wrong', async () => {
    vi.mocked(signIn).mockRejectedValue(new ApiError(401, 'Sign in with the HR Manager account'))
    const onSignedIn = vi.fn()
    render(<LoginPage onSignedIn={onSignedIn} />)

    await userEvent.type(screen.getByLabelText(/Username/), 'hr')
    await userEvent.type(screen.getByLabelText(/Password/), 'wrong')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByText('Wrong username or password')).toBeInTheDocument()
    expect(onSignedIn).not.toHaveBeenCalled()
  })
})
