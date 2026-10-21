package com.company.member.service;

import com.company.member.client.RedemptionServiceClient;
import com.company.member.dto.CreateMemberRequest;
import com.company.member.dto.CreateMemberResponse;
import com.company.member.elasticsearch.document.MemberDocument;
import com.company.member.elasticsearch.mapper.MemberDocumentMapper;
import com.company.member.elasticsearch.repository.MemberSearchRepository;
import com.company.member.entity.Member;
import com.company.member.entity.MemberStatus;
import com.company.member.entity.MembershipTier;
import com.company.member.entity.NightBalance;
import com.company.member.exception.DuplicateEmailException;
import com.company.member.exception.InvalidEmailFormatException;
import com.company.member.exception.MemberCreationException;
import com.company.member.repository.MemberRepository;
import com.company.member.repository.NightBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceCreateMemberTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NightBalanceRepository nightBalanceRepository;

    @Mock
    private RedemptionServiceClient redemptionServiceClient;

    @Mock
    private MemberSearchRepository memberSearchRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, nightBalanceRepository, redemptionServiceClient,
                memberSearchRepository, new MemberDocumentMapper());
    }

    @Test
    void createMember_withValidRequest_createsMemberAndInitialBalance() {
        CreateMemberRequest request = validRequest();
        given(memberRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setMemberId(101L);
            return member;
        });

        CreateMemberResponse response = memberService.createMember(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getMemberId()).isEqualTo(101L);
        assertThat(savedMember.getName()).isEqualTo("Jane Doe");
        assertThat(savedMember.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(savedMember.getTier()).isEqualTo(MembershipTier.GOLD);
        assertThat(savedMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        ArgumentCaptor<NightBalance> balanceCaptor = ArgumentCaptor.forClass(NightBalance.class);
        verify(nightBalanceRepository).save(balanceCaptor.capture());
        assertThat(balanceCaptor.getValue().getMemberId()).isEqualTo(101L);
        assertThat(balanceCaptor.getValue().getAvailableNights()).isEqualTo(5);
        ArgumentCaptor<MemberDocument> documentCaptor = ArgumentCaptor.forClass(MemberDocument.class);
        verify(memberSearchRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getMemberId()).isEqualTo(101L);
        assertThat(documentCaptor.getValue().getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getMemberId()).isEqualTo(101L);
    }

    @Test
    void createMember_doesNotAssignMemberIdBeforePersistence() {
        CreateMemberRequest request = validRequest();
        given(memberRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            assertThat(member.getMemberId()).isNull();
            member.setMemberId(101L);
            return member;
        });

        CreateMemberResponse response = memberService.createMember(request);

        assertThat(response.getMemberId()).isEqualTo(101L);
    }

    @Test
    void createMember_forDifferentMembers_returnsUniqueGeneratedIds() {
        CreateMemberRequest firstRequest = validRequest();
        CreateMemberRequest secondRequest = validRequest();
        secondRequest.setEmail("john.doe@example.com");
        given(memberRepository.existsByEmail(any())).willReturn(false);
        given(memberRepository.save(any(Member.class)))
                .willAnswer(invocation -> assignId(invocation.getArgument(0), 101L))
                .willAnswer(invocation -> assignId(invocation.getArgument(0), 102L));

        CreateMemberResponse firstResponse = memberService.createMember(firstRequest);
        CreateMemberResponse secondResponse = memberService.createMember(secondRequest);

        assertThat(firstResponse.getMemberId()).isEqualTo(101L);
        assertThat(secondResponse.getMemberId()).isEqualTo(102L);
        assertThat(firstResponse.getMemberId()).isNotEqualTo(secondResponse.getMemberId());
        verify(memberRepository, times(2)).save(any(Member.class));
        verify(nightBalanceRepository, times(2)).save(any(NightBalance.class));
    }

    @Test
    void createMember_withDuplicateEmail_rejectsRequestBeforeSaving() {
        CreateMemberRequest request = validRequest();
        given(memberRepository.existsByEmail(request.getEmail())).willReturn(true);

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already exists: jane.doe@example.com");

        verify(memberRepository, never()).save(any(Member.class));
        verify(nightBalanceRepository, never()).save(any(NightBalance.class));
    }

    @Test
    void createMember_withInvalidEmail_rejectsRequestBeforeRepositoryLookup() {
        CreateMemberRequest request = validRequest();
        request.setEmail("invalid-email");

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(InvalidEmailFormatException.class)
                .hasMessage("Email must be in a valid email format");

        verify(memberRepository, never()).existsByEmail(any());
        verify(memberRepository, never()).save(any(Member.class));
        verify(nightBalanceRepository, never()).save(any(NightBalance.class));
    }

    @Test
    void createMember_whenBalanceCreationFails_wrapsFailure() {
        CreateMemberRequest request = validRequest();
        given(memberRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setMemberId(101L);
            return member;
        });
        given(nightBalanceRepository.save(any(NightBalance.class)))
                .willThrow(new RuntimeException("database unavailable"));

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(MemberCreationException.class)
                .hasMessage("Failed to create member: database unavailable")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void createMember_isTransactional() throws NoSuchMethodException {
        Method method = MemberService.class.getMethod("createMember", CreateMemberRequest.class);

        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    private CreateMemberRequest validRequest() {
        CreateMemberRequest request = new CreateMemberRequest();
        request.setName("Jane Doe");
        request.setEmail("jane.doe@example.com");
        request.setTier(MembershipTier.GOLD);
        request.setStatus(MemberStatus.ACTIVE);
        return request;
    }

    private Member assignId(Member member, Long memberId) {
        member.setMemberId(memberId);
        return member;
    }
}
