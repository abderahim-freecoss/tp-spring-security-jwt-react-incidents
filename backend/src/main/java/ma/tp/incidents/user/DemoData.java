package ma.tp.incidents.user;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration @Profile("demo")
public class DemoData {
 @Bean CommandLineRunner demoUsers(UserRepository users,PasswordEncoder passwords){return args->{seed(users,passwords,"admin@example.com","Administrateur",Role.ADMIN);seed(users,passwords,"tech@example.com","Technicien",Role.TECHNICIAN);seed(users,passwords,"employee@example.com","Employé",Role.EMPLOYEE);seed(users,passwords,"employee2@example.com","Employé 2",Role.EMPLOYEE);};}
 private void seed(UserRepository users,PasswordEncoder passwords,String email,String name,Role role){if(!users.existsByEmail(email)){User u=new User(name,email,passwords.encode("DemoPassword123!"));u.setRole(role);users.save(u);}}
}