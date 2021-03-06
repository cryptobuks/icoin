package com.icoin.trading.fee.application.command;

import com.icoin.trading.api.fee.command.paid.CancelPaidFeeCommand;
import com.icoin.trading.api.fee.command.paid.ConfirmPaidFeeCommand;
import com.icoin.trading.api.fee.command.paid.CreatePaidFeeCommand;
import com.icoin.trading.api.fee.command.paid.OffsetPaidFeeCommand;
import com.icoin.trading.api.fee.command.received.CancelReceivedFeeCommand;
import com.icoin.trading.api.fee.command.received.ConfirmReceivedFeeCommand;
import com.icoin.trading.api.fee.command.received.CreateReceivedFeeCommand;
import com.icoin.trading.api.fee.command.received.OffsetReceivedFeeCommand;
import com.icoin.trading.api.fee.domain.FeeTransactionId;
import com.icoin.trading.api.fee.domain.PaidMode;
import com.icoin.trading.api.fee.domain.fee.BusinessType;
import com.icoin.trading.api.fee.domain.fee.CancelledReason;
import com.icoin.trading.api.fee.domain.fee.FeeId;
import com.icoin.trading.api.fee.domain.fee.FeeStatus;
import com.icoin.trading.api.fee.domain.fee.FeeType;
import com.icoin.trading.api.fee.domain.offset.OffsetId;
import com.icoin.trading.api.fee.domain.received.ReceivedSource;
import com.icoin.trading.api.fee.domain.received.ReceivedSourceType;
import com.icoin.trading.api.fee.events.fee.paid.PaidFeeCancelledEvent;
import com.icoin.trading.api.fee.events.fee.paid.PaidFeeConfirmedEvent;
import com.icoin.trading.api.fee.events.fee.paid.PaidFeeCreatedEvent;
import com.icoin.trading.api.fee.events.fee.paid.PaidFeeOffsetedEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeCancelledEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeConfirmedEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeCreatedEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeOffsetedEvent;
import com.icoin.trading.api.tradeengine.domain.PortfolioId;
import com.icoin.trading.api.tradeengine.domain.TransactionId;
import com.icoin.trading.api.users.domain.UserId;
import com.icoin.trading.fee.domain.paid.PaidFee;
import com.icoin.trading.fee.domain.received.ReceivedFee;
import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: liougehooa
 * Date: 14-3-19
 * Time: PM9:03
 * To change this template use File | Settings | File Templates.
 */
public class ReceivedPaidFeeCommandHandlerTest {
    private final FeeTransactionId feeTransactionId = new FeeTransactionId();
    private final TransactionId orderTransactionId = new TransactionId();
    private final PortfolioId portfolioId = new PortfolioId();
    private final UserId userId = new UserId();
    private final OffsetId offsetId = new OffsetId();
    private final Date tradeTime = new Date();
    private final Date dueDate = new Date();
    private final BigMoney sellCommissionAmount = BigMoney.of(CurrencyUnit.of("BTC"), 10);
    private final FeeId receivedFeeId = new FeeId();
    private final FeeId paidFeeId = new FeeId();
    private final ReceivedSource receivedSource = new ReceivedSource(ReceivedSourceType.INTERNAL_ACCOUNT, orderTransactionId.toString());
    private FixtureConfiguration receivedFixture;
    private FixtureConfiguration paidFixture;
    private ReceivedPaidFeeCommandHandler commandHandler;

    @Before
    public void setUp() throws Exception {
        receivedFixture = Fixtures.newGivenWhenThenFixture(ReceivedFee.class);
        paidFixture = Fixtures.newGivenWhenThenFixture(PaidFee.class);

        commandHandler = new ReceivedPaidFeeCommandHandler();

        receivedFixture.registerAnnotatedCommandHandler(commandHandler);
        paidFixture.registerAnnotatedCommandHandler(commandHandler);

        commandHandler.setReceivedFeeRepository(receivedFixture.getRepository());
        commandHandler.setPaidFeeRepository(paidFixture.getRepository());
    }

    @Test
    public void testHandleCreateReceived() throws Exception {
        CreateReceivedFeeCommand command = new CreateReceivedFeeCommand(
                feeTransactionId,
                receivedFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.RECEIVE_COIN,
                BusinessType.TRADE_EXECUTED,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                orderTransactionId.toString(),
                receivedSource);

        receivedFixture.given()
                .when(command)
                .expectEvents(
                        new ReceivedFeeCreatedEvent(
                                receivedFeeId,
                                FeeStatus.PENDING,
                                sellCommissionAmount,
                                FeeType.RECEIVE_COIN,
                                tradeTime,
                                dueDate,
                                portfolioId.toString(),
                                userId.toString(),
                                BusinessType.TRADE_EXECUTED,
                                orderTransactionId.toString(),
                                receivedSource));
    }

    @Test
    public void testHandleConfirmReceived() throws Exception {
        ConfirmReceivedFeeCommand command = new ConfirmReceivedFeeCommand(receivedFeeId, sellCommissionAmount, tradeTime);

        receivedFixture.given(new ReceivedFeeCreatedEvent(
                receivedFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.RECEIVE_MONEY,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                BusinessType.TRADE_EXECUTED,
                orderTransactionId.toString(),
                receivedSource))
                .when(command)
                .expectEvents(new ReceivedFeeConfirmedEvent(receivedFeeId, sellCommissionAmount, tradeTime));
    }

    @Test
    public void testHandleOffsetReceived() throws Exception {
        OffsetReceivedFeeCommand command = new OffsetReceivedFeeCommand(receivedFeeId, offsetId, tradeTime);

        receivedFixture.given(new ReceivedFeeCreatedEvent(
                receivedFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.RECEIVE_MONEY,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                BusinessType.TRADE_EXECUTED,
                orderTransactionId.toString(),
                receivedSource))
                .when(command)
                .expectEvents(new ReceivedFeeOffsetedEvent(receivedFeeId, offsetId, tradeTime));
    }

    @Test
    public void testHandleCancelReceived() throws Exception {
        CancelReceivedFeeCommand command =
                new CancelReceivedFeeCommand(receivedFeeId, CancelledReason.DUPLICATED, tradeTime);

        receivedFixture.given(new ReceivedFeeCreatedEvent(
                receivedFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.RECEIVE_MONEY,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                BusinessType.TRADE_EXECUTED,
                orderTransactionId.toString(),
                receivedSource))
                .when(command)
                .expectEvents(new ReceivedFeeCancelledEvent(receivedFeeId, CancelledReason.DUPLICATED, tradeTime));
    }

    @Test
    public void testHandleCreatePaid() throws Exception {
        CreatePaidFeeCommand command = new CreatePaidFeeCommand(
                feeTransactionId,
                paidFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.PAY_COIN,
                BusinessType.TRADE_EXECUTED,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                orderTransactionId.toString(),
                PaidMode.INTERNAL);

        paidFixture.given()
                .when(command)
                .expectEvents(
                        new PaidFeeCreatedEvent(
                                paidFeeId,
                                FeeStatus.PENDING,
                                sellCommissionAmount,
                                FeeType.PAY_COIN,
                                tradeTime,
                                dueDate,
                                portfolioId.toString(),
                                userId.toString(),
                                BusinessType.TRADE_EXECUTED,
                                orderTransactionId.toString(),
                                PaidMode.INTERNAL));
    }

    @Test
    public void testHandleConfirmPaid() throws Exception {
        ConfirmPaidFeeCommand command = new ConfirmPaidFeeCommand(paidFeeId, "1223123sequenceNumber", tradeTime);

        paidFixture.given(new PaidFeeCreatedEvent(
                paidFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.PAY_COIN,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                BusinessType.TRADE_EXECUTED,
                orderTransactionId.toString(),
                PaidMode.INTERNAL))
                .when(command)
                .expectEvents(new PaidFeeConfirmedEvent(paidFeeId, "1223123sequenceNumber", tradeTime));
    }

    @Test
    public void testHandleOffsetPaid() throws Exception {
        OffsetPaidFeeCommand command = new OffsetPaidFeeCommand(paidFeeId, offsetId, tradeTime);

        paidFixture.given(new PaidFeeCreatedEvent(
                paidFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.PAY_COIN,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                BusinessType.TRADE_EXECUTED,
                orderTransactionId.toString(),
                PaidMode.INTERNAL))
                .when(command)
                .expectEvents(new PaidFeeOffsetedEvent(paidFeeId, offsetId, tradeTime));
    }

    @Test
    public void testHandleCancelPaid() throws Exception {
        CancelPaidFeeCommand command =
                new CancelPaidFeeCommand(paidFeeId, CancelledReason.DUPLICATED, tradeTime);

        paidFixture.given(new PaidFeeCreatedEvent(
                paidFeeId,
                FeeStatus.PENDING,
                sellCommissionAmount,
                FeeType.PAY_COIN,
                tradeTime,
                dueDate,
                portfolioId.toString(),
                userId.toString(),
                BusinessType.TRADE_EXECUTED,
                orderTransactionId.toString(),
                PaidMode.INTERNAL))
                .when(command)
                .expectEvents(new PaidFeeCancelledEvent(paidFeeId, CancelledReason.DUPLICATED, tradeTime));
    }
}
