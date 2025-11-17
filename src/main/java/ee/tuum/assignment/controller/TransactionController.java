package ee.tuum.assignment.controller;

import ee.tuum.assignment.dto.request.TransactionRequest;
import ee.tuum.assignment.dto.response.TransactionResponse;
import ee.tuum.assignment.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {
	private final TransactionService transactionService;

	@PostMapping
	public TransactionResponse createTransaction(@RequestBody @Valid TransactionRequest request) {
		return transactionService.createTransaction(request);
	}

	@GetMapping("/{id}")
	public TransactionResponse getTransactionById(@PathVariable Long id) throws Exception {
		return transactionService.getById(id);
	}
}
