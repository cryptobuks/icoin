package com.icoin.trading.webui.user.facade;

import com.icoin.trading.tradeengine.query.portfolio.PortfolioEntry;
import com.icoin.trading.users.domain.model.user.UserAccount;
import com.icoin.trading.users.query.UserEntry;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: jihual
 * Date: 12/17/13
 * Time: 6:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserServiceFacade {
    UserAccount currentUser();

    UserEntry currentDetailUser();

    boolean isWithdrawPasswordSet();

    PortfolioEntry obtainPortfolioForUser();

    UserEntry findByEmail(String email);

    int findPasswordResetCount(String username, String ip, Date currentDate);

    void resetPasswordWithToken(String token,
                                String password,
                                String confirmedPassword,
                                String operatingIp,
                                Date resetTime);

    boolean generateForgetPasswordToken(String email,
                                       String operatingIp,
                                       Date currentTime);

    void changePassword(String previousPassword,
                        String newPassword,
                        String confirmedNewPassword,
                        String operatingIp,
                        Date changedTime);

    boolean createWithdrawPassword(String withdrawPassword,
                                String confirmedWithdrawPassword,
                                String operatingIp,
                                Date changedTime);

    void changeWithdrawPassword(String previousPassword,
                                String withdrawPassword,
                                String confirmedWithdrawPassword,
                                String operatingIp,
                                Date changedTime);
}