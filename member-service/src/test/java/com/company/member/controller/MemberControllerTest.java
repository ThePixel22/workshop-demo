package com.company.member.controller;

import com.company.member.dto.CreateMemberRequest;
import com.company.member.dto.CreateMemberResponse;
import com.company.member.entity.MemberStatus;
import com.company.member.entity.MembershipTier;
import com.company.member.exception.DuplicateEmailException;
import com.company.member.exception.MemberCreationException;
import com.company.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    void createMember_withValidRequest_returnsCreatedMemberId() throws Exception {
        CreateMemberRequest request = validRequest();
        given(memberService.createMember(any(CreateMemberRequest.class)))
                .willReturn(new CreateMemberResponse(101L));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(101L));

        verify(memberService).createMember(any(CreateMemberRequest.class));
    }

    @ParameterizedTest
    @EnumSource(MembershipTier.class)
    void createMember_withSupportedTier_returnsCreated(MembershipTier tier) throws Exception {
        CreateMemberRequest request = validRequest();
        request.setTier(tier);
        given(memberService.createMember(any(CreateMemberRequest.class)))
                .willReturn(new CreateMemberResponse(101L));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(memberService).createMember(any(CreateMemberRequest.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMandatoryFields")
    void createMember_withMissingMandatoryField_returnsBadRequest(
            String scenario, String requestBody, String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(expectedMessage)));

        verify(memberService, never()).createMember(any(CreateMemberRequest.class));
    }

    @Test
    void createMember_withInvalidEmail_returnsBadRequest() throws Exception {
        CreateMemberRequest request = validRequest();
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Email must be in a valid email format")));

        verify(memberService, never()).createMember(any(CreateMemberRequest.class));
    }

    @Test
    void createMember_withUnsupportedTier_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "name": "Jane Doe",
                  "email": "jane.doe@example.com",
                  "tier": "DIAMOND",
                  "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).createMember(any(CreateMemberRequest.class));
    }

    @Test
    void createMember_withDuplicateEmail_returnsConflict() throws Exception {
        CreateMemberRequest request = validRequest();
        given(memberService.createMember(any(CreateMemberRequest.class)))
                .willThrow(new DuplicateEmailException("Email already exists: jane.doe@example.com"));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Email"))
                .andExpect(jsonPath("$.message").value("Email already exists: jane.doe@example.com"));
    }

    @Test
    void createMember_whenCreationFails_returnsInternalServerError() throws Exception {
        CreateMemberRequest request = validRequest();
        given(memberService.createMember(any(CreateMemberRequest.class)))
                .willThrow(new MemberCreationException("Failed to create member"));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Member Creation Failed"))
                .andExpect(jsonPath("$.message").value("Failed to create member"));
    }

    private static Stream<Arguments> invalidMandatoryFields() {
        return Stream.of(
                Arguments.of("name is mandatory", """
                        {"name":"", "email":"jane.doe@example.com", "tier":"GOLD", "status":"ACTIVE"}
                        """, "Member name is mandatory"),
                Arguments.of("email is mandatory", """
                        {"name":"Jane Doe", "email":"", "tier":"GOLD", "status":"ACTIVE"}
                        """, "Member email is mandatory"),
                Arguments.of("tier is mandatory", """
                        {"name":"Jane Doe", "email":"jane.doe@example.com", "status":"ACTIVE"}
                        """, "Membership tier is mandatory"),
                Arguments.of("status is mandatory", """
                        {"name":"Jane Doe", "email":"jane.doe@example.com", "tier":"GOLD"}
                        """, "Member status is mandatory")
        );
    }

    private CreateMemberRequest validRequest() {
        CreateMemberRequest request = new CreateMemberRequest();
        request.setName("Jane Doe");
        request.setEmail("jane.doe@example.com");
        request.setTier(MembershipTier.GOLD);
        request.setStatus(MemberStatus.ACTIVE);
        return request;
    }
}
