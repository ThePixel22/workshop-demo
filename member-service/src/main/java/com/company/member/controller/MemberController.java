package com.company.member.controller;

import com.company.member.dto.*;
import com.company.member.elasticsearch.document.MemberDocument;
import com.company.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<CreateMemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        CreateMemberResponse response = memberService.createMember(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MemberDocument>> getAll() {
        List<MemberDocument> memberList = memberService.getAll();
        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MemberDocument>> search(@RequestParam String textSearch) {
        Page<MemberDocument> memberList = memberService.search(textSearch);
        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    @GetMapping("/searchByName")
    public ResponseEntity<List<MemberDocument>> searchByName(@RequestParam String searchName) {
        List<MemberDocument> memberList = memberService.searchByName(searchName);
        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long memberId) {
        MemberResponse response = memberService.getMemberById(memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{memberId}/free-night-balance")
    public ResponseEntity<FreeNightBalanceResponse> getFreeNightBalance(@PathVariable Long memberId) {
        FreeNightBalanceResponse response = memberService.getFreeNightBalance(memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{memberId}/balance")
    public ResponseEntity<UpdateBalanceResponse> updateBalance(
            @PathVariable Long memberId,
            @RequestBody UpdateBalanceRequest request) {
        UpdateBalanceResponse response = memberService.updateBalance(memberId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{memberId}/redemptions")
    public ResponseEntity<MemberRedemptionsResponse> getMemberRedemptions(@PathVariable Long memberId) {
        MemberRedemptionsResponse response = memberService.getMemberRedemptions(memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
