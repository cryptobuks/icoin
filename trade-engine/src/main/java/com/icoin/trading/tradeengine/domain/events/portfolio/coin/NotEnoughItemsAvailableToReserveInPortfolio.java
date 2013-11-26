/*
 * Copyright (c) 2010-2012. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.icoin.trading.tradeengine.domain.events.portfolio.coin;


import com.icoin.trading.tradeengine.domain.model.order.OrderBookId;
import com.icoin.trading.tradeengine.domain.model.portfolio.PortfolioId;
import com.icoin.trading.tradeengine.domain.model.transaction.TransactionId;

import java.math.BigDecimal;

/**
 * @author Jettro Coenradie
 */
public class NotEnoughItemsAvailableToReserveInPortfolio {
    private PortfolioId portfolioIdentifier;
    private OrderBookId orderBookIdentifier;
    private TransactionId transactionIdentifier;
    private BigDecimal availableAmountOfItems;
    private BigDecimal amountOfItemsToReserve;

    public NotEnoughItemsAvailableToReserveInPortfolio(PortfolioId portfolioIdentifier,
                                                       OrderBookId orderBookIdentifier,
                                                       TransactionId transactionIdentifier,
                                                       BigDecimal availableAmountOfItems,
                                                       BigDecimal amountOfItemsToReserve) {
        this.portfolioIdentifier = portfolioIdentifier;
        this.orderBookIdentifier = orderBookIdentifier;
        this.transactionIdentifier = transactionIdentifier;
        this.availableAmountOfItems = availableAmountOfItems;
        this.amountOfItemsToReserve = amountOfItemsToReserve;
    }

    public BigDecimal getAmountOfItemsToReserve() {
        return amountOfItemsToReserve;
    }

    public BigDecimal getAvailableAmountOfItems() {
        return availableAmountOfItems;
    }

    public OrderBookId getOrderBookIdentifier() {
        return orderBookIdentifier;
    }

    public PortfolioId getPortfolioIdentifier() {
        return portfolioIdentifier;
    }

    public TransactionId getTransactionIdentifier() {
        return transactionIdentifier;
    }
}