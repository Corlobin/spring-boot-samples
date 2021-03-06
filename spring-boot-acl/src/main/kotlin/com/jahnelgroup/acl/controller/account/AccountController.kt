package com.jahnelgroup.acl.controller.account

import com.jahnelgroup.acl.domain.account.Account
import com.jahnelgroup.acl.domain.account.AccountRepo
import com.jahnelgroup.acl.domain.user.UserRepo
import com.jahnelgroup.acl.service.account.AccountService
import com.jahnelgroup.acl.service.context.UserContextService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class AccountController(
        var accountService: AccountService,
        var userContextService: UserContextService,
        var accountRepo: AccountRepo,
        var userRepo: UserRepo
) {

    @ModelAttribute fun useRepo() = userRepo
    @ModelAttribute fun accountRepo() = accountRepo
    @ModelAttribute fun accountService() = accountService
    @ModelAttribute fun userContextService() = userContextService

    @GetMapping("/")
    fun home(model: Model): String{
        return "home"
    }

    @GetMapping("/createAccount")
    fun createAccount(model: Model): String{
        model.addAttribute("createAccountForm", CreateAccountForm())
        model.addAttribute("content", "createAccount")
        return "home"
    }

    @PostMapping("/createAccount")
    fun post(model: Model, @ModelAttribute(value="createAccountForm") createAccountForm: CreateAccountForm): String{
        var account = Account().apply {
            if( createAccountForm.accountId != null )
                id = createAccountForm.accountId
            name = createAccountForm.name
            primaryOwner = userRepo.findByUsername(createAccountForm.primaryOwner!!).get()
            jointOwners = createAccountForm.jointOwners.asSequence().map { userRepo.findByUsername(it).get() }.toMutableSet()
            readOnly = createAccountForm.readOnly.asSequence().map { userRepo.findByUsername(it).get() }.toMutableSet()
        }

        userContextService.impersonateUser(account.primaryOwner!!) // pretend to be the account owner.
        accountService.save(account)

        model.addAttribute("accountService", accountService)
        return "home"
    }

    @GetMapping("/accounts/{account}/admin")
    @PreAuthorize("hasPermission(#account, 'administration')")
    fun admin(@ModelAttribute @PathVariable account: Account) = "account"

    @GetMapping("/accounts/{account}/write")
    @PreAuthorize("hasPermission(#account, 'write')")
    fun write(@ModelAttribute @PathVariable account: Account) = "account"

    @GetMapping("/accounts/{account}/read")
    @PreAuthorize("hasPermission(#account, 'read')")
    fun read(@ModelAttribute @PathVariable account: Account) = "account"

}