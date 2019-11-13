package sop.ewallet.account.controller;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import sop.ewallet.account.model.Account;
import sop.ewallet.account.model.UserRequest;
import sop.ewallet.account.model.Wallet;
import sop.ewallet.account.repositories.AccountRepositories;
import sop.ewallet.account.repositories.ResourceNotFoundException;

import java.util.Optional;

@RestController
@RequestMapping("/account")
public class AccountController {
//    Repositories
    @Autowired
    private AccountRepositories accountRepositories;

    private  RestTemplate restTemplate = new RestTemplate();
//    public AccountController(RestTemplateBuilder restTemplateBuilder) {
//        this.restTemplate = restTemplateBuilder.build();
//    }

    private UserRequest sentRequest(UserRequest ur, String action){
        ur.getAccount_source().setWallet(accountRepositories.getOne(ur.getAccount_source().getId()).getWallet());
        System.out.println(ur.getAccount_source().getId());
        System.out.println(ur.getAccount_source().getWallet());
        String url;
        switch (ur.getAction()){
            case "deposit":
                url = "http://localhost:9634/service/transaction/deposit";
                break;
            case "withdraw":
                url = "http://localhost:9634/service/transaction/withdraw";
                break;
            case "transfer":
                ur.getAccount_destination().setWallet(accountRepositories.getOne(ur.getAccount_destination().getId()).getWallet());
                url = "http://localhost:9634/service/transaction/transfer";
                break;
            default:
                url = "http://localhost:9634/service/transaction";
        }

        UserRequest response = this.restTemplate.postForObject(url, ur,UserRequest.class);
//        System.out.println(response);
        return response;
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String hello() {
        return "Welcome to You Wallet";
    }

    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public Optional<Account> info(@PathVariable int id){
        return accountRepositories.findById(id);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Account createDefault(){
        return accountRepositories.save(new Account());
    }
//
    @PostMapping(value = "/deposit")
    public Account deposit(@RequestBody UserRequest ur){
//        RequestAction re = new RequestAction("DP", ur.getBalance(), ur.getCurrency_origin(), accountRepositories.getOne((long) ur.getId()));
        UserRequest nur = sentRequest(ur, "deposit");
        induct(nur.getAccount_source().getWallet(), ur.getCurrency_destination(), ur.getAccount_source().getId());
        return nur.getAccount_source();
    }
//
    @PostMapping(value = "/withdraw")
    public Account withdraw(@RequestBody UserRequest ur) {
        UserRequest nur = sentRequest(ur, "withdraw");
        deduct(nur.getAccount_source().getWallet(), ur.getCurrency_source(), ur.getAccount_source().getId());
        return nur.getAccount_source();
    }
//
    @PostMapping (value = "/transfer")
    public Account[] transfer(@RequestBody UserRequest ur){
        UserRequest nur = sentRequest(ur,"transfer");
        deduct(nur.getAccount_source().getWallet(), nur.getCurrency_source(), nur.getAccount_source().getId());
        induct(nur.getAccount_destination().getWallet(), nur.getCurrency_destination(), nur.getAccount_destination().getId());
        return new Account[]{nur.getAccount_source(), nur.getAccount_destination()};
    }
//
////    @GetMapping(value = "/query/{id}")
////    public double test(@PathVariable int id){
////        return accountRepositories.cruMoney(id);
////    }
//
    private void induct(Wallet wd, String currency, int id){
        accountRepositories.findById(id)
                .map(account -> {
                    switch (currency.toLowerCase()){
                        case "usd":
                            account.getWallet().setUSD(wd.getUSD());
                            break;
                        case "cny":
                            account.getWallet().setCNY(wd.getCNY());
                            break;
                        case "thb":
                            account.getWallet().setTHB(wd.getTHB());
                            break;
                        case "eur":
                            account.getWallet().setEUR(wd.getEUR());
                            break;
                        case "jpy":
                            account.getWallet().setJPY(wd.getJPY());
                            break;
                        case "sgd":
                            account.getWallet().setSGD(wd.getSGD());
                            break;
                        default:
                            throw new ResourceNotFoundException("Wrong currency");
                    }
                    return accountRepositories.save(account);
                }).orElseThrow(() -> new ResourceNotFoundException("Account not found with id " + id));
    }

    private void deduct(Wallet wd, String currency, int id) {
        accountRepositories.findById(id)
                .map(account -> {
                    switch (currency.toLowerCase()){
                        case "usd":
                            account.getWallet().setUSD(wd.getUSD());
                            break;
                        case "cny":
                            account.getWallet().setCNY(wd.getCNY());
                            break;
                        case "thb":
                            account.getWallet().setTHB(wd.getTHB());
                            break;
                        case "eur":
                            account.getWallet().setEUR(wd.getEUR());
                            break;
                        case "jpy":
                            account.getWallet().setJPY(wd.getJPY());
                            break;
                        case "sgd":
                            account.getWallet().setSGD(wd.getSGD());
                            break;
                        default:
                            throw new ResourceNotFoundException("Wrong currency");
                    }
                    return accountRepositories.save(account);
                }).orElseThrow(() -> new ResourceNotFoundException("Account not found with id " + id));
    }

}

