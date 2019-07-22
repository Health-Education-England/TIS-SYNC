package uk.nhs.tis.sync.api;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;


/**
 * @author JosephKelly
 *
 */
@RestController
@RequestMapping("/actuator/scrape")
// TODO Get this working... @RequestMapping("${management.endpoints.web.base-path}/scrape")
public class MetricsResource {

  @Autowired
  private MeterRegistry meterRegistry;

  public MetricsResource(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @GetMapping(produces = "text/plain")
  public String getMetricsForScrape() {
    StringBuilder response = new StringBuilder();
    response.append("# Hello Prometheus!");
    // TODO Add filtering by requested metrics
    meterRegistry.forEachMeter(m -> {
      String s = format(m);
      response.append(s);
    });
    return response.toString();
  }

  private static final String PROMETHEUS_LINE_SEPARATOR = "\n";
  private static final String PROMETHEUS_FIELD_SEPARATOR = " ";

  /**
   * Creates a string in a format that Prometheus understands. See {@link Formatter}.
   * 
   * @param meter - The Meter to Format
   * @return A formatted {@link String}
   */
  private String format(Meter meter) {
    // Build Metric header
    StringBuilder builder = new StringBuilder();
    builder.append("# HELP " + meter.getId()
        + (StringUtils.isNotEmpty(meter.getId().getDescription()) ? " a.k.a. '" + meter.getId().getDescription() + "'": "")
        + (StringUtils.isNotEmpty(meter.getId().getBaseUnit()) ? " measured in " + meter.getId().getBaseUnit() : "") + PROMETHEUS_LINE_SEPARATOR);
    builder.append("# TYPE " + meter.getId().getName() + PROMETHEUS_FIELD_SEPARATOR
        + meter.getId().getType().toString().toLowerCase() + PROMETHEUS_LINE_SEPARATOR);
    builder.append(meter.getId().getName() + /* TODO "{labelTagsGoHere" + PROMETHEUS_FIELD_SEPARATOR + "}" + */ PROMETHEUS_FIELD_SEPARATOR);
    meter.measure().forEach(measure -> {
      builder.append(measure.getValue() + PROMETHEUS_FIELD_SEPARATOR
          + (measure.getStatistic() == Statistic.VALUE ? System.currentTimeMillis() : "")
          + PROMETHEUS_LINE_SEPARATOR);
    });
    builder.append(PROMETHEUS_LINE_SEPARATOR);
    return builder.toString();
  }

}
