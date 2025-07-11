package com.banking.demo.Controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.banking.demo.Models.Account;
import com.banking.demo.Models.Transaction;
import com.banking.demo.Repo.AccountRepo;
import com.banking.demo.Service.AccountService;
import com.banking.demo.Service.MailService;
import com.banking.demo.Service.TransactionService;

@Controller
public class HomeController {

    @Autowired
    private AccountService aser;

    @Autowired
    private AccountRepo arepo;

    @Autowired
    private MailService mailser;

    @Autowired
    private TransactionService tser;

    // ====================== AUTH & LANDING ======================

    // Login page
    @GetMapping({"/login"})
    public String createAccount() {
        return "login";
    }

    // Home page
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // FAQs page
    @GetMapping("/faqs")
    public String faq() {
        return "faq";
    }

    // Customer care page
    @GetMapping("/customer-care")
    public String customerCare() {
        return "customer-care";
    }

    // Privacy policy page
    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    // ====================== ACCOUNT CREATION ======================

    // Open account page
    @GetMapping("/openAccount")
    public String openAccount() {
        return "openAccount";
    }

    // Save new account
    @PostMapping("/saveAccount")
    public String saveAccount(
        @RequestParam("username") String userName,
        @RequestParam("govtid") String govtId,
        @RequestParam("email") String email,
        @RequestParam("mobileNumber") String mobileNumber,
        @RequestParam("gender") String gender,
        @RequestParam("password") String password,
        Model model
    ) {
        try {
            long accno = aser.addAccount(userName, govtId, BigDecimal.ZERO, "USER", mobileNumber, email, gender, password);
            model.addAttribute("accno", accno);

            // Send welcome email
            String str = """
                Dear %s,

                Congratulations! Your Bank of Spring account has been successfully created.
                Your Account Number: %s
                You can now log in and start using our banking services.

                If you did not request this account, please contact us immediately at bankofspring@gmail.com.

                Welcome aboard!

                Regards,  
                Bank of Spring
                """.formatted(userName, accno);

            mailser.sendMail(email, "Account Created", str);
            return "account-success";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "openAccount";
        }
    }

    // ====================== FORGOT / RESET PASSWORD ======================

    // Forgot password page
    @GetMapping("/forgotpassword")
    public String forgotPassword() {
        return "forgotPassword";
    }

    // Handle forgot password form submission
    @PostMapping("/forgotmail")
    public String forgotMail(Model model, @RequestParam("email") String mail) {
        Account acc = arepo.findByEmail(mail);
        if (acc != null) {
            mailser.sendMail(mail, "Change Password", "Click the following link to reset your password:\n");
            model.addAttribute("message", "Reset link has been sent to " + mail);
        } else {
            model.addAttribute("message", "User with this mail does not exist");
        }
        return "forgotpassword";
    }

    // Reset password form
    @GetMapping("/resetpassword")
    public String resetPassword() {
        return "resetpassword";
    }

    // Handle password change
    @PostMapping("/changepassword")
    public String changePassword(
        @RequestParam("oldpassword") String oldPassword,
        @RequestParam("newpassword") String newPassword,
        @RequestParam("email") String email,
        Model model
    ) {
        Account acc = arepo.findByEmail(email);
        if (acc == null) {
            model.addAttribute("message", "No account exists with this email");
            return "resetpassword";
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(oldPassword, acc.getPassword())) {
            acc.setPassword(encoder.encode(newPassword));
            arepo.save(acc);

            // Send password change confirmation email
            String body = """
                Dear %s,

                This is to confirm that your Bank of Spring Internet Banking password was successfully changed on %s.

                If you made this change, no further action is needed.
                ⚠️ If not authorized, contact support immediately.

                Regards,  
                Bank of Spring
                """.formatted(acc.getUserName(),
                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

            mailser.sendMail(acc.getEmail(), "Password Changed", body);
            model.addAttribute("message", "Password Changed Successfully");
        } else {
            model.addAttribute("message", "Invalid details");
        }
        return "resetpassword";
    }

    // ====================== ACCOUNT HOME ======================

    // Redirect to user/admin home based on role
    @GetMapping("/accounthome")
    public String accountHome(Principal principal, Model model) {
        Account acc = arepo.findByEmail(principal.getName());
        if ("ADMIN".equalsIgnoreCase(acc.getRole())) {
            return "adminpage";
        }
        model.addAttribute("message", "Welcome, " + acc.getUserName());
        return "accounthome";
    }

    // View user account details
    @GetMapping("/viewDetails")
    public String viewDetail(Principal principal, Model model) {
        Account acc = arepo.findByEmail(principal.getName());
        model.addAttribute("name", acc.getUserName());
        model.addAttribute("accountNumber", acc.getAccountNumber());
        model.addAttribute("govtId", acc.getGovtIdNumber());
        model.addAttribute("mail", acc.getEmail());
        model.addAttribute("balance", acc.getBalance());
        model.addAttribute("mobile", acc.getMobileNumber());
        return "viewdetails";
    }

    // ====================== TRANSFER ======================

    // Show fetch transaction form
    @GetMapping("/fetchdetails")
    public String fetchTransactionDetails() {
        return "fetchdetails";
    }

    // Handle fetch account details to make transaction
    @GetMapping("/transact")
    public String transact(Principal principal, @RequestParam("accountnumber") String accno, Model model) {
        Account acc = arepo.findByAccountNumber(Long.parseLong(accno));
        Account self = arepo.findByEmail(principal.getName());
        if (acc == null) {
            model.addAttribute("error", "Account number not found");
            return "fetchdetails";
        } else if (acc == self) {
            model.addAttribute("error", "Self Account");
            return "fetchdetails";
        }
        model.addAttribute("accno", acc.getAccountNumber());
        model.addAttribute("name", acc.getUserName());
        return "transact";
    }

    // Perform transaction
    @PostMapping("/maketransact")
    public String transactComplete(Principal principal,
                                   @RequestParam("accountnumber") String accno,
                                   @RequestParam("amount") String amount,
                                   @RequestParam("purpose") String purpose,
                                   Model model) {
        Account sender = arepo.findByEmail(principal.getName());
        try {
            BigDecimal amt = new BigDecimal(amount);
            if (amt.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Amount must be positive");

            long receiverAccNo = Long.parseLong(accno);
            Account receiver = arepo.findByAccountNumber(receiverAccNo);

            if (receiver == null) {
                model.addAttribute("accno", accno);
                model.addAttribute("message", "Receiver account not found");
                return "transact";
            }

            tser.transact(sender.getAccountNumber(), receiverAccNo, amt, purpose);

            // Send emails
            String time = LocalDateTime.now().toString();
            mailser.sendMail(sender.getEmail(), "Transaction Alert",
                "Dear %s,\n\nYour transaction of ₹%s to %s is successful.\nPurpose: %s\nTime: %s\n\n- Bank Of Spring"
                    .formatted(sender.getUserName(), amount, receiver.getAccountNumber(), purpose, time));

            mailser.sendMail(receiver.getEmail(), "Payment Received",
                "Dear %s,\n\nYou received ₹%s from %s.\nPurpose: %s\nTime: %s\n\n- Bank Of Spring"
                    .formatted(receiver.getUserName(), amount, sender.getAccountNumber(), purpose, time));

            model.addAttribute("name", receiver.getUserName());
            model.addAttribute("accountNumber", receiver.getAccountNumber());
            model.addAttribute("amount", amount);
            model.addAttribute("purpose", purpose);
            return "successfultransact";

        } catch (Exception e) {
            model.addAttribute("accno", accno);
            model.addAttribute("message", e.getMessage());
            Account receiver = arepo.findByAccountNumber(Long.parseLong(accno));
            if (receiver != null)
                model.addAttribute("name", receiver.getUserName());
            return "transact";
        }
    }

    // ====================== DEPOSIT ======================

    @GetMapping("/depositfetchdetails")
    public String fetchDepositDetails() {
        return "depositfetchdetails";
    }

    @GetMapping("/deposit")
    public String deposit(@RequestParam("accountnumber") String accno, Model model) {
        Account acc = arepo.findByAccountNumber(Long.parseLong(accno));
        if (acc == null) {
            model.addAttribute("error", "No such account exists");
            return "depositfetchdetails";
        }
        model.addAttribute("accno", acc.getAccountNumber());
        model.addAttribute("name", acc.getUserName());
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam("accountnumber") String accno,
                          @RequestParam("amount") String amount,
                          Model model) {
        try {
            BigDecimal am = new BigDecimal(amount);
            Account ac = arepo.findByAccountNumber(Long.parseLong(accno));
            if (am.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Amount must be positive");

            tser.deposit(ac.getAccountNumber(), am);

            model.addAttribute("name", ac.getUserName());
            model.addAttribute("accountNumber", ac.getAccountNumber());
            model.addAttribute("amount", am);

            String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            String str = """
                Dear %s,

                We have received a deposit of ₹%s into your account %s on %s.

                Thank you for choosing Bank of Spring.

                Warm regards,  
                Bank of Spring
                """.formatted(ac.getUserName(), am.toPlainString(), ac.getAccountNumber(), formattedTime);

            mailser.sendMail(ac.getEmail(), "Deposit Alert", str);
            return "depositsuccessful";
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            return "deposit";
        }
    }

    // ====================== TRANSACTIONS ======================

    @GetMapping("/viewtransactions")
    public String displayTransactions(Principal principal, Model model) {
        Account acc = arepo.findByEmail(principal.getName());
        List<Transaction> l = tser.viewAllTransactions(acc);
        l.sort((a, b) -> b.getTime().compareTo(a.getTime()));
        model.addAttribute("transactions", l);
        return "viewtransactions";
    }

    // Admin: view transaction history of a user
    @GetMapping("/adminviewtransactions")
    public String viewTransactions(@RequestParam("accountNumber") String accountNumber, Model model) {
        Account acc = arepo.findByAccountNumber(Long.parseLong(accountNumber));
        if (acc == null) {
            model.addAttribute("error", "Account not exists");
            return "adminfetchdetails";
        } else {
            model.addAttribute("name", acc.getUserName());
            model.addAttribute("email", acc.getEmail());
            model.addAttribute("mobile", acc.getMobileNumber());
            List<Transaction> l = tser.viewAllTransactions(acc);
            l.sort((a, b) -> b.getTime().compareTo(a.getTime()));
            model.addAttribute("transactions", l);
            return "admintransactions";
        }
    }

    @GetMapping("/adminfetchTransactions")
    public String adminfetchDetails() {
        return "adminfetchdetails";
    }

    // ====================== WITHDRAW ======================

    @GetMapping("/withdrawfetchdetails")
    public String fetchWithdrawDetails() {
        return "/withdrawfetch";
    }

    @GetMapping("/withdraw")
    public String withdraw(@RequestParam("accountnumber") String accno, Model model) {
        Account acc = arepo.findByAccountNumber(Long.parseLong(accno));
        if (acc == null) {
            model.addAttribute("error", "No such account exists");
            return "depositfetchdetails";
        }
        model.addAttribute("accno", acc.getAccountNumber());
        model.addAttribute("name", acc.getUserName());
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("accountnumber") String accno,
                           @RequestParam("amount") String amount,
                           Model model) {
        Account ac = arepo.findByAccountNumber(Long.parseLong(accno));
        try {
            BigDecimal am = new BigDecimal(amount);
            if (am.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Amount must be positive");

            tser.withdraw(ac.getAccountNumber(), am);

            model.addAttribute("name", ac.getUserName());
            model.addAttribute("accountNumber", ac.getAccountNumber());
            model.addAttribute("amount", am);

            String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            String str = """
                Dear %s,

                An amount of ₹%s has been withdrawn from your account %s on %s.

                If this was not you, contact support immediately.

                Regards,  
                Bank of Spring
                """.formatted(ac.getUserName(), am.toPlainString(), ac.getAccountNumber(), formattedTime);

            mailser.sendMail(ac.getEmail(), "Withdraw Alert", str);
            return "withdrawsuccessful";
        } catch (Exception e) {
            model.addAttribute("name", ac.getUserName());
            model.addAttribute("accno", ac.getAccountNumber());
            model.addAttribute("message", e.getMessage());
            return "withdraw";
        }
    }
}
