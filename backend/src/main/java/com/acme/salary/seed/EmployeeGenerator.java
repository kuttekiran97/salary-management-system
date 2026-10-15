package com.acme.salary.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * Generates realistic, reproducible employees: the same seed always gives the same people.
 *
 * <p>Salary = a US-market band for the role, scaled by a country pay factor,
 * converted to local currency and rounded to the nearest 1,000.
 */
public final class EmployeeGenerator {

    public record GeneratedEmployee(String employeeCode, String fullName, String email, String jobTitle,
                                    String department, String countryCode, long annualSalary,
                                    LocalDate joiningDate) {
    }

    private record CountryProfile(String code, int weight, double payFactor,
                                  List<String> firstNames, List<String> lastNames) {
    }

    private record Role(String department, String title, int weight, int minUsd, int maxUsd) {
    }

    private static final List<CountryProfile> COUNTRIES = List.of(
            new CountryProfile("IN", 35, 0.30,
                    List.of("Aarav", "Priya", "Rohan", "Ananya", "Vikram", "Sneha", "Arjun", "Kavya", "Rahul",
                            "Divya", "Karan", "Meera", "Aditya", "Pooja", "Siddharth", "Neha", "Ishaan", "Riya",
                            "Varun", "Tanvi", "Nikhil", "Shreya", "Manish", "Aishwarya", "Harsh", "Lakshmi",
                            "Deepak", "Swati", "Gaurav", "Nandini", "Suresh", "Anjali"),
                    List.of("Sharma", "Patel", "Iyer", "Reddy", "Nair", "Gupta", "Mehta", "Kulkarni", "Singh", "Rao",
                            "Joshi", "Desai", "Menon", "Kapoor", "Verma", "Bhat", "Chopra", "Pillai", "Agarwal",
                            "Banerjee", "Malhotra", "Saxena", "Mishra", "Chatterjee", "Shetty", "Naidu", "Pandey",
                            "Krishnan")),
            new CountryProfile("US", 25, 1.00,
                    List.of("James", "Emily", "Michael", "Sarah", "David", "Jessica", "Daniel", "Ashley", "Matthew",
                            "Olivia", "Chris", "Megan", "Ryan", "Lauren", "Andrew", "Hannah", "Joshua", "Rachel",
                            "Brandon", "Natalie", "Tyler", "Samantha", "Kevin", "Amanda", "Justin", "Brittany"),
                    List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Wilson",
                            "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "White", "Harris", "Martin",
                            "Thompson", "Martinez", "Robinson", "Clark", "Lewis", "Walker", "Young", "Allen", "Scott")),
            new CountryProfile("GB", 10, 0.75,
                    List.of("Oliver", "Amelia", "Harry", "Isla", "George", "Ava", "Jack", "Sophie", "Thomas",
                            "Grace", "Charlie", "Emily", "James", "Poppy", "William", "Freya", "Alfie", "Lily"),
                    List.of("Clarke", "Evans", "Hughes", "Wright", "Walker", "Roberts", "Turner", "Hall", "Wood",
                            "Green", "Edwards", "Baker", "Patel", "Cooper", "Ward", "Morris", "Bennett")),
            new CountryProfile("DE", 10, 0.75,
                    List.of("Lukas", "Mia", "Leon", "Hannah", "Felix", "Lena", "Jonas", "Lea", "Paul", "Laura",
                            "Maximilian", "Emma", "Tim", "Sophie", "Niklas", "Anna", "Jan", "Marie"),
                    List.of("Muller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker",
                            "Hoffmann", "Schulz", "Koch", "Richter", "Klein", "Wolf", "Neumann", "Schwarz", "Braun")),
            new CountryProfile("CA", 8, 0.80,
                    List.of("Liam", "Emma", "Noah", "Charlotte", "Ethan", "Chloe", "Lucas", "Zoe", "Owen", "Ava",
                            "Benjamin", "Sophia", "Nathan", "Emily", "Logan", "Maya"),
                    List.of("Tremblay", "Martin", "Roy", "Gagnon", "Leblanc", "Campbell", "MacDonald", "Stewart",
                            "Bouchard", "Gauthier", "Morin", "Lavoie", "Fortin", "Wilson", "Thompson")),
            new CountryProfile("AU", 6, 0.80,
                    List.of("Jack", "Ruby", "William", "Matilda", "Henry", "Ella", "Lachlan", "Chloe", "Cooper",
                            "Isla", "Oliver", "Zoe", "Thomas", "Mia", "Riley", "Sienna"),
                    List.of("Kelly", "Ryan", "King", "Murphy", "Young", "Mitchell", "Harris", "Lee", "Walsh",
                            "Kennedy", "Morgan", "Bell", "Ross", "Hunter", "Reid")),
            new CountryProfile("SG", 6, 0.70,
                    List.of("Wei Ling", "Jun Jie", "Hui Min", "Kai Wen", "Mei Ling", "Zhi Hao", "Siti", "Arun",
                            "Jia Hui", "Wei Jie", "Xin Yi", "Ming Hao", "Nur Aisyah", "Ravi", "Shu Fen", "Darren"),
                    List.of("Tan", "Lim", "Lee", "Ng", "Ong", "Wong", "Goh", "Chua", "Koh", "Teo", "Ang", "Yeo",
                            "Chan", "Sim", "Rahman")));

    private static final List<Role> ROLES = List.of(
            new Role("Engineering", "Software Engineer", 22, 90_000, 140_000),
            new Role("Engineering", "Senior Software Engineer", 12, 140_000, 200_000),
            new Role("Engineering", "Engineering Manager", 4, 180_000, 250_000),
            new Role("Engineering", "QA Engineer", 8, 70_000, 110_000),
            new Role("Product", "Product Manager", 5, 120_000, 180_000),
            new Role("Product", "Product Designer", 4, 85_000, 130_000),
            new Role("Data", "Data Analyst", 6, 65_000, 100_000),
            new Role("Data", "Data Scientist", 5, 110_000, 170_000),
            new Role("Sales", "Sales Executive", 10, 55_000, 95_000),
            new Role("Sales", "Account Manager", 6, 70_000, 120_000),
            new Role("Marketing", "Marketing Specialist", 5, 55_000, 90_000),
            new Role("Human Resources", "HR Generalist", 4, 55_000, 85_000),
            new Role("Human Resources", "Recruiter", 3, 55_000, 90_000),
            new Role("Finance", "Accountant", 3, 60_000, 95_000),
            new Role("Finance", "Financial Analyst", 3, 75_000, 115_000));

    private static final int MAX_YEARS_OF_SERVICE = 10;

    private EmployeeGenerator() {
    }

    /**
     * @param usdRates USD value of one unit of each country's currency, keyed by country code
     * @param today    latest possible joining date; passed in so output does not depend on the clock
     */
    public static List<GeneratedEmployee> generate(int count, Map<String, BigDecimal> usdRates,
                                                   LocalDate today, long seed) {
        Random random = new Random(seed);
        Map<String, Integer> emailUses = new HashMap<>();
        List<GeneratedEmployee> employees = new ArrayList<>(count);

        for (int i = 1; i <= count; i++) {
            CountryProfile country = pick(COUNTRIES, CountryProfile::weight, random);
            Role role = pick(ROLES, Role::weight, random);
            String first = country.firstNames().get(random.nextInt(country.firstNames().size()));
            String last = country.lastNames().get(random.nextInt(country.lastNames().size()));

            employees.add(new GeneratedEmployee(
                    "EMP-%05d".formatted(i),
                    first + " " + last,
                    uniqueEmail(first, last, emailUses),
                    role.title(),
                    role.department(),
                    country.code(),
                    localSalary(role, country, usdRates.get(country.code()), random),
                    today.minusDays(random.nextInt(MAX_YEARS_OF_SERVICE * 365))));
        }
        return employees;
    }

    private static long localSalary(Role role, CountryProfile country, BigDecimal usdRate, Random random) {
        double usd = (role.minUsd() + random.nextDouble() * (role.maxUsd() - role.minUsd())) * country.payFactor();
        BigDecimal local = BigDecimal.valueOf(usd).divide(usdRate, 0, RoundingMode.HALF_UP);
        long roundedToThousand = local.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP).longValue() * 1000;
        return Math.max(roundedToThousand, 1000);
    }

    /** firstname.lastname@acme.com, with a number added when the name is already taken. */
    private static String uniqueEmail(String first, String last, Map<String, Integer> emailUses) {
        String base = (first + "." + last).toLowerCase(Locale.ROOT).replace(' ', '.');
        int uses = emailUses.merge(base, 1, Integer::sum);
        return (uses == 1 ? base : base + uses) + "@acme.com";
    }

    private static <T> T pick(List<T> options, ToIntFunction<T> weight, Random random) {
        int total = options.stream().mapToInt(weight).sum();
        int ticket = random.nextInt(total);
        for (T option : options) {
            ticket -= weight.applyAsInt(option);
            if (ticket < 0) {
                return option;
            }
        }
        throw new IllegalStateException("unreachable");
    }
}
