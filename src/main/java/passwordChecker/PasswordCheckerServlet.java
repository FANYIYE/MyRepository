package passwordChecker;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/passwordChecker")
public class PasswordCheckerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 预设规则的正则表达式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*].*");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String password = request.getParameter("password");

        int score = 0;
        List<String> satisfiedRules = new ArrayList<>();
        List<String> unsatisfiedRules = new ArrayList<>();

        if (password.length() >= 8) {
            score++;
            satisfiedRules.add("长度");
        } else {
            unsatisfiedRules.add("长度");
        }

        if (UPPERCASE_PATTERN.matcher(password).matches()) {
            score++;
            satisfiedRules.add("大写字母");
        } else {
            unsatisfiedRules.add("大写字母");
        }

        if (LOWERCASE_PATTERN.matcher(password).matches()) {
            score++;
            satisfiedRules.add("小写字母");
        } else {
            unsatisfiedRules.add("小写字母");
        }

        if (DIGIT_PATTERN.matcher(password).matches()) {
            score++;
            satisfiedRules.add("数字");
        } else {
            unsatisfiedRules.add("数字");
        }

        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            score++;
            satisfiedRules.add("特殊字符");
        } else {
            unsatisfiedRules.add("特殊字符");
        }

        String strength = "";
        if (score <= 1) {
            strength = "弱";
        } else if (score <= 3) {
            strength = "中等";
        } else {
            strength = "强";
        }

        String result = String.format("密码强度：%s (%d/5)\n满足：%s\n未满足：%s",
                strength,
                score,
                String.join("、", satisfiedRules),
                String.join("、", unsatisfiedRules));

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(result);
        out.flush();
    }
}
