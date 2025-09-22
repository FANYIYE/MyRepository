document.addEventListener('DOMContentLoaded', () => {
    const passwordInput = document.getElementById('password');
    const strengthBar = document.getElementById('strength-bar');
    const strengthText = document.getElementById('strength-text');
    const rules = {
        length: document.getElementById('rule-length'),
        uppercase: document.getElementById('rule-uppercase'),
        lowercase: document.getElementById('rule-lowercase'),
        number: document.getElementById('rule-number'),
        special: document.getElementById('rule-special')
    };

    passwordInput.addEventListener('input', () => {
        const password = passwordInput.value;
        let score = 0;

        // 规则验证
        const hasLength = password.length >= 8;
        const hasUppercase = /[A-Z]/.test(password);
        const hasLowercase = /[a-z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        const hasSpecial = /[!@#$%^&*]/.test(password);

        // 更新分数和规则列表样式
        if (hasLength) score++;
        updateRule(rules.length, hasLength);

        if (hasUppercase) score++;
        updateRule(rules.uppercase, hasUppercase);

        if (hasLowercase) score++;
        updateRule(rules.lowercase, hasLowercase);

        if (hasNumber) score++;
        updateRule(rules.number, hasNumber);

        if (hasSpecial) score++;
        updateRule(rules.special, hasSpecial);

        // 更新强度进度条和文本
        updateStrength(score);
    });

    function updateRule(element, isSatisfied) {
        if (isSatisfied) {
            element.classList.remove('unsatisfied');
            element.classList.add('satisfied');
        } else {
            element.classList.remove('satisfied');
            element.classList.add('unsatisfied');
        }
    }

    function updateStrength(score) {
        let strength = '弱';
        let barClass = 'weak';
        let barWidth = (score / 5) * 100;

        if (score === 0 && passwordInput.value === '') {
            strength = '弱';
            barClass = '';
            barWidth = 0;
        } else if (score <= 2) {
            strength = '弱';
            barClass = 'weak';
        } else if (score <= 4) {
            strength = '中等';
            barClass = 'medium';
        } else {
            strength = '强';
            barClass = 'strong';
        }

        strengthText.textContent = strength;
        strengthBar.style.width = `${barWidth}%`;
        strengthBar.className = `strength-bar ${barClass}`;
    }
});