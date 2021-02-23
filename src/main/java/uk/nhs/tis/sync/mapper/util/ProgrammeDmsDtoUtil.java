package uk.nhs.tis.sync.mapper.util;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeCurriculumDTO;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ProgrammeDmsDtoUtil {

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Map {

  }

  /**
   * A method to map the Ids of ProgrammeCurriculumDtos contained in a set to a set of Strings.
   * @param curricula The set of ProgrammeCurriculumDtos.
   * @return          The set of Strings.
   */
  @Map
  public Set<String> map(Set<ProgrammeCurriculumDTO> curricula) {
    return curricula
        .stream()
        .map(curriculum -> curriculum.getId().toString())
        .collect(Collectors.toSet());
  }
}
