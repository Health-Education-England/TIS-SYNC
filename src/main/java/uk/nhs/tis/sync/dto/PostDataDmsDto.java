package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Value;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
public class PostDataDmsDto {

  String id;
  String nationalPostNumber;
  String status;
  String employingBodyId;
  String trainingBodyId;
  String oldPostId;
  String newPostId;
  String owner;
  String intrepidId;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PostDataDmsDto(@JsonProperty("id") String id,
                        @JsonProperty("nationalPostNumber") String nationalPostNumber,
                        @JsonProperty ("status") String status,
                        @JsonProperty("employingBodyId") String employingBodyId,
                        @JsonProperty("trainingBodyId") String trainingBodyId,
                        @JsonProperty("oldPostId") String oldPostId,
                        @JsonProperty("newPostId") String newPostId,
                        @JsonProperty("owner") String owner,
                        @JsonProperty("intrepidId") String intrepidId) {
    this.id = id;
    this.nationalPostNumber = nationalPostNumber;
    this.status = status;
    this.employingBodyId = employingBodyId;
    this.trainingBodyId = trainingBodyId;
    this.oldPostId = oldPostId;
    this.newPostId = newPostId;
    this.owner = owner;
    this.intrepidId = intrepidId;
  }
}
