// Function to generate a CAPTCHA
function generateCaptcha() {
    const captcha = Math.random().toString(36).substring(2, 8).toUpperCase();
    document.getElementById("capt").value = captcha;
}

// Function to refresh CAPTCHA
function refreshCaptcha() {
    generateCaptcha();
}

// Function to open the Terms and Conditions modal
function openTermsModal() {
    const modal = document.getElementById("termsModal");
    modal.style.display = "block";
}

// Function to validate CAPTCHA
function validateCaptcha() {
    const captchaInput = document.getElementById("captcha-input").value;
    const captchaValue = document.getElementById("capt").value;
    if (captchaInput !== captchaValue) {
        showPopup("Invalid CAPTCHA entered.");
        return false;
    }
    return true;
}

// Function to handle form submission
function handleSubmit(event) {
    event.preventDefault(); // Prevent default form submission

    // Validate all fields before submitting
    const emailValid = validateEmail();
    const passwordValid = validatePassword();
    const contactNoValid = validateContactNo();
    
    // Validate CAPTCHA
    const captchaValid = validateCaptcha();

    // Check if all validations passed
    if (!emailValid || !passwordValid || !contactNoValid || !captchaValid) {
        showPopup("Invalid CAPTCHA entered.");
        return; // Prevent form submission if any validation fails
    }

    // If all validations are valid, proceed with form submission
    document.querySelector("form").submit();
}

// Function to show a popup message for error or confirmation
function showPopup(message) {
    // Create a new div element to display the message
    const popup = document.createElement('div');
    popup.classList.add('popup-message');
    
    // Style the popup (you can customize this as needed)
    popup.style.position = 'fixed';
    popup.style.top = '20px';
    popup.style.right = '20px';
    popup.style.backgroundColor = '#ffdddd'; // Light red background for error
    popup.style.border = '1px solid red';
    popup.style.padding = '10px';
    popup.style.zIndex = '1000';
    popup.style.color = 'red';
    popup.style.fontWeight = 'bold';
    popup.innerText = message;

    // Append the popup to the body
    document.body.appendChild(popup);

    // Remove the popup after 5 seconds
    setTimeout(() => {
        popup.remove();
    }, 5000); // 5 seconds
}

// Email Validation Function
function validateEmail() {
    const email = document.getElementById("email").value;
    const emailError = document.getElementById("emailError");
    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;

    if (!emailPattern.test(email)) {
        emailError.innerText = "Please enter a valid email address.";
        return false; // Return false if invalid
    } else {
        emailError.innerText = ""; // Clear error message if valid
        return true; // Return true if valid
    }
}

// Password Validation Function
function validatePassword() {
    const password = document.getElementById("password").value;
    const passwordError = document.getElementById("passwordError");
    const minLength = 8;
    const passwordPattern = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$/;

    if (password.length < minLength) {
        passwordError.innerText = `Password must be at least ${minLength} characters long.`;
        return false; // Return false if invalid
    } else if (!passwordPattern.test(password)) {
        passwordError.innerText = "Password must include a number, uppercase letter, and special character.";
        return false; // Return false if invalid
    } else {
        passwordError.innerText = ""; // Clear error message if valid
        return true; // Return true if valid
    }
}

// Contact Number Validation Function
function validateContactNo() {
    const contactNo = document.getElementById("contactNo").value;
    const contactNoError = document.getElementById("contactNoError");
    const contactNoPattern = /^\d{10}$/; // Assuming a valid contact number is 10 digits

    if (!contactNoPattern.test(contactNo)) {
        contactNoError.innerText = "Please enter a valid 10-digit mobile number.";
        return false; // Return false if invalid
    } else {
        contactNoError.innerText = ""; // Clear error message if valid
        return true; // Return true if valid
    }
}

// Event listener for document load to initialize CAPTCHA and session messages
document.addEventListener("DOMContentLoaded", function () {
    generateCaptcha();

    // Debugging session message
    console.log("Session Message: ", sessionMessage);
    if (sessionMessage) {
        showPopup(sessionMessage);
    }

    // Modal close button functionality
    const modal = document.getElementById("termsModal");
    const closeBtn = document.querySelector(".close");

    closeBtn.onclick = function() {
        modal.style.display = "none";
    };

    // Agree button functionality
    const agreeButton = document.getElementById("agreeButton");
    agreeButton.onclick = function() {
        modal.style.display = "none";
        document.getElementById("checkbox").checked = true;
    };

    // Attach the handleSubmit function to the form
    const form = document.querySelector("form");
    form.addEventListener("submit", handleSubmit);
});

// Close modal when clicking outside of it
window.onclick = function(event) {
    const modal = document.getElementById("termsModal");
    if (event.target === modal) {
        modal.style.display = "none";
    }
};
