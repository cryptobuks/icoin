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

package com.icoin.trading.tradeengine.application.command.transaction.command;


import com.homhon.base.command.CommandSupport;
import com.icoin.trading.tradeengine.domain.model.coin.CoinId;
import com.icoin.trading.tradeengine.domain.model.transaction.TransactionId;
import org.joda.money.BigMoney;

import java.util.Date;

/**
 * @author Jettro Coenradie
 */
public class ExecutedTransactionCommand extends CommandSupport<ExecutedTransactionCommand> {

    private TransactionId transactionIdentifier;
    private CoinId coinId;
    private BigMoney tradeAmount;
    private BigMoney itemPrice;
    private BigMoney executedMoney;
    private BigMoney commission;
    private Date time;

    public ExecutedTransactionCommand(TransactionId transactionIdentifier,
                                      CoinId coinId,
                                      BigMoney tradeAmount,
                                      BigMoney itemPrice,
                                      BigMoney executedMoney,
                                      BigMoney commission,
                                      Date time) {
        this.transactionIdentifier = transactionIdentifier;
        this.coinId = coinId;
        this.tradeAmount = tradeAmount;
        this.itemPrice = itemPrice;
        this.executedMoney = executedMoney;
        this.commission = commission;
        this.time = time;
    }

    public CoinId getCoinId() {
        return coinId;
    }

    public BigMoney getTradeAmount() {
        return tradeAmount;
    }

    public BigMoney getItemPrice() {
        return itemPrice;
    }

    public TransactionId getTransactionIdentifier() {
        return transactionIdentifier;
    }


    public BigMoney getExecutedMoney() {
        return executedMoney;
    }

    public BigMoney getCommission() {
        return commission;
    }

    public Date getTime() {
        return time;
    }
}
