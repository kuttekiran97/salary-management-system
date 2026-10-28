import { ApiError, api, toQueryString } from './client'

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
