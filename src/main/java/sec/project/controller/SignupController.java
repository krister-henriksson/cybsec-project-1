package sec.project.controller;

import static java.sql.DriverManager.println;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sec.project.domain.Account;
import sec.project.repository.AccountRepository;


@Controller
public class SignupController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PasswordEncoder encoder;
    
    

    @RequestMapping("/")
    public String defaultMapping() {
        return "index";
 
        // If user auth'ed, go to "thanks", else go to signup
        /*
        Account account = accountRepository.findByUsername(auth.getName());
        if (account == null)
            return "redirect:/form";
        else
            return "thanks";
        */
        /*
        if (auth == null)
            return "index";
        else
            return "redirect:/thanks";
        */        
    }

        
    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm() {
        return "form";
    }

    
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signup(Model model,
            @RequestParam String username,
            @RequestParam String address,
            @RequestParam String password
        ){
        Account account = new Account();
        
        account.setUsername(username);
        account.setAddress(address);
        account.setPassword(encoder.encode(password));
        accountRepository.save(account);
        accountRepository.flush();
        
        model.addAttribute("username", username);
        return "done";
    }

    
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String greet(
            @RequestParam String username,
            Model model
        ){
        Account account = accountRepository.findByUsername(username);
        if (account == null)
            return "error";
        else {
            model.addAttribute("address", account.getAddress());
            println(account.getAddress());
            return "order";
        }
    }

    
    @RequestMapping(value = "/updateaddress", method = RequestMethod.POST)
    public String updateAddress(@RequestParam String username,
                                @RequestParam String address){       
        //Account account = accountRepository.findByUsername(auth.getName());
        Account account = accountRepository.findByUsername(username);
        if (account == null) return "error";
        else {
            account.setAddress(address);
            accountRepository.flush();
            //return "redirect:/user?username=" + auth.getName();
            return "redirect:/user?username=" + username;
        }
    }
    
    @RequestMapping(value = "/updatepassword", method = RequestMethod.POST)
    public String updatePassword(@RequestParam String username,
                                @RequestParam String password){       
        //Account account = accountRepository.findByUsername(auth.getName());
        Account account = accountRepository.findByUsername(username);
        if (account == null) return "error";
        else {
            account.setPassword(encoder.encode(password));
            accountRepository.flush();
            //return "redirect:/user?username=" + auth.getName();
            return "redirect:/user?username=" + username;
        }
    }

    @RequestMapping(value = "/deleteme")
    public String deleteMe(@RequestParam String username){       
        //Account account = accountRepository.findByUsername(auth.getName());
        Account account = accountRepository.findByUsername(username);
        if (account == null) return "error";
        else {
            accountRepository.delete(account);
            accountRepository.flush();
            return "redirect:/logout";
        }
    }


    @RequestMapping(value="/thanks", method = RequestMethod.GET)
    public String thanksPage(Authentication auth){
        Account account = accountRepository.findByUsername(auth.getName());
        if (account == null)
            return "error";
        else if (auth.getName().equals("admin")){
            return "redirect:/showallusers";            
        } else {
            return "redirect:/user?username=" + auth.getName();
        }
    }
    
    @RequestMapping(value="/showallusers", method = RequestMethod.GET)
    public String showAllUsers(Model model){        
        List<Account> accountList = accountRepository.findAll();
        List<String> accountNames = new ArrayList<>();
        int size = accountList.size();
        for (int i=0; i<size; i++){
            accountNames.add( accountList.get(i).getUsername() );
        }
        model.addAttribute("list", accountNames);
        return "accounts";
    }

    @RequestMapping(value = "/deleteuser", method = RequestMethod.POST)
    public String deleteUser(@RequestParam String username){
        Account account = accountRepository.findByUsername(username);
        if (account == null) return "error";
        else {
            accountRepository.delete(account);
            accountRepository.flush();
            return "redirect:/showallusers";
        }
    }
    
    /*
    public String thanksPage(
            Authentication auth,
            Model model){
        
        Account account = accountRepository.findByUsername(auth.getName());
        if (account == null)
            return "error";
        else {
            model.addAttribute("address", account.getAddress());
            println(account.getAddress());
            return "order";
        }
    }
*/
    
    
    

/*
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage() {
        return "login";
    }
  
    private AuthenticationManager authenticationManager; // specific for Spring Security
        
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginVerify(
            @RequestParam String name,
            @RequestParam String password
        ){

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(name, password));
        
        if (authenticate.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authenticate);             
            return "thanks";
        }
        else {
            return "login";
        }
    }

    */
    
    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){    
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login";
    } 
    
}
