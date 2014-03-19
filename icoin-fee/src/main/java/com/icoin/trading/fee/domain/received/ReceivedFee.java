package com.icoin.trading.fee.domain.received;

import com.icoin.trading.api.fee.domain.fee.BusinessType;
import com.icoin.trading.api.fee.domain.fee.CancelledReason;
import com.icoin.trading.api.fee.domain.fee.FeeId;
import com.icoin.trading.api.fee.domain.fee.FeeStatus;
import com.icoin.trading.api.fee.domain.fee.FeeType;
import com.icoin.trading.api.fee.domain.received.ReceivedSource;
import com.icoin.trading.api.fee.events.fee.ReceivedFeeCancelledEvent;
import com.icoin.trading.api.fee.events.fee.ReceivedFeeConfirmedEvent;
import com.icoin.trading.api.fee.events.fee.ReceivedFeeCreatedEvent;
import com.icoin.trading.api.fee.events.fee.ReceivedFeeOffsetedEvent;
import com.icoin.trading.fee.domain.fee.FeeAggregateRoot;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.joda.money.BigMoney;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: liougehooa
 * Date: 4/10/13
 * Time: 3:14 PM
 */
public class ReceivedFee extends FeeAggregateRoot<ReceivedFee> {
    private ReceivedSource receivedSource;

    public ReceivedFee(FeeId feeId,
                       FeeStatus feeStatus,
                       BigMoney amount,
                       FeeType feeType,
                       Date dueDate,
                       Date businessCreationTime,
                       String userAccountId,
                       BusinessType businessType,
                       String businessReferenceId,
                       ReceivedSource receivedSource) {
        apply(new ReceivedFeeCreatedEvent(feeId,
                feeStatus,
                amount,
                feeType,
                dueDate,
                businessCreationTime,
                userAccountId,
                businessType,
                businessReferenceId,
                receivedSource));
    }

    public void confirm(Date confirmedDate) {
        apply(new ReceivedFeeConfirmedEvent(feeId, confirmedDate));
    }

    public void cancel(CancelledReason cancelReason, Date cancelledDate) {
        apply(new ReceivedFeeCancelledEvent(feeId, cancelReason, cancelledDate));
    }

    public void offset(Date offsetDate) {
        apply(new ReceivedFeeOffsetedEvent(feeId, offsetDate));
    }

    @EventHandler
    public void on(ReceivedFeeConfirmedEvent event) {
        onConfirm(event);
    }

    @EventHandler
    public void on(ReceivedFeeCancelledEvent event) {
        onCancel(event);
    }

    @EventHandler
    public void on(ReceivedFeeOffsetedEvent event) {
        onOffset(event);
    }

}