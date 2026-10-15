package com.acme.salary.insights;

import com.acme.salary.insights.InsightsService.CountryInsight;
import com.acme.salary.insights.InsightsService.JobTitleInsights;
import com.acme.salary.insights.InsightsService.Overview;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public Overview overview() {
        return service.overview();
    }

    @GetMapping("/by-country")
    public List<CountryInsight> byCountry() {
        return service.byCountry();
    }

    @GetMapping("/by-job-title")
    public JobTitleInsights byJobTitle(@RequestParam(required = false) String country) {
        return service.byJobTitle(country);
    }
}
