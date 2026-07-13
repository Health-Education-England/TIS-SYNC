/*
 * The MIT License (MIT)
 *
 * Copyright 2026 Crown Copyright (NHS England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.nhs.tis.sync.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.transformuk.hee.tis.tcs.service.job.post.PostView;
import com.transformuk.hee.tis.tcs.service.repository.PostElasticSearchRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostElasticSearchServiceTest {

  @Mock
  private PostElasticSearchRepository postElasticSearchRepository;

  @InjectMocks
  private PostElasticSearchService postElasticSearchService;

  @Test
  void shouldSaveDocumentsWhenPostViewIsNotEmpty() {
    PostView postView = new PostView();
    postView.setId(100L);
    postView.setNationalPostNumber("ABC/XY317/018/HT/002");

    List<PostView> postViews = Collections.singletonList(postView);

    postElasticSearchService.saveDocuments(postViews);

    verify(postElasticSearchRepository).saveAll(postViews);
  }

  @Test
  void shouldNotSaveDocumentsWhenPostViewIsEmpty() {
    postElasticSearchService.saveDocuments(Collections.emptyList());

    verify(postElasticSearchRepository, never()).saveAll(anyList());
  }

  @Test
  void shouldNotSaveDocumentsWhenPostViewsIsNull() {
    postElasticSearchService.saveDocuments(null);

    verify(postElasticSearchRepository, never()).saveAll(anyList());
  }
}
