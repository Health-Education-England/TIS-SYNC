package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataRequestServiceTests {

  @Mock
  private TcsServiceImpl tcsServiceImplMock;

  @Spy
  @InjectMocks
  private DataRequestService testObj;

  @Test
  public void shouldRetrieveADtoWhenPassedAnAmazonSQSMessage() {
    PostDTO expectedPostDTO = new PostDTO();
    expectedPostDTO.setId(10L);

    AmazonSqsMessageDto amazonSQSMessageDto = new AmazonSqsMessageDto("Post", "10");

    when(tcsServiceImplMock.getPostById(10L)).thenReturn(expectedPostDTO);

    Object retrievedDto = testObj.retrieveDto(amazonSQSMessageDto);

    Assert.assertEquals(retrievedDto, expectedPostDTO);
  }

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void shouldCatchExceptionWhenADtoIsNotFoundAndDtoWillReturnNull() {
    AmazonSqsMessageDto amazonSQSMessageDto = new AmazonSqsMessageDto("Post", "10");
    when(tcsServiceImplMock.getPostById(10L)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    Object dto = testObj.retrieveDto(amazonSQSMessageDto);

    Assert.assertNull(dto);
  }
}
