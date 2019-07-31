package uk.nhs.tis.sync.api;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;


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
    response.append("# Hello Prometheus!" + PROMETHEUS_LINE_SEPARATOR);
    // TODO Add filtering by requested metrics
    meterRegistry.forEachMeter(m -> response.append(format(m)));
    return response.toString();
  }

  private static final String PROMETHEUS_LINE_SEPARATOR = "\n";
  private static final String PROMETHEUS_FIELD_SEPARATOR = " ";

  /**
   * Creates a string in a format that Prometheus understands.
   * 
   * @param meter - The Meter to Format
   * @return A formatted {@link String}
   */
  private String format(Meter meter) {
    // Build Metric header
    StringBuilder nameBuilder = new StringBuilder();
    nameBuilder.append(meter.getId().getName());
    List<Tag> tags = meter.getId().getTags();
    if (CollectionUtils.isNotEmpty(tags)) {
      nameBuilder.append("{");
      meter.getId().getTags()
          .forEach(tag -> nameBuilder.append(tag.getKey() + "=\"" + tag.getValue() + "\","));
      nameBuilder.append("}");
    }
    String meterName = nameBuilder.toString().replaceAll("\\.|\\s", "_");

    StringBuilder measureBuilder = new StringBuilder();
    measureBuilder.append(meterName);
    measureBuilder.append(PROMETHEUS_FIELD_SEPARATOR);
    Measurement measure = meter.measure().iterator().next();
    measureBuilder.append(measure.getValue() + PROMETHEUS_FIELD_SEPARATOR);
    measureBuilder.append(System.currentTimeMillis());
    measureBuilder.append(PROMETHEUS_LINE_SEPARATOR);
    return measureBuilder.toString();
  }

}
