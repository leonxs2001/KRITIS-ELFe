package de.thb.kritis_elfe.security.passwordValidation;

import de.thb.kritis_elfe.controller.form.UserRegisterFormModel;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, UserRegisterFormModel> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserRegisterFormModel userRegisterFormModel, ConstraintValidatorContext context) {
        boolean isValid = userRegisterFormModel.getPassword().equals(userRegisterFormModel.getConfirmPassword());;
        if(!isValid){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode( "confirmPassword" ).addConstraintViolation();
        }
        return isValid;
    }
}
