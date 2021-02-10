package uk.nhs.tis.sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.mapper.PostDtoToDataDmsDtoMapper;
import uk.nhs.tis.sync.mapper.TrustDtoToDataDmsDtoMapper;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DmsRecordAssemblerTest {

  private DmsRecordAssembler dmsRecordAssembler;

  private ObjectMapper objectMapper;

  private PostDTO postDTO;

  @Mock
  private PostDtoToDataDmsDtoMapper postDtoToDataDmsDtoMapper;

  @Mock
  TrustDtoToDataDmsDtoMapper trustDtoToDataDmsDtoMapper;

  @Before
  public void setUp() {
    dmsRecordAssembler = new DmsRecordAssembler();
    objectMapper = new ObjectMapper();

    PostDTO newPost = new PostDTO();
    newPost.setId(184668L);

    postDTO = new PostDTO();
    postDTO.setId(44381L);
    postDTO.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDTO.status(Status.CURRENT);
    postDTO.employingBodyId(287L);
    postDTO.trainingBodyId(1464L);
    postDTO.newPost(newPost);
    postDTO.oldPost(null);
    postDTO.owner("Health Education England North West London");
    postDTO.intrepidId("128374444");
  }

  @Test
  public void shouldReturnARecordAsAJsonStringWhenPassedADto() throws JsonProcessingException {
    String actualRecord = dmsRecordAssembler.buildRecord(postDTO);
    Map actualRecordMap = objectMapper.readValue(actualRecord, Map.class);
    Map metadata = (Map) actualRecordMap.get("metadata");

    String timestamp = (String) metadata.get("timestamp");

    String expectedRecord = "{\n" +
        "\"data\":\t{\n" +
        "\"id\":\t44381,\n" +
        "\"nationalPostNumber\":\t\"EAN/8EJ83/094/SPR/001\",\n" +
        "\"status\":\t\"CURRENT\",\n" +
        "\"employingBodyId\":\t287,\n" +
        "\"trainingBodyId\":\t1464,\n" +
        "\"newPostId\":\t184668,\n" +
        "\"owner\":\t\"Health Education England North West London\",\n" +
        "\"intrepidId\":\t\"128374444\"\n" +
        "},\n" +
        "\"metadata\":\t{\n" +
        "\"timestamp\":\t\"" + timestamp + "\",\n" +
        "\"record-type\":\t\"data\",\n" +
        "\"operation\":\t\"load\",\n" +
        "\"partition-key-type\":\t\"schema-table\",\n" +
        "\"schema-name\":\t\"tcs\",\n" +
        "\"table-name\":\t\"Post\",\n" +
        "\"transaction-id\":\t\"transaction-id\"\n" +
        "}\n" +
        "}";

    Map<String, String> expectedRecordMap = objectMapper.readValue(expectedRecord, Map.class);

    assertEquals(expectedRecordMap.toString(), actualRecordMap.toString());
  }

}
