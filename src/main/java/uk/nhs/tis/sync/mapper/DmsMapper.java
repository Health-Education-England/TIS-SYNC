package uk.nhs.tis.sync.mapper;

import java.util.Collections;
import java.util.List;

/**
 * A top level mapper interface to allow generic calling of mappers.
 *
 * @param <I> The input type.
 * @param <O> The output type.
 */
public interface DmsMapper<I, O> {

  /**
   * Convert an entity DTO input to a DMS DTO output.
   *
   * @param input The input to convert.
   * @return The converted DMS DTO.
   */
  O toDmsDto(I input);

  /**
   * Convert an entity DTO input to a DMS DTO output.
   *
   * @param input The input to convert.
   * @return The list of converted DMS DTOs
   */
  default List<O> toListDmsDto(I input) {
    return Collections.singletonList(toDmsDto(input));
  }

  /**
   * Convert from an object to a list of DMS DTOs, a {@link ClassCastException} will be thrown if
   * the provided object is not of type {@link I}.
   *
   * @param object The object to convert.
   * @return The converted object.
   */
  default List<O> objectToDmsDto(Object object) {
    return toListDmsDto((I) object);
  }
}
