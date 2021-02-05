package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataRequestServiceTests {

  @Mock
  private TcsServiceImpl tcsServiceImplMock;

  @Mock
  private ReferenceServiceImpl referenceServiceImplMock;

  private DataRequestService testObj;

  private AmazonSqsMessageDto messageForAPost;

  private AmazonSqsMessageDto messageForATrust;

  @Before
  public void setUp() {
    tcsServiceImplMock = mock(TcsServiceImpl.class);
    referenceServiceImplMock = mock(ReferenceServiceImpl.class);
    testObj = new DataRequestService(tcsServiceImplMock,
        referenceServiceImplMock);
    messageForAPost = new AmazonSqsMessageDto("Post", "10");
    messageForATrust = new AmazonSqsMessageDto("Trust", "20");
  }

  @Test
  public void shouldRetrieveAPostDtoWhenPassedAnAmazonSqsMessageAskingForAPost() {
    PostDTO expectedPostDto = new PostDTO();
    expectedPostDto.setId(10L);
    when(tcsServiceImplMock.getPostById(10L)).thenReturn(expectedPostDto);

    messageForAPost = new AmazonSqsMessageDto("Post", "10");
    Object retrievedDto = testObj.retrieveDto(messageForAPost);

    Assert.assertEquals(expectedPostDto, retrievedDto);
    verify(tcsServiceImplMock).getPostById(10L);
  }

  @Test
  public void shouldRetrieveATrustDtoWhenPassedAnAmazonSqsMessageRequestingATrust() {
    TrustDTO expectedTrustDto = new TrustDTO();
    expectedTrustDto.setId(20L);
    when(referenceServiceImplMock.findTrustById(20L)).thenReturn(expectedTrustDto);

    Object retrievedDto = testObj.retrieveDto(messageForATrust);

    verify(referenceServiceImplMock).findTrustById(20L);
    Assert.assertEquals(expectedTrustDto, retrievedDto);
  }


  @Test
  public void shouldCatchExceptionWhenAPostDtoIsNotFoundAndDtoWillReturnNull() {
    when(tcsServiceImplMock.getPostById(10L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    Object post = testObj.retrieveDto(messageForAPost);
    Assert.assertNull(post);

    Throwable throwable1 = catchThrowable(() -> testObj.retrieveDto(messageForAPost));
    assertThat(throwable1).isNull();
  }

  @Test
  public void shouldCatchExceptionWhenATrustDtoIsNotFoundAndDtoWillReturnNull() {
    when(referenceServiceImplMock.findTrustById(20L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    Object trust = testObj.retrieveDto(messageForATrust);
    Assert.assertNull(trust);

    Throwable throwable2 = catchThrowable(() -> testObj.retrieveDto(messageForATrust));
    assertThat(throwable2).isNull();
  }
}
