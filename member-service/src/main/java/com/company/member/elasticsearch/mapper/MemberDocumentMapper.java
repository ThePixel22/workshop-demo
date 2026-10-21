package com.company.member.elasticsearch.mapper;

import com.company.member.elasticsearch.document.MemberDocument;
import com.company.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberDocumentMapper {

    public MemberDocument toDocument(Member member) {
        if (member == null) {
            return null;
        }

        return new MemberDocument(
                member.getMemberId(),
                member.getName(),
                member.getEmail(),
                member.getTier(),
                member.getStatus()
        );
    }
}
