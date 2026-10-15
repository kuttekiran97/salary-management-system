import type { Employee, ReferenceData } from './api/types'

export const referenceData: ReferenceData = {
  countries: [
    { code: 'IN', name: 'India', currencyCode: 'INR', usdRate: 0.012 },
    { code: 'US', name: 'United States', currencyCode: 'USD', usdRate: 1 },
  ],
  departments: [
    { name: 'Engineering', jobTitles: ['Software Engineer', 'QA Engineer'] },
    { name: 'Finance', jobTitles: ['Accountant'] },
  ],
}

export const priya: Employee = {
  id: 1,
  employeeCode: 'EMP-00001',
  fullName: 'Priya Sharma',
  email: 'priya@acme.com',
  jobTitle: 'Software Engineer',
  department: 'Engineering',
  countryCode: 'IN',
  countryName: 'India',
  currencyCode: 'INR',
  annualSalary: 1800000,
  annualSalaryUsd: 21600,
  joiningDate: '2022-04-01',
}
