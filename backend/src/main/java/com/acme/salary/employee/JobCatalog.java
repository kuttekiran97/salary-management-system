package com.acme.salary.employee;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The fixed list of departments and the job titles in each.
 *
 * <p>Free-text titles would split insights ("Sr. Engineer" vs "Senior Engineer"),
 * so titles are chosen from this list and validated against it.
 */
public final class JobCatalog {

    private static final Map<String, List<String>> TITLES_BY_DEPARTMENT;

    static {
        Map<String, List<String>> titles = new LinkedHashMap<>();
        titles.put("Engineering", List.of("Software Engineer", "Senior Software Engineer", "Engineering Manager", "QA Engineer"));
        titles.put("Product", List.of("Product Manager", "Product Designer"));
        titles.put("Data", List.of("Data Analyst", "Data Scientist"));
        titles.put("Sales", List.of("Sales Executive", "Account Manager"));
        titles.put("Marketing", List.of("Marketing Specialist"));
        titles.put("Human Resources", List.of("HR Generalist", "Recruiter"));
        titles.put("Finance", List.of("Accountant", "Financial Analyst"));
        TITLES_BY_DEPARTMENT = Collections.unmodifiableMap(titles);
    }

    private JobCatalog() {
    }

    /** Departments in display order, each with its job titles. */
    public static Map<String, List<String>> titlesByDepartment() {
        return TITLES_BY_DEPARTMENT;
    }

    /** The department a job title belongs to, or null if the title is not in the catalog. */
    public static String departmentOf(String jobTitle) {
        return TITLES_BY_DEPARTMENT.entrySet().stream()
                .filter(entry -> entry.getValue().contains(jobTitle))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static boolean isValid(String department, String jobTitle) {
        return TITLES_BY_DEPARTMENT.getOrDefault(department, List.of()).contains(jobTitle);
    }
}
