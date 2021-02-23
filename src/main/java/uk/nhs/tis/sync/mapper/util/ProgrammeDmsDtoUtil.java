package uk.nhs.tis.sync.mapper.util;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeCurriculumDTO;
import org.springframework.stereotype.Component;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProgrammeDmsDtoUtil {

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Map {

  }

  @Map
  public Set<String> map(Set<ProgrammeCurriculumDTO> curricula) {
    return curricula
        .stream()
        .map(curriculum -> curriculum.getId().toString())
        .collect(Collectors.toSet());
  }
}
