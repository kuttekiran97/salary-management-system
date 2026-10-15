import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
  InputAdornment,
  MenuItem,
  Paper,
  Snackbar,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import SearchIcon from '@mui/icons-material/Search'
import { DataGrid, type GridColDef, type GridSortModel } from '@mui/x-data-grid'
import { api } from '../api/client'
import type { Employee, EmployeeSort } from '../api/types'
import { formatCount, formatDate, formatMoney } from '../format'
import { useApi } from '../useApi'
import EmployeeFormDialog from './EmployeeFormDialog'

const PAGE_SIZES = [25, 50, 100]
const SEARCH_DELAY_MS = 300

/** Grid columns that can be sorted, and the API sort each one maps to. */
const SORT_BY_FIELD: Record<string, EmployeeSort> = {
  employeeCode: 'EMPLOYEE_CODE',
  fullName: 'NAME',
  annualSalaryUsd: 'SALARY',
  joiningDate: 'JOINING_DATE',
}

type DialogState = { kind: 'closed' } | { kind: 'form'; employee?: Employee } | { kind: 'delete'; employee: Employee }

export default function EmployeesPage() {
  const [searchText, setSearchText] = useState('')
  const [search, setSearch] = useState('')
  const [country, setCountry] = useState('')
  const [department, setDepartment] = useState('')
  const [jobTitle, setJobTitle] = useState('')
  const [pagination, setPagination] = useState({ page: 0, pageSize: 25 })
  const [sortModel, setSortModel] = useState<GridSortModel>([{ field: 'fullName', sort: 'asc' }])
  const [dialog, setDialog] = useState<DialogState>({ kind: 'closed' })
  const [message, setMessage] = useState<string>()

  // Wait until typing pauses before searching, instead of querying on every keystroke
  useEffect(() => {
    const timer = setTimeout(() => {
      setSearch(searchText.trim())
      setPagination((p) => ({ ...p, page: 0 }))
    }, SEARCH_DELAY_MS)
    return () => clearTimeout(timer)
  }, [searchText])

  const referenceData = useApi(api.referenceData, [])
  const sort = sortModel[0]
  const employees = useApi(
    () => api.listEmployees({
      search,
      country,
      department,
      jobTitle,
      sort: SORT_BY_FIELD[sort?.field] ?? 'NAME',
      descending: sort?.sort === 'desc',
      page: pagination.page,
      size: pagination.pageSize,
    }),
    [search, country, department, jobTitle, sort?.field, sort?.sort, pagination.page, pagination.pageSize],
  )

  const jobTitles = useMemo(() => {
    const departments = referenceData.data?.departments ?? []
    return department
      ? departments.find((d) => d.name === department)?.jobTitles ?? []
      : departments.flatMap((d) => d.jobTitles)
  }, [referenceData.data, department])

  /** Changing a filter returns to the first page, since the old page may no longer exist. */
  const filterSetter = (setter: (value: string) => void) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setter(event.target.value)
    setPagination((p) => ({ ...p, page: 0 }))
  }

  const columns: GridColDef<Employee>[] = [
    { field: 'employeeCode', headerName: 'Code', width: 110 },
    { field: 'fullName', headerName: 'Name', flex: 1, minWidth: 160 },
    { field: 'jobTitle', headerName: 'Job title', flex: 1, minWidth: 170, sortable: false },
    { field: 'department', headerName: 'Department', width: 140, sortable: false },
    { field: 'countryName', headerName: 'Country', width: 140, sortable: false },
    {
      field: 'annualSalary',
      headerName: 'Salary (local)',
      width: 150,
      align: 'right',
      headerAlign: 'right',
      sortable: false,
      valueFormatter: (_value, row) => formatMoney(row.annualSalary, row.currencyCode),
    },
    {
      field: 'annualSalaryUsd',
      headerName: 'Salary (USD)',
      description: 'Converted at a fixed rate so salaries in different currencies can be compared and sorted',
      width: 130,
      align: 'right',
      headerAlign: 'right',
      valueFormatter: (value: number) => formatMoney(value, 'USD'),
    },
    {
      field: 'joiningDate',
      headerName: 'Joined',
      width: 120,
      valueFormatter: (value: string) => formatDate(value),
    },
    {
      field: 'actions',
      headerName: '',
      width: 96,
      sortable: false,
      renderCell: ({ row }) => (
        <>
          <Tooltip title="Edit">
            <IconButton aria-label={`Edit ${row.fullName}`} onClick={() => setDialog({ kind: 'form', employee: row })}>
              <EditOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton aria-label={`Delete ${row.fullName}`} onClick={() => setDialog({ kind: 'delete', employee: row })}>
              <DeleteOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </>
      ),
    },
  ]

  async function confirmDelete(employee: Employee) {
    try {
      await api.deleteEmployee(employee.id)
      setMessage(`${employee.fullName} was removed`)
      employees.reload()
    } catch (e) {
      setMessage(e instanceof Error ? e.message : 'Could not remove the employee')
    } finally {
      setDialog({ kind: 'closed' })
    }
  }

  return (
    // minmax(0, 1fr) lets the column shrink to the screen; the wide table then scrolls inside its own box
    <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: 'minmax(0, 1fr)' }}>
      <Box sx={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1 }}>
        <Box>
          <Typography variant="h5" component="h2">Employees</Typography>
          <Typography color="text.secondary">
            {employees.data ? `${formatCount(employees.data.totalItems)} employees` : ' '}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          disabled={!referenceData.data}
          onClick={() => setDialog({ kind: 'form' })}
        >
          Add employee
        </Button>
      </Box>

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '2fr 1fr 1fr 1fr' } }}>
        <TextField
          label="Search"
          placeholder="Name, email or employee code"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          size="small"
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> } }}
        />
        <TextField label="Country" select size="small" value={country} onChange={filterSetter(setCountry)}>
          <MenuItem value="">All countries</MenuItem>
          {referenceData.data?.countries.map((c) => <MenuItem key={c.code} value={c.code}>{c.name}</MenuItem>)}
        </TextField>
        <TextField
          label="Department"
          select
          size="small"
          value={department}
          onChange={filterSetter((value) => {
            setDepartment(value)
            setJobTitle('')
          })}
        >
          <MenuItem value="">All departments</MenuItem>
          {referenceData.data?.departments.map((d) => <MenuItem key={d.name} value={d.name}>{d.name}</MenuItem>)}
        </TextField>
        <TextField label="Job title" select size="small" value={jobTitle} onChange={filterSetter(setJobTitle)}>
          <MenuItem value="">All job titles</MenuItem>
          {jobTitles.map((title) => <MenuItem key={title} value={title}>{title}</MenuItem>)}
        </TextField>
      </Box>

      {employees.error && (
        <Alert severity="error" action={<Button color="inherit" onClick={employees.reload}>Retry</Button>}>
          Could not load employees: {employees.error.message}
        </Alert>
      )}

      <Paper variant="outlined" sx={{ height: 640 }}>
        <DataGrid
          rows={employees.data?.items ?? []}
          columns={columns}
          rowCount={employees.data?.totalItems ?? 0}
          loading={employees.loading}
          paginationMode="server"
          sortingMode="server"
          paginationModel={pagination}
          onPaginationModelChange={setPagination}
          pageSizeOptions={PAGE_SIZES}
          sortModel={sortModel}
          onSortModelChange={(model) => {
            setSortModel(model)
            setPagination((p) => ({ ...p, page: 0 }))
          }}
          disableColumnFilter
          disableColumnMenu
          disableRowSelectionOnClick
          // At most 100 rows are shown per page, so rendering them all is cheap
          disableVirtualization
          localeText={{ noRowsLabel: 'No employees match these filters' }}
          sx={{ border: 0 }}
        />
      </Paper>

      {dialog.kind === 'form' && referenceData.data && (
        <EmployeeFormDialog
          employee={dialog.employee}
          referenceData={referenceData.data}
          onClose={() => setDialog({ kind: 'closed' })}
          onSaved={(saved) => {
            setDialog({ kind: 'closed' })
            setMessage(dialog.employee ? `${saved.fullName} was updated` : `${saved.fullName} was added`)
            employees.reload()
          }}
        />
      )}

      {dialog.kind === 'delete' && (
        <Dialog open onClose={() => setDialog({ kind: 'closed' })}>
          <DialogTitle>Remove {dialog.employee.fullName}?</DialogTitle>
          <DialogContent>
            <DialogContentText>
              This permanently deletes {dialog.employee.employeeCode} and their salary record.
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialog({ kind: 'closed' })}>Cancel</Button>
            <Button color="error" variant="contained" onClick={() => confirmDelete(dialog.employee)}>Remove</Button>
          </DialogActions>
        </Dialog>
      )}

      <Snackbar open={Boolean(message)} autoHideDuration={4000} onClose={() => setMessage(undefined)} message={message} />
    </Box>
  )
}
