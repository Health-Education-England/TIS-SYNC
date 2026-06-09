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

import com.transformuk.hee.tis.tcs.service.job.post.PostView;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.repository.PostElasticSearchRepository;

/**
 * Class for Post Elasticsearch service.
 */
@Component
public class PostElasticSearchService {

  private PostElasticSearchRepository postElasticSearchRepository;

  /**
   * Constructor for Post Elasticsearch service.
   */
  public PostElasticSearchService(PostElasticSearchRepository postElasticSearchRepository) {
    this.postElasticSearchRepository = postElasticSearchRepository;
  }

  /**
   * Method for saving documents in posts index.
   */
  public void saveDocuments(List<PostView> postViews) {
    if (CollectionUtils.isNotEmpty(postViews)) {
      postElasticSearchRepository.saveAll(postViews);
    }
  }
}
