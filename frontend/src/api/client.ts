import type {
  CountryInsight,
  Employee,
  EmployeeInput,
  EmployeeQuery,
  JobTitleInsights,
  Overview,
  Page,
  ReferenceData,
} from './types'

// Empty in development (Vite proxies /api); set VITE_API_URL when the API is hosted elsewhere
const BASE_URL = import.meta.env.VITE_API_URL ?? ''

/** An error response from the API, with per-field messages when the backend provides them. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

// Credentials live in sessionStorage: they survive a page refresh and are gone when the tab closes
const SESSION_KEY = 'salary-management.session'

interface Session {
  username: string
  /** The value of the Authorization header */
  authorization: string
}

function readSession(): Session | undefined {
  try {
    const stored = sessionStorage.getItem(SESSION_KEY)
    return stored ? (JSON.parse(stored) as Session) : undefined
  } catch {
    return undefined
  }
}

function writeSession(session: Session | undefined) {
  try {
    if (session) {
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(session))
    } else {
      sessionStorage.removeItem(SESSION_KEY)
    }
  } catch {
    // Storage can be unavailable (e.g. blocked site data); the user then signs in again after a refresh
  }
}

let session = readSession()
let onSessionEnded: () => void = () => {}

/** The signed-in username, or undefined when nobody is signed in. */
export function currentUser(): string | undefined {
  return session?.username
}

/** Called when the API rejects the stored credentials, so the app can show the sign-in page. */
export function onSignedOut(handler: () => void) {
  onSessionEnded = handler
}

/** HTTP Basic header value; UTF-8 encoded so non-ASCII passwords work. */
export function basicAuthorization(username: string, password: string): string {
  const bytes = new TextEncoder().encode(`${username}:${password}`)
  return `Basic ${btoa(String.fromCharCode(...bytes))}`
}

async function request<T>(path: string, init?: RequestInit, authorization = session?.authorization): Promise<T> {
  const headers: Record<string, string> = {}
  if (init?.body) {
    headers['Content-Type'] = 'application/json'
  }
  if (authorization) {
    headers.Authorization = authorization
  }
  const response = await fetch(BASE_URL + path, { ...init, headers })
  if (!response.ok) {
    // Errors are RFC 9457 problem details: { detail, errors?: { field: message } }
    const problem = await response.json().catch(() => ({}))
    const error = new ApiError(response.status, problem.detail ?? `Request failed (${response.status})`, problem.errors ?? {})
    if (response.status === 401 && authorization === session?.authorization && session) {
      signOut()
      onSessionEnded()
    }
    throw error
  }
  return response.status === 204 ? (undefined as T) : response.json()
}

/** Checks the credentials with the API and, if they are valid, keeps them for later requests. */
export async function signIn(username: string, password: string): Promise<string> {
  const authorization = basicAuthorization(username, password)
  const user = await request<{ username: string }>('/api/auth/me', undefined, authorization)
  session = { username: user.username, authorization }
  writeSession(session)
  return user.username
}

export function signOut() {
  session = undefined
  writeSession(undefined)
}

/** Builds a query string, leaving out empty values. */
export function toQueryString(params: object): string {
  const query = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      query.set(key, String(value))
    }
  }
  const text = query.toString()
  return text ? `?${text}` : ''
}

export const api = {
  listEmployees: (query: EmployeeQuery) => request<Page<Employee>>(`/api/employees${toQueryString(query)}`),

  createEmployee: (input: EmployeeInput) =>
    request<Employee>('/api/employees', { method: 'POST', body: JSON.stringify(input) }),

  updateEmployee: (id: number, input: EmployeeInput) =>
    request<Employee>(`/api/employees/${id}`, { method: 'PUT', body: JSON.stringify(input) }),

  deleteEmployee: (id: number) => request<void>(`/api/employees/${id}`, { method: 'DELETE' }),

  referenceData: () => request<ReferenceData>('/api/reference-data'),

  overview: () => request<Overview>('/api/insights/overview'),

  byCountry: () => request<CountryInsight[]>('/api/insights/by-country'),

  byJobTitle: (country?: string) =>
    request<JobTitleInsights>(`/api/insights/by-job-title${toQueryString({ country })}`),
}
