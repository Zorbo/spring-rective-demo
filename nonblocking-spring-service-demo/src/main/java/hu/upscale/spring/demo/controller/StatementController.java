package hu.upscale.spring.demo.controller;

import hu.upscale.spring.demo.client.PostAccountStatementRequest;
import hu.upscale.spring.demo.client.PostAccountStatementResponse;
import hu.upscale.spring.demo.service.AccountStatementGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author László Zoltán
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class StatementController {

    private final AccountStatementGeneratorService accountStatementGeneratorService;

    @PostMapping(path = "/accounts/statements")
    public Mono<PostAccountStatementResponse> createAccountStatement(@RequestBody PostAccountStatementRequest postAccountStatementRequest) {
        return accountStatementGeneratorService.generateAccountStatement(postAccountStatementRequest.getLastTransactionId())
                .map(accountStatementId -> PostAccountStatementResponse.builder().accountStatementId(accountStatementId).build());
    }

}
