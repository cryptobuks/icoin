package com.icoin.trading.fee.saga;

import com.icoin.trading.api.fee.command.offset.CancelOffsetCommand;
import com.icoin.trading.api.fee.command.offset.OffsetFeesCommand;
import com.icoin.trading.api.fee.command.receivable.CancelAccountReceivableFeeCommand;
import com.icoin.trading.api.fee.command.receivable.ConfirmAccountReceivableFeeCommand;
import com.icoin.trading.api.fee.command.receivable.OffsetAccountReceivableFeeCommand;
import com.icoin.trading.api.fee.command.received.CancelReceivedFeeCommand;
import com.icoin.trading.api.fee.command.received.ConfirmReceivedFeeCommand;
import com.icoin.trading.api.fee.command.received.OffsetReceivedFeeCommand;
import com.icoin.trading.api.fee.domain.FeeTransactionId;
import com.icoin.trading.api.fee.domain.fee.FeeId;
import com.icoin.trading.api.fee.domain.offset.CancelledReason;
import com.icoin.trading.api.fee.domain.offset.OffsetId;
import com.icoin.trading.api.fee.events.fee.receivable.AccountReceivableFeeCancelledEvent;
import com.icoin.trading.api.fee.events.fee.receivable.AccountReceivableFeeConfirmedEvent;
import com.icoin.trading.api.fee.events.fee.receivable.AccountReceivableFeeCreatedEvent;
import com.icoin.trading.api.fee.events.fee.receivable.AccountReceivableFeeOffsetedEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeCancelledEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeConfirmedEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeCreatedEvent;
import com.icoin.trading.api.fee.events.fee.received.ReceivedFeeOffsetedEvent;
import com.icoin.trading.api.fee.events.offset.FeesOffsetedEvent;
import com.icoin.trading.api.fee.events.offset.OffsetAmountNotMatchedEvent;
import com.icoin.trading.api.fee.events.offset.OffsetCancelledEvent;
import com.icoin.trading.api.fee.events.offset.OffsetCreatedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.axonframework.saga.annotation.SagaEventHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: liougehooa
 * Date: 14-3-18
 * Time: PM9:01
 * To change this template use File | Settings | File Templates.
 */
public class ReceiveTransactionFeeManagerSaga extends AbstractAnnotatedSaga {
    protected transient CommandGateway commandGateway;

    protected FeeTransactionId feeTransactionId;
    protected FeeId accountReceivableId;
    protected FeeId receivedFeeId;
    protected OffsetId offsetId;
    protected TransactionStatus accountReceivableStatus = TransactionStatus.NEW;
    protected TransactionStatus paidFeeStatus = TransactionStatus.NEW;
    protected TransactionStatus offsetStatus = TransactionStatus.NEW;

    @SagaEventHandler(associationProperty = "feeId", keyName = "accountReceivableId")
    public void onReceivableCreated(final AccountReceivableFeeCreatedEvent event) {
        accountReceivableStatus = TransactionStatus.CREATED;

        commandGateway.send(new ConfirmAccountReceivableFeeCommand(event.getFeeId(), event.getBusinessCreationTime()));
    }

    @SagaEventHandler(associationProperty = "feeId", keyName = "receivedFeeId")
    public void onReceivedCreated(final ReceivedFeeCreatedEvent event) {
        paidFeeStatus = TransactionStatus.CREATED;

        commandGateway.send(new ConfirmReceivedFeeCommand(event.getFeeId(), event.getAmount(), event.getBusinessCreationTime()));
    }

    @SagaEventHandler(associationProperty = "offsetId")
    public void onOffsetCreated(final OffsetCreatedEvent event) {
        offsetStatus = TransactionStatus.CONFIRMED;

        commandGateway.send(new OffsetFeesCommand(offsetId, event.getStartedDate()));
    }

    @SagaEventHandler(associationProperty = "offsetId")
    public void onFeesOffseted(final FeesOffsetedEvent event) {
        offsetStatus = TransactionStatus.OFFSETED;

        offsetIfPossible(event.getOffsetedDate());
    }

    private void offsetIfPossible(Date offsetDate) {
        if (offsetStatus == TransactionStatus.OFFSETED
                && accountReceivableStatus == TransactionStatus.CONFIRMED
                && paidFeeStatus == TransactionStatus.CONFIRMED) {
            commandGateway.send(new OffsetAccountReceivableFeeCommand(accountReceivableId, offsetId, offsetDate));
            commandGateway.send(new OffsetReceivedFeeCommand(receivedFeeId, offsetId, offsetDate));
        }
    }

    @SagaEventHandler(associationProperty = "feeId", keyName = "accountReceivableId")
    public void onReceivableConfirmed(final AccountReceivableFeeConfirmedEvent event) {
        accountReceivableStatus = TransactionStatus.CONFIRMED;

        offsetIfPossible(event.getConfirmedDate());
    }

    @SagaEventHandler(associationProperty = "feeId", keyName = "receivedFeeId")
    public void onReceivedConfirmed(final ReceivedFeeConfirmedEvent event) {
        paidFeeStatus = TransactionStatus.CONFIRMED;

        offsetIfPossible(event.getConfirmedDate());
    }

    @SagaEventHandler(associationProperty = "feeId", keyName = "accountReceivableId")
    public void onReceivableOffseted(final AccountReceivableFeeOffsetedEvent event) {
        accountReceivableStatus = TransactionStatus.OFFSETED;

        completeIfPossible();
    }

    @SagaEventHandler(associationProperty = "feeId", keyName = "receivedFeeId")
    public void onReceivedOffseted(final ReceivedFeeOffsetedEvent event) {
        paidFeeStatus = TransactionStatus.OFFSETED;

        completeIfPossible();
    }

    private void completeIfPossible() {
        if (offsetStatus == TransactionStatus.OFFSETED
                && accountReceivableStatus == TransactionStatus.OFFSETED
                && paidFeeStatus == TransactionStatus.OFFSETED) {
            end();
        }

        if (offsetStatus == TransactionStatus.CANCELLED
                && accountReceivableStatus == TransactionStatus.CANCELLED
                && paidFeeStatus == TransactionStatus.CANCELLED) {
            end();
        }
    }

    @SagaEventHandler(associationProperty = "offsetId")
    public void onOffsetAmountNotMatched(final OffsetAmountNotMatchedEvent event) {
        commandGateway.send(new CancelOffsetCommand(offsetId, CancelledReason.AMOUNT_NOT_MATCHED, event.getOffsetDate()));
    }

    @SagaEventHandler(associationProperty = "offsetId")
    public void onOffsetCancelled(final OffsetCancelledEvent event) {
        offsetStatus = TransactionStatus.CANCELLED;

        commandGateway.send(new CancelAccountReceivableFeeCommand(accountReceivableId, com.icoin.trading.api.fee.domain.fee.CancelledReason.OFFSET_ERROR, event.getCancelledDate()));
        commandGateway.send(new CancelReceivedFeeCommand(receivedFeeId, com.icoin.trading.api.fee.domain.fee.CancelledReason.OFFSET_ERROR, event.getCancelledDate()));
    }

    @SagaEventHandler(associationProperty = "feeId", keyName = "accountReceivableId")
    public void onReceivableCancelled(final AccountReceivableFeeCancelledEvent event) {
        accountReceivableStatus = TransactionStatus.CANCELLED;

        completeIfPossible();
    }

    @SagaEventHandler(associationProperty = "feeId", keyName = "receivedFeeId")
    public void onReceivedCancelled(final ReceivedFeeCancelledEvent event) {
        paidFeeStatus = TransactionStatus.CANCELLED;

        completeIfPossible();
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
}