package uk.nhs.hee.tis.sync.repository;

import com.transformuk.hee.tis.tcs.service.job.person.PersonView;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonElasticSearchRepository extends ElasticsearchRepository<PersonView, String> {
}
