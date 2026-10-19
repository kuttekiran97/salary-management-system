import { ApiError, api, basicAuthorization, currentUser, onSignedOut, signIn, signOut, toQueryString } from './client'

describe('toQueryString', () => {
  it('leaves out empty values', () => {
    expect(toQueryString({ search: '', country: 'IN', department: undefined, page: 0 })).toBe('?country=IN&page=0')
  })

  it('returns an empty string when nothing is set', () => {
    expect(toQueryString({ country: undefined })).toBe('')
  })
})

describe('api', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('turns a problem response into an ApiError with field errors', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ status: 409, detail: 'Another employee already has this email', errors: { email: 'Another employee already has this email' } }),
      { status: 409 },
    )))

    const error = await api.deleteEmployee(1).catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 409, fieldErrors: { email: 'Another employee already has this email' } })
  })

  it('still reports an error when the body is not JSON', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('Bad gateway', { status: 502 })))

    await expect(api.overview()).rejects.toMatchObject({ status: 502, message: 'Request failed (502)' })
  })

  it('sends JSON when saving', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ id: 1 }), { status: 201 }))
    vi.stubGlobal('fetch', fetchMock)

    await api.createEmployee({
      employeeCode: 'EMP-00001', fullName: 'Priya Sharma', email: 'priya@acme.com', jobTitle: 'Software Engineer',
      department: 'Engineering', countryCode: 'IN', annualSalary: 1800000, joiningDate: '2022-04-01',
    })

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/employees')
    expect(init.method).toBe('POST')
    expect(init.headers).toEqual({ 'Content-Type': 'application/json' })
  })
})

describe('signing in', () => {
  const ok = (body: object) => new Response(JSON.stringify(body), { status: 200 })

  afterEach(() => {
    signOut()
    vi.unstubAllGlobals()
  })

  it('encodes credentials as UTF-8 HTTP Basic', () => {
    expect(basicAuthorization('hr', 'secret')).toBe('Basic aHI6c2VjcmV0')
    expect(basicAuthorization('hr', 'pässword')).toBe('Basic aHI6cMOkc3N3b3Jk')
  })

  it('checks the credentials, then sends them with every request', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(ok({ username: 'hr' }))
      .mockResolvedValueOnce(ok({ headcount: 1 }))
    vi.stubGlobal('fetch', fetchMock)

    expect(await signIn('hr', 'secret')).toBe('hr')
    await api.overview()

    expect(currentUser()).toBe('hr')
    expect(fetchMock.mock.calls[0][0]).toBe('/api/auth/me')
    expect(fetchMock.mock.calls[1][1].headers).toEqual({ Authorization: 'Basic aHI6c2VjcmV0' })
  })

  it('keeps nobody signed in when the password is wrong', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('{}', { status: 401 })))

    await expect(signIn('hr', 'wrong')).rejects.toMatchObject({ status: 401 })
    expect(currentUser()).toBeUndefined()
  })

  it('signs out and notifies the app when the API rejects the stored credentials', async () => {
    const signedOut = vi.fn()
    onSignedOut(signedOut)
    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce(ok({ username: 'hr' }))
      .mockResolvedValueOnce(new Response('{}', { status: 401 })))

    await signIn('hr', 'secret')
    await expect(api.overview()).rejects.toMatchObject({ status: 401 })

    expect(currentUser()).toBeUndefined()
    expect(signedOut).toHaveBeenCalledOnce()
  })
})
