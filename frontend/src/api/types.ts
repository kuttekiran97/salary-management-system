// Types mirror the backend's JSON contract (see README "API").

export interface Employee {
  id: number
  employeeCode: string
  fullName: string
  email: string
  jobTitle: string
  department: string
  countryCode: string
  countryName: string
  currencyCode: string
  /** Whole units of the local currency */
  annualSalary: number
  annualSalaryUsd: number
  /** yyyy-MM-dd */
  joiningDate: string
}

export interface EmployeeInput {
  employeeCode: string
  fullName: string
  email: string
  jobTitle: string
  department: string
  countryCode: string
  annualSalary: number
  joiningDate: string
}

export interface Page<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}

export type EmployeeSort = 'NAME' | 'EMPLOYEE_CODE' | 'JOINING_DATE' | 'SALARY'

export interface EmployeeQuery {
  search?: string
  country?: string
  department?: string
  jobTitle?: string
  sort: EmployeeSort
  descending: boolean
  page: number
  size: number
}

export interface CountryOption {
  code: string
  name: string
  currencyCode: string
  usdRate: number
}

export interface DepartmentOption {
  name: string
  jobTitles: string[]
}

export interface ReferenceData {
  countries: CountryOption[]
  departments: DepartmentOption[]
}

export interface SalaryStats {
  min: number
  max: number
  average: number
  median: number
  total: number
}

export interface Overview {
  headcount: number
  salaryUsd: SalaryStats
}

export interface CountryInsight {
  countryCode: string
  countryName: string
  currencyCode: string
  headcount: number
  salary: SalaryStats
  salaryUsd: SalaryStats
}

export interface JobTitleInsights {
  countryCode: string | null
  currencyCode: string
  jobTitles: JobTitleInsight[]
}

export interface JobTitleInsight {
  jobTitle: string
  department: string
  headcount: number
  salary: SalaryStats
}
