package com.acme.salary.employee;

/** Fields the employee list can be sorted by. */
public enum EmployeeSort {
    NAME,
    EMPLOYEE_CODE,
    JOINING_DATE,
    /** Sorts by the USD equivalent, so salaries in different currencies compare correctly. */
    SALARY
}
