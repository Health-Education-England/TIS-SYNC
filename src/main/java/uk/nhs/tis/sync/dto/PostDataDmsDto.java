package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PostDataDmsDto {

  @JsonProperty("id") String id;
  @JsonProperty("nationalPostNumber") String nationalPostNumber;
  @JsonProperty("status") String status;
  @JsonProperty("employingBodyId") String employingBodyId;
  @JsonProperty("trainingBodyId") String trainingBodyId;
  @JsonProperty("oldPostId") String oldPostId;
  @JsonProperty("newPostId") String newPostId;
  @JsonProperty("owner") String owner;
  @JsonProperty("intrepidId") String intrepidId;

  /**
   * Constructor for a PostDataDmsDto, an object which represents the "data" portion of a record.
   * A DmsDto will have it as a field along with a MetadataDto. It's meant to reflect only the
   * relevant fields of a PostDto.
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PostDataDmsDto() {
    //Constructor for a PostDataDmsDto
  }
}
