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

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {}
  if (init?.body) {
    headers['Content-Type'] = 'application/json'
  }
  const response = await fetch(BASE_URL + path, { ...init, headers })
  if (!response.ok) {
    // Errors are RFC 9457 problem details: { detail, errors?: { field: message } }
    const problem = await response.json().catch(() => ({}))
    throw new ApiError(response.status, problem.detail ?? `Request failed (${response.status})`, problem.errors ?? {})
  }
  return response.status === 204 ? (undefined as T) : response.json()
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
