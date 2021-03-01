package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PostDmsDto {

  String id;
  String nationalPostNumber;
  String status;
  String employingBodyId;
  String trainingBodyId;
  String oldPostId;
  String newPostId;
  String owner;
  String intrepidId;
}
