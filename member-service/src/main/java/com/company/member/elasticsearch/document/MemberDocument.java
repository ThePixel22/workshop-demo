package com.company.member.elasticsearch.document;

import com.company.member.entity.MemberStatus;
import com.company.member.entity.MembershipTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDocument {
    @Id
    private Long memberId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String email;

    @Field(type = FieldType.Keyword)
    private MembershipTier tier;

    @Field(type = FieldType.Keyword)
    private MemberStatus status;
}
