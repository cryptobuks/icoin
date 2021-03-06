package com.icoin.trading.webui.user.form;

import com.homhon.util.Strings;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

/**
 * Created with IntelliJ IDEA.
 * User: jihual
 * Date: 2/10/14
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChangeWithdrawPasswordForm {
    @NotEmpty
    @Size(min = 6, max = 16, message = "Previous withdraw password length error")
    private String previousWithdrawPassword;
    @NotEmpty
    @Size(min = 6, max = 16, message = "Withdraw password length must be between 6 and 16")
    private String withdrawPassword;
    @NotEmpty
    @Size(min = 6, max = 16, message = "Confirmed new password length must be between 6 and 16")
    private String confirmedWithdrawPassword;

    public String getPreviousWithdrawPassword() {
        return previousWithdrawPassword;
    }

    public void setPreviousWithdrawPassword(String previousWithdrawPassword) {
        this.previousWithdrawPassword = previousWithdrawPassword;
    }

    public String getWithdrawPassword() {
        return withdrawPassword;
    }

    public void setWithdrawPassword(String withdrawPassword) {
        this.withdrawPassword = withdrawPassword;
    }

    public String getConfirmedWithdrawPassword() {
        return confirmedWithdrawPassword;
    }

    public void setConfirmedWithdrawPassword(String confirmedWithdrawPassword) {
        this.confirmedWithdrawPassword = confirmedWithdrawPassword;
    }

    public boolean isValid() {
        return Strings.hasText(withdrawPassword) && Strings.hasText(previousWithdrawPassword) &&
                !previousWithdrawPassword.equals(withdrawPassword)
                && withdrawPassword.equals(confirmedWithdrawPassword);
    }
}