import type { Employee, EmployeeInput } from '../api/types'

/** Form fields as typed by the user; everything is text until submitted. */
export interface EmployeeForm {
  employeeCode: string
  fullName: string
  email: string
  department: string
  jobTitle: string
  countryCode: string
  annualSalary: string
  joiningDate: string
}

export type FormErrors = Partial<Record<keyof EmployeeForm, string>>

export const emptyForm: EmployeeForm = {
  employeeCode: '',
  fullName: '',
  email: '',
  department: '',
  jobTitle: '',
  countryCode: '',
  annualSalary: '',
  joiningDate: '',
}

export function formFromEmployee(employee: Employee): EmployeeForm {
  return {
    employeeCode: employee.employeeCode,
    fullName: employee.fullName,
    email: employee.email,
    department: employee.department,
    jobTitle: employee.jobTitle,
    countryCode: employee.countryCode,
    annualSalary: String(employee.annualSalary),
    joiningDate: employee.joiningDate,
  }
}

/**
 * Mirrors the API's format rules so most mistakes are caught before a round trip.
 * The API still validates everything; business rules such as duplicates are only checked there.
 *
 * @param today yyyy-MM-dd, passed in so the check does not depend on the clock
 */
export function validate(form: EmployeeForm, today: string): FormErrors {
  const errors: FormErrors = {}
  const required: (keyof EmployeeForm)[] =
    ['employeeCode', 'fullName', 'email', 'department', 'jobTitle', 'countryCode', 'annualSalary', 'joiningDate']
  for (const field of required) {
    if (!form[field].trim()) {
      errors[field] = 'Required'
    }
  }
  if (form.employeeCode.trim() && !/^EMP-\d{5}$/.test(form.employeeCode.trim())) {
    errors.employeeCode = 'Must look like EMP-00001'
  }
  if (form.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = 'Enter a valid email'
  }
  if (form.annualSalary.trim() && !/^[1-9]\d*$/.test(form.annualSalary.trim())) {
    errors.annualSalary = 'Enter a whole number greater than 0'
  }
  if (form.joiningDate && form.joiningDate > today) {
    errors.joiningDate = 'Cannot be in the future'
  }
  return errors
}

export function toInput(form: EmployeeForm): EmployeeInput {
  return {
    employeeCode: form.employeeCode.trim(),
    fullName: form.fullName.trim(),
    email: form.email.trim(),
    department: form.department,
    jobTitle: form.jobTitle,
    countryCode: form.countryCode,
    annualSalary: Number(form.annualSalary.trim()),
    joiningDate: form.joiningDate,
  }
}
