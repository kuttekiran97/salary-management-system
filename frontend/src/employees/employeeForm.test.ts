import { emptyForm, formFromEmployee, toInput, validate, type EmployeeForm } from './employeeForm'
import { priya } from '../testData'

const TODAY = '2026-10-15'
const valid: EmployeeForm = formFromEmployee(priya)

describe('validate', () => {
  it('accepts a complete, valid form', () => {
    expect(validate(valid, TODAY)).toEqual({})
  })

  it('marks every empty field as required', () => {
    expect(Object.keys(validate(emptyForm, TODAY))).toHaveLength(8)
  })

  it('checks the employee code format', () => {
    expect(validate({ ...valid, employeeCode: 'E-1' }, TODAY).employeeCode).toBe('Must look like EMP-00001')
  })

  it('checks the email format', () => {
    expect(validate({ ...valid, email: 'priya@' }, TODAY).email).toBe('Enter a valid email')
  })

  it.each(['0', '-5', '12.5', 'abc'])('rejects salary %s', (salary) => {
    expect(validate({ ...valid, annualSalary: salary }, TODAY).annualSalary).toBeDefined()
  })

  it('accepts today but not a future joining date', () => {
    expect(validate({ ...valid, joiningDate: TODAY }, TODAY).joiningDate).toBeUndefined()
    expect(validate({ ...valid, joiningDate: '2026-10-16' }, TODAY).joiningDate).toBe('Cannot be in the future')
  })
})

describe('toInput', () => {
  it('trims text and converts the salary to a number', () => {
    expect(toInput({ ...valid, fullName: '  Priya Sharma ', annualSalary: ' 1800000 ' })).toMatchObject({
      fullName: 'Priya Sharma',
      annualSalary: 1800000,
    })
  })
})
