package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.junit.Assert;
import org.junit.Before;
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

  @Mock
  private ReferenceServiceImpl referenceServiceImplMock;

  @Spy
  @InjectMocks
  private DataRequestService testObj;

  private AmazonSqsMessageDto messageForAPost;

  private AmazonSqsMessageDto messageForATrust;

  @Before
  public void setUp() {
    messageForAPost = new AmazonSqsMessageDto("Post", "10");
    messageForATrust = new AmazonSqsMessageDto("Trust", "20");
  }

  @Test
  public void shouldRetrieveAPostDtoWhenPassedAnAmazonSqsMessageAskingForAPost() {
    PostDTO expectedPostDto = new PostDTO();
    expectedPostDto.setId(10L);
    when(tcsServiceImplMock.getPostById(10L)).thenReturn(expectedPostDto);

    Object retrievedDto = testObj.retrieveDto(messageForAPost);

    Assert.assertEquals(retrievedDto, expectedPostDto);
  }

  @Test
  public void shouldRetrieveATrustDtoWhenPassedAndAmazonSqsMessageAskingForATrust() {
    TrustDTO expectedTrustDto = new TrustDTO();
    expectedTrustDto.setId(20L);
    //when(referenceServiceImplMock.findTrustByTrustKnownAs(20L)).thenReturn(expectedTrustDto);

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
