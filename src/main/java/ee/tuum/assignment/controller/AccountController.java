package ee.tuum.assignment.controller;

import ee.tuum.assignment.dto.request.AccountRequest;
import ee.tuum.assignment.dto.response.AccountResponse;
import ee.tuum.assignment.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
	private final AccountService accountService;

	@PostMapping
	public AccountResponse createAccount(@RequestBody @Valid AccountRequest request) {
		return accountService.createAccount(request);
	}

	@GetMapping("/{id}")
	public AccountResponse getAccountById(@PathVariable Long id){
		return accountService.getAccountById(id);
	}
}
