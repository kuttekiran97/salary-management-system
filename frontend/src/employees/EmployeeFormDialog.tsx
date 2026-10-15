import { useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  InputAdornment,
  MenuItem,
  TextField,
} from '@mui/material'
import { ApiError, api } from '../api/client'
import type { Employee, ReferenceData } from '../api/types'
import { emptyForm, formFromEmployee, toInput, validate, type EmployeeForm, type FormErrors } from './employeeForm'

interface Props {
  /** The employee to edit, or undefined to add a new one */
  employee?: Employee
  referenceData: ReferenceData
  onClose: () => void
  onSaved: (employee: Employee) => void
}

/** Today's local date as yyyy-MM-dd (toISOString would give the UTC date, which can be a day off). */
const today = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

/** Add or edit an employee. Mount it only while open, so each opening starts with fresh state. */
export default function EmployeeFormDialog({ employee, referenceData, onClose, onSaved }: Props) {
  const [form, setForm] = useState<EmployeeForm>(employee ? formFromEmployee(employee) : emptyForm)
  const [errors, setErrors] = useState<FormErrors>({})
  const [formError, setFormError] = useState<string>()
  const [saving, setSaving] = useState(false)

  const jobTitles = referenceData.departments.find((d) => d.name === form.department)?.jobTitles ?? []
  const currency = referenceData.countries.find((c) => c.code === form.countryCode)?.currencyCode

  const set = (field: keyof EmployeeForm) => (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value
    setForm((current) => ({
      ...current,
      [field]: value,
      // A new department has different job titles, so the old title no longer applies
      ...(field === 'department' ? { jobTitle: '' } : {}),
    }))
    setErrors((current) => ({ ...current, [field]: undefined }))
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    const found = validate(form, today())
    setErrors(found)
    if (Object.keys(found).length > 0) {
      return
    }
    setSaving(true)
    setFormError(undefined)
    try {
      const input = toInput(form)
      onSaved(employee ? await api.updateEmployee(employee.id, input) : await api.createEmployee(input))
    } catch (e) {
      if (e instanceof ApiError && Object.keys(e.fieldErrors).length > 0) {
        setErrors(e.fieldErrors as FormErrors)
      } else {
        setFormError(e instanceof Error ? e.message : 'Could not save the employee')
      }
    } finally {
      setSaving(false)
    }
  }

  const field = (name: keyof EmployeeForm, label: string) => ({
    name,
    label,
    value: form[name],
    onChange: set(name),
    error: Boolean(errors[name]),
    helperText: errors[name] ?? ' ',
    fullWidth: true,
  })

  return (
    <Dialog open onClose={onClose} maxWidth="sm" fullWidth>
      <Box component="form" onSubmit={submit} noValidate>
        <DialogTitle>{employee ? `Edit ${employee.fullName}` : 'Add employee'}</DialogTitle>
        <DialogContent>
          {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, columnGap: 2, pt: 1 }}>
            <TextField {...field('employeeCode', 'Employee code')} placeholder="EMP-00001" autoFocus />
            <TextField {...field('fullName', 'Full name')} />
            <TextField {...field('email', 'Email')} type="email" sx={{ gridColumn: '1 / -1' }} />
            <TextField {...field('department', 'Department')} select>
              {referenceData.departments.map((d) => <MenuItem key={d.name} value={d.name}>{d.name}</MenuItem>)}
            </TextField>
            <TextField
              {...field('jobTitle', 'Job title')}
              select
              disabled={!form.department}
              helperText={errors.jobTitle ?? (form.department ? ' ' : 'Choose a department first')}
            >
              {jobTitles.map((title) => <MenuItem key={title} value={title}>{title}</MenuItem>)}
            </TextField>
            <TextField {...field('countryCode', 'Country')} select>
              {referenceData.countries.map((c) => (
                <MenuItem key={c.code} value={c.code}>{c.name} ({c.currencyCode})</MenuItem>
              ))}
            </TextField>
            <TextField
              {...field('annualSalary', 'Annual salary')}
              inputMode="numeric"
              slotProps={{
                input: { startAdornment: currency && <InputAdornment position="start">{currency}</InputAdornment> },
              }}
              helperText={errors.annualSalary ?? 'In the local currency of the country'}
            />
            <TextField
              {...field('joiningDate', 'Joining date')}
              type="date"
              slotProps={{ inputLabel: { shrink: true }, htmlInput: { max: today() } }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" loading={saving}>
            {employee ? 'Save changes' : 'Add employee'}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}
