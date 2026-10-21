package com.company.member.elasticsearch.repository;

import com.company.member.elasticsearch.document.MemberDocument;
import com.company.member.entity.MembershipTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface MemberSearchRepository extends ElasticsearchRepository<MemberDocument, Long> {
    List<MemberDocument> findByNameContaining(String name);

    List<MemberDocument> findByTier(MembershipTier tier);

    @Query("""
        {
          "bool": {
            "minimum_should_match": 1,
            "should": [
              {
                "match": {
                  "name": {
                    "query": "?0"
                  }
                }
              },
              {
                "wildcard": {
                  "email": {
                    "value": "*?0*",
                    "case_insensitive": true
                  }
                }
              },
              {
                "term": {
                  "tier": {
                    "value": "?0",
                    "case_insensitive": true
                  }
                }
              }
            ]
          }
        }
        """)
    Page<MemberDocument> searchByKeyword(
            String keyword,
            Pageable pageable
    );
}
