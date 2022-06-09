package uk.nhs.tis.sync.mapper;

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
   * Convert from an object to a DMS DTO output, a {@link ClassCastException} will be thrown if the
   * provided object is not of type {@link I}.
   *
   * @param object The object to convert.
   * @return The converted object.
   */
  default O objectToDmsDto(Object object) {
    return toDmsDto((I) object);
  }
}
